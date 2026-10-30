package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SentinelAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var pendente = false
    private val processar = Runnable {
        pendente = false
        analisarTela()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pacote = event?.packageName?.toString() ?: return
        if (ignorar(pacote)) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            handler.post(processar)
            return
        }
        if (!pendente) {
            pendente = true
            handler.postDelayed(processar, ATRASO_MS)
        }
    }

    override fun onInterrupt() {
        handler.removeCallbacks(processar)
    }

    override fun onDestroy() {
        handler.removeCallbacks(processar)
        instance = null
        super.onDestroy()
    }

    private fun ignorar(pacote: String) = pacote == packageName || pacote in PACOTES_IGNORADOS

    @Suppress("DEPRECATION")
    private fun analisarTela() {
        val raiz = rootInActiveWindow ?: return
        try {
            val pacote = raiz.packageName?.toString() ?: return
            if (ignorar(pacote)) return

            val blocos = mutableListOf<String>()
            coletar(raiz, blocos, intArrayOf(0))

            val suspeitos = mutableListOf<Pair<String, ScamDetector.Resultado>>()
            var analisou = false
            for (bloco in blocos) {
                if (!SentinelState.repetido(bloco)) analisou = true
                val resultado = ScamDetector.analisar(bloco)
                if (resultado.nivel != NivelRisco.SEGURO && resultado.score >= SCORE_MINIMO_TELA) {
                    suspeitos.add(bloco to resultado)
                }
            }
            if (analisou) SentinelState.registrarAnalise()

            val nome = nomeApp(pacote)
            suspeitos
                .sortedByDescending { it.second.score }
                .take(MAX_ALERTAS_POR_TELA)
                .forEach { (bloco, resultado) ->
                    if (!SentinelState.podeAlertar("tela", bloco)) return@forEach
                    SentinelState.registrar(
                        "Tela • $nome", bloco, resultado.motivos,
                        StatusAlerta.CONFIRMADO_LOCAL, resultado.nivel
                    )
                    NotificacaoHelper.notificar(this, "$nome (tela)", resultado.motivos, bloco)
                }
        } finally {
            raiz.recycle()
        }
    }

    @Suppress("DEPRECATION")
    private fun coletar(no: AccessibilityNodeInfo?, saida: MutableList<String>, contador: IntArray) {
        if (no == null || contador[0] >= MAX_NOS || saida.size >= MAX_BLOCOS) return
        contador[0]++
        if (!no.isVisibleToUser || no.isPassword) return

        if (!no.isEditable) {
            val texto = no.text?.toString()?.trim()
            if (!texto.isNullOrEmpty() && texto.length >= TAMANHO_MINIMO) {
                saida.add(texto.take(TAMANHO_MAXIMO))
            }
        }

        for (i in 0 until no.childCount) {
            val filho = no.getChild(i) ?: continue
            coletar(filho, saida, contador)
            filho.recycle()
        }
    }

    private fun nomeApp(pacote: String): String {
        NOMES_CONHECIDOS[pacote]?.let { return it }
        return try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pacote, 0)).toString()
        } catch (_: Exception) {
            pacote
        }
    }

    companion object {
        @Volatile
        private var instance: SentinelAccessibilityService? = null

        fun desligar(): Boolean {
            val servico = instance ?: return false
            servico.disableSelf()
            return true
        }

        private const val ATRASO_MS = 250L
        private const val MAX_NOS = 600
        private const val MAX_BLOCOS = 40
        private const val TAMANHO_MINIMO = 25
        private const val TAMANHO_MAXIMO = 600
        private const val MAX_ALERTAS_POR_TELA = 3
        private const val SCORE_MINIMO_TELA = 30

        private val PACOTES_IGNORADOS = setOf(
            "com.android.systemui",
            "com.google.android.inputmethod.latin",
            "com.samsung.android.honeyboard",
            "com.google.android.apps.nexuslauncher",
            "com.sec.android.app.launcher",
            "br.com.bb.android"
        )

        private val NOMES_CONHECIDOS = mapOf(
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WhatsApp Business",
            "org.telegram.messenger" to "Telegram",
            "com.google.android.apps.messaging" to "Mensagens",
            "com.samsung.android.messaging" to "Mensagens",
            "com.google.android.gm" to "Gmail",
            "com.instagram.android" to "Instagram",
            "com.facebook.katana" to "Facebook",
            "com.facebook.orca" to "Messenger",
            "com.android.chrome" to "Chrome"
        )
    }
}