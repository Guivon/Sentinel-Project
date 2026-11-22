package br.com.sentinel.app

import br.com.sentinel.app.data.GmailHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailAuthenticationTest {

    @Test
    fun `passing results are not a failure`() {
        val result = GmailHelper.EmailAuthentication.parse(
            "mx.google.com; dkim=pass header.i=@bank.com; spf=pass smtp.mailfrom=bank.com; dmarc=pass"
        )
        assertEquals("pass", result.spf)
        assertEquals("pass", result.dkim)
        assertFalse(result.failed)
    }

    @Test
    fun `failed spf marks the message`() {
        val result = GmailHelper.EmailAuthentication.parse("mx.google.com; spf=fail; dkim=pass; dmarc=pass")
        assertTrue(result.failed)
    }

    @Test
    fun `missing header is not a failure`() {
        val result = GmailHelper.EmailAuthentication.parse("")
        assertFalse(result.failed)
    }
}
