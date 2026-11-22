package br.com.sentinel.app.services

import android.Manifest
import android.app.Notification
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.content.ContextCompat
import br.com.sentinel.app.data.SentinelState
import br.com.sentinel.app.domain.AutoAnalysis
import br.com.sentinel.app.domain.OfficialContacts
import br.com.sentinel.app.domain.SenderContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SentinelNotificationListener : NotificationListenerService() {

    private val monitoredApps = setOf("com.whatsapp")
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName !in monitoredApps) return

        val extras = sbn.notification.extras
        val heading = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        if (SentinelState.repeated(content)) return

        val officialInstitution = OfficialContacts.identify(heading)
        if (officialInstitution == null && isInContacts(heading)) {
            Log.d("Sentinel", "Mensagem de contato salvo, ignorando")
            return
        }

        val extraSigns = if (officialInstitution == null) listOf("Número desconhecido" to 20) else emptyList()
        val appCtx = applicationContext
        serviceScope.launch {
            try {
                AutoAnalysis.process(
                    context = appCtx,
                    source = "WhatsApp ($heading)",
                    backendApp = "WhatsApp",
                    content = content,
                    officialInstitution = officialInstitution,
                    extraSigns = extraSigns,
                    sender = SenderContext(heading, false, null)
                )
            } catch (e: Exception) {
                Log.e("Sentinel", "Erro ao analisar notificação: ${e.message}")
            }
        }
    }

    private fun isInContacts(nameOrNumber: String): Boolean {
        val allowed = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!allowed) return false

        return try {
            val looksLikeNumber = nameOrNumber.all { it.isDigit() || it in "+()- " }
            val cursor = if (looksLikeNumber) {
                contentResolver.query(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
                        .appendPath(nameOrNumber).build(),
                    arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                    null,
                    null,
                    null
                )
            } else {
                contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
                    "${ContactsContract.Contacts.DISPLAY_NAME} = ?",
                    arrayOf(nameOrNumber),
                    null
                )
            }
            cursor?.use { it.moveToFirst() } ?: false
        } catch (e: Exception) {
            Log.w("Sentinel", "Não foi possível consultar contatos: ${e.message}")
            false
        }
    }
}
