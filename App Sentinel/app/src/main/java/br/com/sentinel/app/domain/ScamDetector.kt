package br.com.sentinel.app.domain

import java.text.Normalizer

enum class RiskLevel { SAFE, DOUBTFUL, SUSPICIOUS }

object ScamDetector {

    private class Category(val name: String, words: List<String>, val weightValue: Int, val strong: Boolean = false) {
        private val patterns = words.map { word ->
            val escaped = Regex.escape(word)
            Regex(if (word.length <= 3) "\\b$escaped\\b" else "\\b$escaped")
        }

        fun appears(content: String) = patterns.any { it.containsMatchIn(content) }
    }

    private const val CAT_MONEY = "Pedido de dinheiro/Pix"
    private const val CAT_COERCION = "Ameaça/coerção"
    private const val CAT_VIOLENCE = "Ameaça de violência"

    private val categories = listOf(
        Category(
            CAT_MONEY,
            listOf("pix", "transferencia", "transferir", "boleto", "cartao", "deposito", "depositar", "dinheiro"),
            20
        ),
        Category(
            CAT_COERCION,
            listOf(
                "bloquear",
                "bloqueio",
                "bloqueada",
                "cancelar",
                "cancelado",
                "suspender",
                "suspenso",
                "multa",
                "perder",
                "perda",
                "negativado",
                "negativar"
            ),
            25
        ),
        Category(
            CAT_VIOLENCE,
            listOf(
                "matar",
                "matei",
                "matou",
                "te mato",
                "morrer",
                "morte",
                "sequestr",
                "machucar",
                "te pegar",
                "acabar com voce"
            ),
            40,
            strong = true
        ),
        Category(
            "Ameaça de crime",
            listOf(
                "vou roubar",
                "vou assaltar",
                "vou invadir",
                "vou arrombar",
                "vou espancar",
                "vou te bater",
                "vou quebrar"
            ),
            30
        ),
        Category(
            "Urgência",
            listOf(
                "urgente",
                "urgencia",
                "agora",
                "imediato",
                "imediatamente",
                "agora mesmo",
                "hoje mesmo",
                "ainda hoje",
                "prazo final",
                "rapido",
                "ultima chance",
                "nao desligue",
                "fique na linha"
            ),
            15
        ),
        Category(
            "Pedido de dados sensíveis",
            listOf(
                "senha",
                "codigo",
                "cpf",
                "confirme",
                "confirmar",
                "digite",
                "informe",
                "cartao de credito",
                "dados bancarios"
            ),
            25
        ),
        Category("Isca de prêmio", listOf("premio", "ganhou", "sorteio", "sorteado", "parabens"), 20),
        Category("Link suspeito", listOf("clique", "acesse o link", "clique aqui", "clique no link"), 20)
    )

    private const val NOTIFY_THRESHOLD = 30
    private const val SUSPICIOUS_THRESHOLD = 65
    private const val COMBO_BONUS = 20
    private val DIMINISHING_FACTORS = doubleArrayOf(1.0, 0.6, 0.4, 0.25)

    data class DetectionResult(
        val level: RiskLevel,
        val score: Int,
        val reasons: List<String>,
        val strongSignal: Boolean = false,
        val asksPayment: Boolean = false
    )

    private fun normalizeText(content: String): String {
        val withoutAccents = Normalizer.normalize(content, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutAccents.lowercase()
    }

    fun analyze(content: String?, extraSigns: List<Pair<String, Int>> = emptyList()): DetectionResult {
        if (content.isNullOrBlank() && extraSigns.isEmpty()) {
            return DetectionResult(RiskLevel.SAFE, 0, emptyList())
        }

        val normalizedContent = content?.let { normalizeText(it) } ?: ""

        val found = categories.filter { it.appears(normalizedContent) }
        var scoreText = diminishingSum(found.map { it.weightValue })
        val reasonsText = found.map { it.name }.toMutableList()

        val hasMoney = found.any { it.name == CAT_MONEY }
        val hasThreat = found.any { it.name == CAT_COERCION || it.name == CAT_VIOLENCE }
        if (hasMoney && hasThreat) {
            scoreText += COMBO_BONUS
            reasonsText.add("Dinheiro exigido sob ameaça")
        }

        val totalScore = scoreText + extraSigns.sumOf { it.second }
        val reasons = extraSigns.map { it.first } + reasonsText
        val strong = found.any { it.strong }

        var level = when {
            totalScore >= SUSPICIOUS_THRESHOLD -> RiskLevel.SUSPICIOUS
            totalScore >= NOTIFY_THRESHOLD -> RiskLevel.DOUBTFUL
            else -> RiskLevel.SAFE
        }
        if (strong && level == RiskLevel.SAFE) level = RiskLevel.DOUBTFUL

        return DetectionResult(level, totalScore.coerceAtMost(100), reasons, strong, hasMoney)
    }

    private fun diminishingSum(weights: List<Int>): Int = weights
        .sortedDescending()
        .withIndex()
        .sumOf { (index, weight) -> weight * DIMINISHING_FACTORS[minOf(index, DIMINISHING_FACTORS.lastIndex)] }
        .toInt()
}
