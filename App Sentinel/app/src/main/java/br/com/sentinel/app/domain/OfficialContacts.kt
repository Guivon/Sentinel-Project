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
}
