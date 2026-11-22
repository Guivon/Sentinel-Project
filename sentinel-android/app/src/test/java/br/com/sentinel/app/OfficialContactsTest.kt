package br.com.sentinel.app

import br.com.sentinel.app.domain.OfficialContacts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfficialContactsTest {

    @Test
    fun `recognizes address inside angle brackets`() {
        assertEquals("Banco do Brasil", OfficialContacts.identify("Banco do Brasil <atendimento@bb.com.br>"))
    }

    @Test
    fun `recognizes official subdomain`() {
        assertEquals("Banco do Brasil", OfficialContacts.identify("alertas@mail.bb.com.br"))
    }

    @Test
    fun `does not confuse lookalike domain`() {
        assertNull(OfficialContacts.identify("falso@bb.com.br.golpe.com"))
        assertNull(OfficialContacts.identify("falso@notbb.com.br"))
    }

    @Test
    fun `official name in text does not fool when address differs`() {
        assertNull(OfficialContacts.identify("Banco do Brasil bb.com.br <golpe@exemplo.com>"))
    }

    @Test
    fun `recognizes official number with formatting`() {
        assertEquals("Banco do Brasil", OfficialContacts.identify("0800 729 0722"))
        assertNull(OfficialContacts.identify("11999998888"))
    }

    @Test
    fun `extracts domain from assorted formats`() {
        assertEquals("bb.com.br", OfficialContacts.extractDomain("Nome <a@bb.com.br>"))
        assertEquals("bb.com.br", OfficialContacts.extractDomain("a@bb.com.br"))
        assertNull(OfficialContacts.extractDomain("sem arroba"))
    }
}
