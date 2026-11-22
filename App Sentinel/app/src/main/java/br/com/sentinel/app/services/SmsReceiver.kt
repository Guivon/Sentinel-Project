package br.com.sentinel.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import br.com.sentinel.app.domain.AutoAnalysis
import br.com.sentinel.app.domain.OfficialContacts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val bySender = linkedMapOf<String, StringBuilder>()
        for (part in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            val sender = part.originatingAddress ?: "Desconhecido"
            val body = part.messageBody ?: continue
            bySender.getOrPut(sender) { StringBuilder() }.append(body)
        }
        if (bySender.isEmpty()) return

        val pending = goAsync()
        val appCtx = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                for ((sender, content) in bySender) {
                    AutoAnalysis.process(
                        context = appCtx,
                        source = "SMS ($sender)",
                        backendApp = "SMS",
                        content = content.toString(),
                        officialInstitution = OfficialContacts.identify(sender)
                    )
                }
            } catch (e: Exception) {
                Log.e("Sentinel", "Erro ao analisar SMS: ${e.message}")
            } finally {
                pending.finish()
            }
        }
    }
}
