package br.com.sentinel.app.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.sentinel.app.R
import br.com.sentinel.app.data.GmailHelper
import br.com.sentinel.app.domain.AutoAnalysis
import br.com.sentinel.app.domain.OfficialContacts
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException

class GmailCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val accountEmail = GmailHelper.connectedEmail(applicationContext) ?: return Result.success()

        return try {
            val credential = GmailHelper.createCredential(applicationContext, accountEmail)
            val gmail = GmailHelper.createService(credential)
            val emails = GmailHelper.fetchLatestEmails(gmail, 10)
            val prefs = applicationContext.getSharedPreferences("sentinel_gmail", Context.MODE_PRIVATE)
            val processedIds = readProcessedIds(prefs)
            val newIds = mutableListOf<String>()

            for (email in emails) {
                if (email.id !in newIds) newIds.add(email.id)
                if (email.id in processedIds) continue

                AutoAnalysis.process(
                    context = applicationContext,
                    source = "E-mail (${email.sender})",
                    backendApp = "E-mail",
                    content = "${email.subject} ${email.body}",
                    historyContent = email.subject,
                    officialInstitution = OfficialContacts.identify(email.sender),
                    extraSigns = authenticationSigns(email.authentication)
                )
            }

            val updatedItems = (processedIds + newIds.filter { it !in processedIds }).takeLast(MAX_IDS)
            prefs.edit { putString(KEY_IDS, updatedItems.joinToString(",")) }

            Result.success()
        } catch (e: UserRecoverableAuthIOException) {
            Log.w("Sentinel", "Gmail precisa de nova autorização do usuário")
            Result.failure()
        } catch (e: Exception) {
            Log.w("Sentinel", "Falha ao verificar o Gmail: ${e.message}")
            Result.retry()
        }
    }

    private fun authenticationSigns(authentication: GmailHelper.EmailAuthentication): List<Pair<String, Int>> {
        if (!authentication.failed) return emptyList()
        return listOf(applicationContext.getString(R.string.email_authentication_failed) to AUTH_FAILURE_WEIGHT)
    }

    private fun readProcessedIds(prefs: SharedPreferences): List<String> {
        val ordered = prefs.getString(KEY_IDS, "").orEmpty().split(",").filter { it.isNotBlank() }

        val oldItems = prefs.getStringSet(KEY_IDS_LEGACY, emptySet()).orEmpty().filter { it !in ordered }
        return oldItems + ordered
    }

    private companion object {
        const val KEY_IDS = "ids_ordenados"
        const val KEY_IDS_LEGACY = "ids_processados"
        const val MAX_IDS = 200
        const val AUTH_FAILURE_WEIGHT = 20
    }
}
