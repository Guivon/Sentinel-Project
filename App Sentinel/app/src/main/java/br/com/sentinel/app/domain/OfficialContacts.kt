package br.com.sentinel.app.domain

object OfficialContacts {

    private val officialNumbers = mapOf(
        "08007290722" to "Banco do Brasil",
        "40040001" to "Banco do Brasil",
        "1058" to "Claro",
        "1057" to "TIM"
    )

    private val officialDomains = mapOf(
        "bb.com.br" to "Banco do Brasil",
        "vivo.com.br" to "Vivo",
        "claro.com.br" to "Claro",
        "tim.com.br" to "TIM"
    )

    private val brandDomains = mapOf(
        "bancodobrasil" to ("Banco do Brasil" to listOf("bb.com.br")),
        "itau" to ("Itaú" to listOf("itau.com.br", "itau.com")),
        "bradesco" to ("Bradesco" to listOf("bradesco.com.br")),
        "nubank" to ("Nubank" to listOf("nubank.com.br")),
        "santander" to ("Santander" to listOf("santander.com.br")),
        "sicoob" to ("Sicoob" to listOf("sicoob.com.br")),
        "sicredi" to ("Sicredi" to listOf("sicredi.com.br")),
        "picpay" to ("PicPay" to listOf("picpay.com")),
        "mercadopago" to ("Mercado Pago" to listOf("mercadopago.com.br", "mercadopago.com")),
        "caixa" to ("Caixa" to listOf("caixa.gov.br")),
        "bb" to ("Banco do Brasil" to listOf("bb.com.br"))
    )

    private val domainRegex = Regex("(?:https?://)?((?:[a-z0-9-]+\\.)+[a-z]{2,})", RegexOption.IGNORE_CASE)

    private val angleBracketAddressRegex = Regex("<([^<>]+)>")

    fun identify(sender: String): String? {
        val cleaned = sender.trim().lowercase()

        if (cleaned.contains("@")) {
            val domain = extractDomain(cleaned) ?: return null
            for ((key, institution) in officialDomains) {
                if (domain == key || domain.endsWith(".$key")) return institution
            }
            return null
        }

        val onlyDigits = cleaned.filter { it.isDigit() }
        return officialNumbers[onlyDigits]
    }

    internal fun extractDomain(sender: String): String? {
        val betweenAngles = angleBracketAddressRegex.find(sender)?.groupValues?.get(1)
        val address = (betweenAngles ?: sender).trim()
        if (!address.contains("@")) return null
        val domain = address.substringAfterLast("@").trim().trimEnd('>', '.', ' ')
        return domain.ifEmpty { null }
    }

    fun lookalikeBrand(text: String): String? {
        for (match in domainRegex.findAll(text)) {
            val domain = match.groupValues[1].lowercase()
            for ((token, entry) in brandDomains) {
                val (brand, official) = entry
                val hasToken = if (token == "bb") {
                    domain.split('.', '-').contains("bb")
                } else {
                    domain.contains(token)
                }
                val isOfficial = official.any { domain == it || domain.endsWith(".$it") }
                if (hasToken && !isOfficial) return brand
            }
        }
        return null
    }

    fun lookalikeSigns(text: String): List<Pair<String, Int>> {
        val brand = lookalikeBrand(text) ?: return emptyList()
        return listOf("Link imita o site de $brand" to LOOKALIKE_WEIGHT)
    }

    private const val LOOKALIKE_WEIGHT = 30
}
