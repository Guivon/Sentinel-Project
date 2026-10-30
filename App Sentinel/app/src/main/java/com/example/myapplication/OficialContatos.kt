package com.example.myapplication

object OficialContatos {

    private val numerosOficiais = mapOf(
        "08007290722" to "Banco do Brasil",
        "40040001" to "Banco do Brasil",
        "1058" to "Claro",
        "1057" to "TIM"
    )

    private val dominiosOficiais = mapOf(
        "bb.com.br" to "Banco do Brasil",
        "vivo.com.br" to "Vivo",
        "claro.com.br" to "Claro",
        "tim.com.br" to "TIM"
    )

    fun identificar(remetente: String): String? {
        val limpo = remetente.trim().lowercase()

        if (limpo.contains("@")) {
            val dominio = limpo.substringAfter("@")
            for ((chave, instituicao) in dominiosOficiais) {
                if (dominio == chave || dominio.endsWith(".$chave")) return instituicao
            }
            return null
        }

        val soDigitos = limpo.filter { it.isDigit() }
        return numerosOficiais[soDigitos]
    }
}