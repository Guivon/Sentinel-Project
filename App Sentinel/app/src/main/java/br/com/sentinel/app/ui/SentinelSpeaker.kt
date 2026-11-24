package br.com.sentinel.app.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object SentinelSpeaker {
    private const val UTTERANCE_ID = "sentinel_read_aloud"

    private var engine: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null

    fun init(context: Context) {
        if (engine != null) return
        engine = TextToSpeech(context.applicationContext) { status ->
            val tts = engine
            if (status == TextToSpeech.SUCCESS && tts != null) {
                tts.language = Locale("pt", "BR")
                ready = true
                pending?.let { speak(it) }
                pending = null
            }
        }
    }

    fun speak(text: String) {
        val tts = engine
        if (tts == null || !ready) {
            pending = text
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun shutdown() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
        pending = null
    }
}
