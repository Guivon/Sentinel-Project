package br.com.sentinel.app.domain

object BehaviorMonitor {

    const val ARM_SCORE = 60
    private const val ARM_WINDOW_MS = 5 * 60_000L
    private const val COOLDOWN_MS = 3 * 60_000L
    private const val TYPING_WINDOW_MS = 5_000L
    private const val TYPING_LIMIT = 12
    private const val SWITCH_WINDOW_MS = 10_000L
    private const val SWITCH_LIMIT = 6
    private const val MAX_EVENTS = 64

    class Report(val summary: String, val signals: List<String>, val armedScore: Int, val fakeLink: Boolean)

    private val lock = Any()
    private val typing = ArrayDeque<Long>()
    private val switches = ArrayDeque<Long>()
    private var armedUntil = 0L
    private var armedScore = 0
    private var lastReport = 0L
    private var fakeBrand: String? = null

    fun arm(score: Int, now: Long = System.currentTimeMillis()) {
        if (score < ARM_SCORE) return
        synchronized(lock) {
            armedUntil = now + ARM_WINDOW_MS
            armedScore = maxOf(armedScore, score)
        }
    }

    fun onTyping(now: Long = System.currentTimeMillis()) = synchronized(lock) { push(typing, now) }

    fun onWindowChange(now: Long = System.currentTimeMillis()) = synchronized(lock) { push(switches, now) }

    fun onScreenText(blocks: List<String>, now: Long = System.currentTimeMillis()) {
        val brand = blocks.firstNotNullOfOrNull { OfficialContacts.lookalikeBrand(it) } ?: return
        synchronized(lock) { if (now < armedUntil) fakeBrand = brand }
    }

    fun check(now: Long = System.currentTimeMillis()): Report? = synchronized(lock) {
        if (now >= armedUntil || now - lastReport < COOLDOWN_MS) return null
        val signals = mutableListOf<String>()
        val typed = typing.count { now - it <= TYPING_WINDOW_MS }
        if (typed >= TYPING_LIMIT) signals += "Digitação muito rápida logo após uma mensagem suspeita"
        val changes = switches.count { now - it <= SWITCH_WINDOW_MS }
        if (changes >= SWITCH_LIMIT) signals += "Troca muito rápida entre telas e abas"
        val brand = fakeBrand
        if (brand != null) signals += "Link que imita o site de $brand aberto na tela"
        if (signals.isEmpty()) return null
        val score = armedScore
        lastReport = now
        fakeBrand = null
        typing.clear()
        switches.clear()
        val summary = "Contexto: mensagem suspeita recebida há pouco (pontuação $score). " +
            "Sinais de comportamento observados: ${signals.joinToString("; ")}. " +
            "Contagens: $typed toques em 5 s, $changes trocas de tela em 10 s."
        Report(summary, signals, score, brand != null)
    }

    private fun push(queue: ArrayDeque<Long>, now: Long) {
        queue.addLast(now)
        while (queue.size > MAX_EVENTS) queue.removeFirst()
    }
}
