package br.com.sentinel.app.domain

import android.content.Context
import android.util.Log
import br.com.sentinel.app.data.AlertStatus
import br.com.sentinel.app.data.SentinelApi
import br.com.sentinel.app.data.SentinelState
import br.com.sentinel.app.services.NotificationHelper
import kotlinx.coroutines.withTimeoutOrNull

object AutoAnalysis {

    private const val TAG = "Sentinel"
    private const val AI_TIMEOUT_MS = 8_000L

    suspend fun process(
        context: Context,
        source: String,
        backendApp: String,
        content: String,
        historyContent: String = content,
        officialInstitution: String? = null,
        extraSigns: List<Pair<String, Int>> = emptyList()
    ) {
        val local = ScamDetector.analyze(content, extraSigns)
        SentinelState.recordAnalysis()
        Log.d(TAG, "$source: score=${local.score}, nivel=${local.level}")

        if (local.level == RiskLevel.SAFE) {
            if (officialInstitution != null) {
                SentinelState.record(
                    source,
                    historyContent,
                    listOf("Diz ser de $officialInstitution (identidade não verificada)"),
                    AlertStatus.OFFICIAL
                )
                NotificationHelper.sendOfficialNotification(context, source, officialInstitution)
            }
            return
        }

        val response = withTimeoutOrNull(AI_TIMEOUT_MS) {
            SentinelApi.analyze(backendApp, content, appScore = local.score)
        }
        val final = if (response != null) {
            ScamDetector.DetectionResult(response.appLevel(), response.aiScore, response.reasons)
        } else {
            local
        }
        val status = if (response != null) AlertStatus.CONFIRMED_AI else AlertStatus.CONFIRMED_LOCAL

        if (final.level == RiskLevel.SAFE) {
            SentinelState.record(source, historyContent, final.reasons, AlertStatus.DISMISSED, final.level)
            return
        }

        val reasons = if (officialInstitution != null) {
            listOf("Diz ser de $officialInstitution, mas o conteúdo é suspeito (possível falsificação)") + final.reasons
        } else {
            final.reasons
        }
        SentinelState.record(source, historyContent, reasons, status, final.level)
        NotificationHelper.sendNotification(context, source, reasons, historyContent)
    }
}
