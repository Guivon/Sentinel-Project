package br.com.sentinel.app.data

import android.util.Log
import br.com.sentinel.app.BuildConfig
import io.github.jan.supabase.auth.auth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.withCharset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Duration
import java.time.OffsetDateTime

object CommunityApi {

    private val baseUrl = BuildConfig.BACKEND_URL.trimEnd('/') + "/api"

    data class RegionalAlert(val title: String, val region: String, val createdAt: String) {
        fun subtitle(): String {
            val age = runCatching {
                val minutes = Duration.between(OffsetDateTime.parse(createdAt), OffsetDateTime.now()).toMinutes()
                when {
                    minutes < 1 -> "Agora"
                    minutes < 60 -> "Há $minutes min"
                    minutes < 24 * 60 -> "Há ${minutes / 60} h"
                    minutes < 48 * 60 -> "Ontem"
                    else -> "Há ${minutes / (24 * 60)} dias"
                }
            }.getOrDefault("")
            return listOf(age, region).filter { it.isNotBlank() }.joinToString(" • ")
        }
    }

    suspend fun fetchAlerts(): List<RegionalAlert>? = withContext(Dispatchers.IO) {
        call("GET", "/alertas", null)?.let { body ->
            runCatching {
                val array = JSONObject(body).getJSONArray("alertas")
                (0 until array.length()).map { index ->
                    val item = array.getJSONObject(index)
                    RegionalAlert(
                        title = item.optString("titulo"),
                        region = item.optString("regiao"),
                        createdAt = item.optString("criado_em")
                    )
                }
            }.getOrNull()
        }
    }

    suspend fun reportScam(description: String, region: String): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("descricao", description)
            put("regiao", region)
        }
        call("POST", "/denuncias", body.toString()) != null
    }

    private suspend fun call(method: String, path: String, body: String?): String? {
        return try {
            var result = request(method, path, body, SentinelAuthToken.current())
            if (result.first == 401) {
                SentinelAuthToken.refresh()
                result = request(method, path, body, SentinelAuthToken.current())
            }
            if (result.first in 200..299) result.second else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("CommunityApi", "Erro em $path: ${e.message}")
            null
        }
    }

    private suspend fun request(method: String, path: String, body: String?, token: String?): Pair<Int, String> {
        val response = HttpClientProvider.client.request(baseUrl + path) {
            this.method = HttpMethod.parse(method)
            if (!token.isNullOrBlank()) bearerAuth(token)
            if (body != null) {
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                setBody(body)
            }
        }
        val status = response.status.value
        return status to if (status in 200..299) response.bodyAsText() else ""
    }
}

private object SentinelAuthToken {
    fun current(): String? = SupabaseProvider.client.auth.currentAccessTokenOrNull()
    suspend fun refresh() {
        runCatching { SupabaseProvider.client.auth.refreshCurrentSession() }
    }
}
