package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val mensagens = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (msg in mensagens) {
            val remetente = msg.originatingAddress ?: "Desconhecido"
            val texto = msg.messageBody ?: continue

            val instituicaoOficial = OficialContatos.identificar(remetente)
            if (instituicaoOficial != null) {
                Log.d("Sentinel", "SMS de número oficial: $instituicaoOficial")
                SentinelState.registrar("SMS ($remetente)", texto, listOf("Canal oficial: $instituicaoOficial"), StatusAlerta.OFICIAL)
                NotificacaoHelper.notificarOficial(context, "SMS ($remetente)", instituicaoOficial)
                continue
            }

            val resultado = ScamDetector.analisar(texto)
            SentinelState.registrarAnalise()
            Log.d("Sentinel", "SMS de $remetente, score=${resultado.score}, nivel=${resultado.nivel}")

            when (resultado.nivel) {
                NivelRisco.SUSPEITO, NivelRisco.DUVIDOSO -> {
                    SentinelState.registrar("SMS ($remetente)", texto, resultado.motivos, StatusAlerta.CONFIRMADO_LOCAL)
                    NotificacaoHelper.notificar(context, "SMS ($remetente)", resultado.motivos, texto)
                }
                NivelRisco.SEGURO -> {}
            }
        }
    }
}