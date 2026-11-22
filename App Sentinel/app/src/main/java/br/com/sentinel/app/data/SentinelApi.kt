package br.com.sentinel.app.data

import android.util.Log
import br.com.sentinel.app.BuildConfig
import br.com.sentinel.app.domain.RiskLevel
import io.github.jan.supabase.auth.auth
import io.ktor.client.plugins.timeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.withCharset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object SentinelApi {

    private val analysisUrl = BuildConfig.BACKEND_URL.trimEnd('/') + "/api/analisar"

    data class BackendResponse(
        val suspicious: Boolean,
        val confidence: Double,
        val reasons: List<String>,
        val aiScore: Int = 0,
        val level: String = "baixo",
        val scamType: String = "",
        val recommendation: String = ""
    ) {
        fun appLevel(): RiskLevel = when (level) {
            "critico", "alto" -> RiskLevel.SUSPICIOUS
            "medio" -> RiskLevel.DOUBTFUL
            else -> RiskLevel.SAFE
        }
    }

    class UploadFile(val name: String, val bytes: ByteArray)

    private class HttpResult(val statusCode: Int, val body: String)

    suspend fun analyze(
        app: String,
        content: String,
        token: String? = null,
        appScore: Int? = null,
        timeoutMs: Int = 60_000,
        file: UploadFile? = null
    ): BackendResponse? = withContext(Dispatchers.IO) {
        try {
            val auth = SupabaseProvider.client.auth

            var httpResult = callBackend(
                app,
                content,
                token ?: auth.currentAccessTokenOrNull(),
                appScore,
                timeoutMs,
                file
            )

            if (httpResult.statusCode == 401 && token == null) {
                runCatching { auth.refreshCurrentSession() }
                httpResult = callBackend(app, content, auth.currentAccessTokenOrNull(), appScore, timeoutMs, file)
            }

            if (httpResult.statusCode == 200) {
                parseResponse(JSONObject(httpResult.body))
            } else {
                Log.w("SentinelApi", "Backend respondeu ${httpResult.statusCode}")
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SentinelApi", "Erro ao chamar backend: ${e.message}")
            null
        }
    }

    private fun parseResponse(json: JSONObject): BackendResponse {
        val reasons = json.optJSONArray("motivos")
        val reasonsList = mutableListOf<String>()
        if (reasons != null) {
            for (index in 0 until reasons.length()) reasonsList.add(reasons.getString(index))
        }
        val suspicious = json.optBoolean("suspeito", false)
        val confidence = json.optDouble("confianca", 0.0)

        val level = if (json.has("nivel")) {
            json.optString("nivel", "baixo")
        } else if (suspicious) {
            "alto"
        } else {
            "baixo"
        }
        val aiScore = if (json.has("pontuacao")) json.optInt("pontuacao", 0) else (confidence * 100).toInt()
        return BackendResponse(
            suspicious = suspicious,
            confidence = confidence,
            reasons = reasonsList,
            aiScore = aiScore,
            level = level,
            scamType = json.optString("tipo_de_golpe", ""),
            recommendation = json.optString("recomendacao", "")
        )
    }

    private suspend fun callBackend(
        app: String,
        content: String,
        token: String?,
        appScore: Int?,
        timeoutMs: Int,
        file: UploadFile?
    ): HttpResult {
        val response = HttpClientProvider.client.post(analysisUrl) {
            if (!token.isNullOrBlank()) bearerAuth(token)
            timeout {
                connectTimeoutMillis = minOf(timeoutMs, MAX_CONNECT_MS).toLong()
                requestTimeoutMillis = timeoutMs.toLong()
                socketTimeoutMillis = timeoutMs.toLong()
            }
            val score = appScore?.coerceIn(0, 100)
            if (file == null) {
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                val body = JSONObject().apply {
                    put("app", app)
                    put("texto", content)
                    if (score != null) put("pontuacao_app", score)
                }
                setBody(body.toString())
            } else {
                val safeName = file.name.replace("\"", "").replace("\r", "").replace("\n", "")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("app", app)
                            append("texto", content)
                            if (score != null) append("pontuacao_app", score.toString())
                            append(
                                "arquivo",
                                file.bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "application/octet-stream")
                                    append(HttpHeaders.ContentDisposition, "filename=\"$safeName\"")
                                }
                            )
                        }
                    )
                )
            }
        }
        val statusCode = response.status.value
        return HttpResult(statusCode, if (statusCode == HTTP_OK) response.bodyAsText() else "")
    }

    private const val MAX_CONNECT_MS = 30_000
    private const val HTTP_OK = 200
}
