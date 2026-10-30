package com.example.myapplication

import android.app.Notification
import android.content.ContentResolver
import android.provider.ContactsContract
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SentinelNotificationListener : NotificationListenerService() {

    private val appsMonitorados = setOf("com.whatsapp")

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName !in appsMonitorados) return

        val extras = sbn.notification.extras
        val titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        if (SentinelState.repetido(texto)) return

        val instituicaoOficial = OficialContatos.identificar(titulo)
        if (instituicaoOficial != null) {
            Log.d("Sentinel", "WhatsApp de número oficial: $instituicaoOficial")
            SentinelState.registrar("WhatsApp ($titulo)", texto, listOf("Canal oficial: $instituicaoOficial"), StatusAlerta.OFICIAL)
            NotificacaoHelper.notificarOficial(this, "WhatsApp ($titulo)", instituicaoOficial)
            return
        }

        val conhecido = estaNosContatos(titulo)
        if (conhecido) {
            Log.d("Sentinel", "Mensagem de contato conhecido ($titulo), ignorando")
            return
        }

        Log.d("Sentinel", "WhatsApp de número desconhecido: $titulo")

        val sinaisExtras = listOf("Número desconhecido" to 20)
        val resultado = ScamDetector.analisar(texto, sinaisExtras)
        SentinelState.registrarAnalise()
        Log.d("Sentinel", "WhatsApp score=${resultado.score}, nivel=${resultado.nivel}")

        when (resultado.nivel) {
            NivelRisco.SUSPEITO, NivelRisco.DUVIDOSO -> {
                SentinelState.registrar("WhatsApp ($titulo)", texto, resultado.motivos, StatusAlerta.CONFIRMADO_LOCAL, resultado.nivel)
                NotificacaoHelper.notificar(this, "WhatsApp ($titulo)", resultado.motivos, texto)
            }
            NivelRisco.SEGURO -> {}
        }
    }

    private fun estaNosContatos(nomeOuNumero: String): Boolean {
        val pareceNumero = nomeOuNumero.all { it.isDigit() || it in "+()- " }
        if (!pareceNumero) return true

        val resolver: ContentResolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
                .appendPath(nomeOuNumero).build(),
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null, null, null
        )
        val encontrado = cursor?.use { it.moveToFirst() } ?: false
        return encontrado
    }
}