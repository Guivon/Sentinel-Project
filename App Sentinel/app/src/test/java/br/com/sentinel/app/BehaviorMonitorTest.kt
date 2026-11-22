package br.com.sentinel.app

import br.com.sentinel.app.domain.BehaviorMonitor
import br.com.sentinel.app.domain.OfficialContacts
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BehaviorMonitorTest {

    @Test
    fun `no report when not armed`() {
        repeat(20) { BehaviorMonitor.onWindowChange(1_000L) }
        assertNull(BehaviorMonitor.check(1_500L))
    }

    @Test
    fun `fast window changes after a suspicious message produce a report`() {
        BehaviorMonitor.arm(80, 10_000_000L)
        repeat(8) { BehaviorMonitor.onWindowChange(10_001_000L + it) }
        assertNotNull(BehaviorMonitor.check(10_002_000L))
    }

    @Test
    fun `lookalike bank domain is detected`() {
        assertNotNull(OfficialContacts.lookalikeBrand("acesse bb-seguro-app.xyz agora"))
        assertNull(OfficialContacts.lookalikeBrand("acesse https://www.bb.com.br"))
    }
}
