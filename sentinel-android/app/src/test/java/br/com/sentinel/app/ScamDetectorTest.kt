package br.com.sentinel.app

import br.com.sentinel.app.domain.RiskLevel
import br.com.sentinel.app.domain.ScamDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScamDetectorTest {

    @Test
    fun `normal message is safe`() {
        assertEquals(RiskLevel.SAFE, ScamDetector.analyze("Oi, vamos almoçar amanhã?").level)
    }

    @Test
    fun `empty text is safe`() {
        assertEquals(RiskLevel.SAFE, ScamDetector.analyze("").level)
        assertEquals(RiskLevel.SAFE, ScamDetector.analyze(null).level)
    }

    @Test
    fun `violence threat is never marked safe`() {
        val result = ScamDetector.analyze("vou te matar")
        assertTrue(result.level != RiskLevel.SAFE)
        assertTrue(result.strongSignal)
    }

    @Test
    fun `money under threat becomes suspicious`() {
        val result = ScamDetector.analyze("Faça o pix agora ou sua conta será bloqueada, confirme sua senha")
        assertEquals(RiskLevel.SUSPICIOUS, result.level)
    }

    @Test
    fun `accents do not interfere`() {
        assertTrue(ScamDetector.analyze("TRANSFERÊNCIA URGENTE").score > 0)
    }
}
