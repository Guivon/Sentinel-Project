package com.example.myapplication

import android.content.Context
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException

class GmailCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val emailConta = GmailHelper.emailConectado(applicationContext) ?: return Result.success()

        return try {
            val credential = GmailHelper.criarCredential(applicationContext, emailConta)
            val gmail = GmailHelper.criarServico(credential)
            val emails = GmailHelper.buscarUltimosEmails(gmail, 10)
            val prefs = applicationContext.getSharedPreferences("sentinel_gmail", Context.MODE_PRIVATE)
            val idsProcessados = prefs.getStringSet("ids_processados", emptySet()) ?: emptySet()
            val novosIds = mutableSetOf<String>()

            for (email in emails) {
                novosIds.add(email.id)
                if (email.id in idsProcessados) continue

                val instituicaoOficial = OficialContatos.identificar(email.remetente)
                if (instituicaoOficial != null) {
                    SentinelState.registrar("E-mail (${email.remetente})", email.assunto, listOf("Canal oficial: $instituicaoOficial"), StatusAlerta.OFICIAL)
                    NotificacaoHelper.notificarOficial(applicationContext, "E-mail (${email.remetente})", instituicaoOficial)
                    continue
                }

                val textoCompleto = "${email.assunto} ${email.corpo}"
                val resultado = ScamDetector.analisar(textoCompleto)
                SentinelState.registrarAnalise()

                when (resultado.nivel) {
                    NivelRisco.SUSPEITO, NivelRisco.DUVIDOSO -> {
                        SentinelState.registrar("E-mail (${email.remetente})", email.assunto, resultado.motivos, StatusAlerta.CONFIRMADO_LOCAL)
                        NotificacaoHelper.notificar(applicationContext, "E-mail (${email.remetente})", resultado.motivos, email.assunto)
                    }
                    NivelRisco.SEGURO -> {}
                }
            }

            prefs.edit { putStringSet("ids_processados", (idsProcessados + novosIds).toList().takeLast(200).toSet()) }

            Result.success()
        } catch (e: UserRecoverableAuthIOException) {
            Result.failure()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}