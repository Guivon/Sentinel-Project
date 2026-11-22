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
        extraSigns: List<Pair<String, Int>> = emptyList(),
        sender: SenderContext? = null
    ) {
        val signs = extraSigns + senderSigns(content, sender) + OfficialContacts.lookalikeSigns(content)
        val local = ScamDetector.analyze(content, signs)
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
            SentinelApi.analyze(backendApp, content, appScore = local.score, sender = sender)
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

        BehaviorMonitor.arm(final.score)
        val reasons = if (officialInstitution != null) {
            listOf("Diz ser de $officialInstitution, mas o conteúdo é suspeito (possível falsificação)") + final.reasons
        } else {
            final.reasons
        }
        SentinelState.record(source, historyContent, reasons, status, final.level)
        NotificationHelper.sendNotification(context, source, reasons, historyContent)
    }

    private fun senderSigns(content: String, sender: SenderContext?): List<Pair<String, Int>> {
        if (sender == null || sender.officialInstitution != null || sender.inContacts != false) return emptyList()
        if (!ScamDetector.analyze(content).asksPayment) return emptyList()
        return listOf("Remetente fora dos contatos pedindo pagamento" to UNKNOWN_PAYMENT_WEIGHT)
    }

    private const val UNKNOWN_PAYMENT_WEIGHT = 25

    suspend fun processBehavior(context: Context, report: BehaviorMonitor.Report) {
        val response = withTimeoutOrNull(AI_TIMEOUT_MS) {
            SentinelApi.analyze("Comportamento", report.summary, appScore = report.armedScore)
        }
        val level = response?.appLevel()
            ?: if (report.signals.size >= 2 || report.fakeLink) RiskLevel.SUSPICIOUS else RiskLevel.DOUBTFUL
        if (level == RiskLevel.SAFE) return
        val reasons = response?.reasons?.takeIf { it.isNotEmpty() } ?: report.signals
        val status = if (response != null) AlertStatus.CONFIRMED_AI else AlertStatus.CONFIRMED_LOCAL
        SentinelState.record("Comportamento", report.signals.joinToString("; "), reasons, status, level)
        NotificationHelper.sendNotification(context, "Comportamento", reasons, report.signals.joinToString("; "))
    }
}
