package com.example.myapplication

import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SentinelApi {

    private const val BASE_URL = "https://sentinel-project-eqr2.onrender.com/api/analisar"

    data class RespostaBackend(
        val suspeito: Boolean,
        val confianca: Double,
        val motivos: List<String>,
        val pontuacao: Int = 0,
        val nivel: String = "baixo"
    ) {
        fun nivelApp(): NivelRisco = when (nivel) {
            "critico", "alto" -> NivelRisco.SUSPEITO
            "medio" -> NivelRisco.DUVIDOSO
            else -> NivelRisco.SEGURO
        }
    }

    private class Retorno(val codigo: Int, val corpo: String)

    suspend fun analisar(
        app: String,
        texto: String,
        token: String? = null,
        scoreApp: Int? = null
    ): RespostaBackend? = withContext(Dispatchers.IO) {
        try {
            val auth = SupabaseProvider.client.auth

            var retorno = chamar(app, texto, token ?: auth.currentAccessTokenOrNull(), scoreApp)

            if (retorno.codigo == 401 && token == null) {
                runCatching { auth.refreshCurrentSession() }
                retorno = chamar(app, texto, auth.currentAccessTokenOrNull(), scoreApp)
            }

            if (retorno.codigo == 200) {
                val json = JSONObject(retorno.corpo)
                val motivos = json.optJSONArray("motivos")
                val listaMotivos = mutableListOf<String>()
                if (motivos != null) {
                    for (i in 0 until motivos.length()) listaMotivos.add(motivos.getString(i))
                }
                RespostaBackend(
                    suspeito = json.optBoolean("suspeito", false),
                    confianca = json.optDouble("confianca", 0.0),
                    motivos = listaMotivos,
                    pontuacao = json.optInt("pontuacao", 0),
                    nivel = json.optString("nivel", "baixo")
                )
            } else {
                android.util.Log.w("SentinelApi", "Backend respondeu ${retorno.codigo}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SentinelApi", "Erro ao chamar backend: ${e.message}")
            null
        }
    }

    private fun chamar(app: String, texto: String, token: String?, scoreApp: Int?): Retorno {
        val conexao = URL(BASE_URL).openConnection() as HttpURLConnection
        try {
            conexao.requestMethod = "POST"
            conexao.setRequestProperty("Content-Type", "application/json")
            if (!token.isNullOrBlank()) {
                conexao.setRequestProperty("Authorization", "Bearer $token")
            }
            conexao.doOutput = true
            conexao.connectTimeout = 30_000
            conexao.readTimeout = 60_000

            val corpo = JSONObject().apply {
                put("app", app)
                put("texto", texto)
                if (scoreApp != null) put("score_app", scoreApp.coerceIn(0, 100))
            }
            conexao.outputStream.use { it.write(corpo.toString().toByteArray()) }

            val codigo = conexao.responseCode
            val resposta = if (codigo == 200) conexao.inputStream.bufferedReader().readText() else ""
            return Retorno(codigo, resposta)
        } finally {
            conexao.disconnect()
        }
    }
}