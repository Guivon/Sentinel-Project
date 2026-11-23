package br.com.sentinel.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AlertStatus
import br.com.sentinel.app.data.SentinelApi
import br.com.sentinel.app.data.SentinelState
import br.com.sentinel.app.domain.QrScanner
import br.com.sentinel.app.domain.RiskLevel
import br.com.sentinel.app.domain.ScamDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AnalysisUiState(
    val result: ScamDetector.DetectionResult? = null,
    val content: String = "",
    val analyzingAi: Boolean = false,
    val aiUnavailable: Boolean = false,
    val abortMessage: Int? = null
)

class AnalysisViewModel : ViewModel() {

    private val mutableState = MutableStateFlow(AnalysisUiState())
    val state: StateFlow<AnalysisUiState> = mutableState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun consumeAbortMessage() {
        mutableState.update { it.copy(abortMessage = null) }
    }

    fun analyze(context: Context, text: String, file: AttachedFile?) {
        val appContext = context.applicationContext
        val historyContent = when {
            file == null -> text
            text.isBlank() -> appContext.getString(R.string.attachment_history_only, file.name)
            else -> appContext.getString(R.string.attachment_history_with_text, file.name, text)
        }
        val localResult = ScamDetector.analyze(text)
        SentinelState.recordAnalysis()
        mutableState.value = AnalysisUiState(
            result = localResult,
            content = historyContent,
            analyzingAi = true
        )

        scope.launch {
            val upload = file?.let { withContext(Dispatchers.IO) { readUpload(appContext, it) } }
            if (file != null && upload == null) {
                abort(R.string.attachment_unreadable)
                return@launch
            }
            val source = appContext.getString(R.string.manual_analysis)
            val pix = upload?.let { QrScanner.findPix(appContext, it.bytes) }
            val response = SentinelApi.analyze(
                source,
                text,
                appScore = localResult.score,
                file = upload,
                pix = pix
            )
            if (response == null && file != null && text.isBlank()) {
                abort(R.string.attachment_analysis_failed)
                return@launch
            }
            val finalResult = if (response != null) {
                ScamDetector.DetectionResult(
                    level = response.appLevel(),
                    score = response.aiScore,
                    reasons = response.reasons
                )
            } else {
                localResult
            }
            mutableState.update {
                it.copy(result = finalResult, analyzingAi = false, aiUnavailable = response == null)
            }
            val status = when {
                finalResult.level == RiskLevel.SAFE -> AlertStatus.DISMISSED
                response != null -> AlertStatus.CONFIRMED_AI
                else -> AlertStatus.CONFIRMED_LOCAL
            }
            SentinelState.record(source, historyContent, finalResult.reasons, status, finalResult.level)
        }
    }

    private fun abort(messageRes: Int) {
        mutableState.update { it.copy(analyzingAi = false, abortMessage = messageRes) }
    }

    override fun onCleared() {
        scope.cancel()
    }
}
