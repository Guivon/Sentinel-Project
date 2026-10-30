package com.example.myapplication

import android.accounts.Account
import android.content.Context
import androidx.core.content.edit
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64

object GmailHelper {

    private const val PREFS = "sentinel_gmail"
    private const val CHAVE_EMAIL = "email_conectado"

    fun salvarEmailConectado(context: Context, email: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(CHAVE_EMAIL, email)
        }
    }

    fun emailConectado(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CHAVE_EMAIL, null)
    }

    fun criarCredential(context: Context, email: String): GoogleAccountCredential {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = Account(email, "com.google")
        return credential
    }

    fun criarServico(credential: GoogleAccountCredential): Gmail {
        return Gmail.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Sentinel").build()
    }

    suspend fun buscarUltimosEmails(gmail: Gmail, quantidade: Long = 10): List<EmailResumo> = withContext(Dispatchers.IO) {
        val lista = gmail.users().messages().list("me").setMaxResults(quantidade).execute()
        val mensagens = lista.messages ?: return@withContext emptyList()

        mensagens.mapNotNull { msgRef ->
            val msg = gmail.users().messages().get("me", msgRef.id).execute()
            val headers = msg.payload?.headers ?: return@mapNotNull null

            val remetente = headers.find { it.name == "From" }?.value ?: "Desconhecido"
            val assunto = headers.find { it.name == "Subject" }?.value ?: ""
            val corpo = extrairCorpo(msg)

            EmailResumo(msg.id, remetente, assunto, corpo)
        }
    }

    private fun extrairCorpo(msg: com.google.api.services.gmail.model.Message): String {
        val parts = msg.payload?.parts
        val bodyData = if (parts != null) {
            parts.find { it.mimeType == "text/plain" }?.body?.data
                ?: parts.firstOrNull()?.body?.data
        } else {
            msg.payload?.body?.data
        }

        return if (bodyData != null) {
            String(Base64.getUrlDecoder().decode(bodyData))
        } else {
            msg.snippet ?: ""
        }
    }

    data class EmailResumo(
        val id: String,
        val remetente: String,
        val assunto: String,
        val corpo: String
    )
}