package com.example.myapplication

import java.text.Normalizer

enum class NivelRisco { SEGURO, DUVIDOSO, SUSPEITO }

object ScamDetector {

    private class Categoria(
        val nome: String,
        palavras: List<String>,
        val peso: Int,
        val forte: Boolean = false
    ) {
        private val padroes = palavras.map { p ->
            val esc = Regex.escape(p)
            Regex(if (p.length <= 3) "\\b$esc\\b" else "\\b$esc")
        }

        fun aparece(texto: String) = padroes.any { it.containsMatchIn(texto) }
    }

    private const val CAT_DINHEIRO = "Pedido de dinheiro/Pix"
    private const val CAT_COERCAO = "Ameaça/coerção"
    private const val CAT_VIOLENCIA = "Ameaça de violência"

    private val categorias = listOf(
        Categoria(CAT_DINHEIRO, listOf("pix", "transferencia", "transferir", "boleto", "cartao", "deposito", "depositar", "dinheiro"), 20),
        Categoria(CAT_COERCAO, listOf("bloquear", "bloqueio", "bloqueada", "cancelar", "cancelado", "suspender", "suspenso", "multa", "perder", "perda", "negativado", "negativar"), 25),
        Categoria(CAT_VIOLENCIA, listOf("matar", "matei", "matou", "te mato", "morrer", "morte", "sequestr", "machucar", "te pegar", "acabar com voce"), 40, forte = true),
        Categoria("Ameaça de crime", listOf("vou roubar", "vou assaltar", "vou invadir", "vou arrombar", "vou espancar", "vou te bater", "vou quebrar"), 30),
        Categoria("Urgência", listOf("urgente", "urgencia", "agora", "imediato", "imediatamente", "hoje", "rapido", "ja", "ultima chance", "nao desligue", "fique na linha"), 15),
        Categoria("Pedido de dados sensíveis", listOf("senha", "codigo", "cpf", "confirme", "confirmar", "digite", "informe", "cartao de credito", "dados bancarios"), 25),
        Categoria("Isca de prêmio", listOf("premio", "ganhou", "sorteio", "sorteado", "parabens"), 20),
        Categoria("Link suspeito", listOf("clique", "acesse o link", "clique aqui", "clique no link"), 20)
    )

    private const val LIMIAR_NOTIFICAR = 30
    private const val LIMIAR_SUSPEITO = 65

    data class Resultado(
        val nivel: NivelRisco,
        val score: Int,
        val motivos: List<String>,
        val sinalForte: Boolean = false
    )

    private fun normalizar(texto: String): String {
        val semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return semAcento.lowercase()
    }

    fun analisar(texto: String?, sinaisExtras: List<Pair<String, Int>> = emptyList()): Resultado {
        if (texto.isNullOrBlank() && sinaisExtras.isEmpty()) {
            return Resultado(NivelRisco.SEGURO, 0, emptyList())
        }

        val textoNormalizado = texto?.let { normalizar(it) } ?: ""

        val encontradas = categorias.filter { it.aparece(textoNormalizado) }
        var scoreTexto = encontradas.sumOf { it.peso }
        val motivosTexto = encontradas.map { it.nome }.toMutableList()

        val temDinheiro = encontradas.any { it.nome == CAT_DINHEIRO }
        val temAmeaca = encontradas.any { it.nome == CAT_COERCAO || it.nome == CAT_VIOLENCIA }
        if (temDinheiro && temAmeaca) {
            scoreTexto += 25
            motivosTexto.add("Dinheiro exigido sob ameaça")
        }

        val scoreTotal = scoreTexto + sinaisExtras.sumOf { it.second }
        val motivos = sinaisExtras.map { it.first } + motivosTexto
        val forte = encontradas.any { it.forte }

        var nivel = when {
            scoreTotal >= LIMIAR_SUSPEITO -> NivelRisco.SUSPEITO
            scoreTotal >= LIMIAR_NOTIFICAR -> NivelRisco.DUVIDOSO
            else -> NivelRisco.SEGURO
        }
        if (forte && nivel == NivelRisco.SEGURO) nivel = NivelRisco.DUVIDOSO

        return Resultado(nivel, scoreTotal.coerceAtMost(100), motivos, forte)
    }
}