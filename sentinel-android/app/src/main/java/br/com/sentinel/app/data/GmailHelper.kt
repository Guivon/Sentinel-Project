package br.com.sentinel.app.data

import android.accounts.Account
import android.content.Context
import androidx.core.content.edit
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64

object GmailHelper {

    private const val PREFS = "sentinel_gmail"
    private const val KEY_EMAIL = "email_conectado"

    fun saveConnectedEmail(context: Context, email: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_EMAIL, email)
        }
    }

    fun connectedEmail(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EMAIL, null)
    }

    fun createCredential(context: Context, email: String): GoogleAccountCredential {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = Account(email, "com.google")
        return credential
    }

    fun createService(credential: GoogleAccountCredential): Gmail {
        return Gmail.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Sentinel").build()
    }

    suspend fun fetchLatestEmails(gmail: Gmail, limit: Long = 10): List<EmailSummary> = withContext(Dispatchers.IO) {
        val listResponse = gmail.users().messages().list("me").setMaxResults(limit).execute()
        val messages = listResponse.messages ?: return@withContext emptyList()

        messages.mapNotNull { messageRef ->
            val msg = gmail.users().messages().get("me", messageRef.id).execute()
            val headers = msg.payload?.headers ?: return@mapNotNull null

            val sender = headers.find { it.name.equals("From", ignoreCase = true) }?.value ?: "Desconhecido"
            val subject = headers.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: ""
            val body = extractBody(msg)
            val authHeader = headers.firstOrNull { it.name.equals("Authentication-Results", ignoreCase = true) }
            val authentication = EmailAuthentication.parse(authHeader?.value.orEmpty())

            EmailSummary(msg.id, sender, subject, body, authentication)
        }
    }

    private fun extractBody(msg: Message): String {
        val payload = msg.payload ?: return msg.snippet ?: ""
        val content = findPart(payload, "text/plain")
            ?: findPart(payload, "text/html")?.let { it.replace(Regex("<[^>]+>"), " ") }
        return content ?: msg.snippet ?: ""
    }

    private fun findPart(part: MessagePart, mimeType: String): String? {
        if (part.mimeType == mimeType) {
            part.body?.data?.let { return String(Base64.getUrlDecoder().decode(it), Charsets.UTF_8) }
        }
        part.parts?.forEach { childPart -> findPart(childPart, mimeType)?.let { return it } }
        return null
    }

    data class EmailAuthentication(val spf: String?, val dkim: String?, val dmarc: String?) {

        val failed: Boolean
            get() = listOf(spf, dkim, dmarc).any { it in FAILED_RESULTS }

        companion object {
            private val FAILED_RESULTS = setOf("fail", "softfail", "permerror", "temperror")

            fun parse(header: String): EmailAuthentication = EmailAuthentication(
                spf = result("spf", header),
                dkim = result("dkim", header),
                dmarc = result("dmarc", header)
            )

            private fun result(mechanism: String, header: String): String? =
                Regex("\\b$mechanism=(\\w+)", RegexOption.IGNORE_CASE)
                    .find(header)?.groupValues?.get(1)?.lowercase()
        }
    }

    data class EmailSummary(
        val id: String,
        val sender: String,
        val subject: String,
        val body: String,
        val authentication: EmailAuthentication
    )
}
