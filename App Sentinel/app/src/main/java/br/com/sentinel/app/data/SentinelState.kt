package br.com.sentinel.app.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import br.com.sentinel.app.domain.RiskLevel
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

enum class AlertStatus { CONFIRMED_LOCAL, ANALYZING, CONFIRMED_AI, DISMISSED, OFFICIAL }

data class Alert(
    val id: String = UUID.randomUUID().toString(),
    val app: String,
    val snippet: String,
    val reasons: List<String>,
    val timeLabel: String,
    val status: AlertStatus,
    val level: RiskLevel = RiskLevel.SUSPICIOUS
)

object SentinelState {
    private const val TAG = "SentinelState"

    private val alertsFlow = MutableStateFlow<List<Alert>>(emptyList())
    private val totalAnalyzedFlow = MutableStateFlow(0)
    private val scamsAvoidedFlow = MutableStateFlow(0)

    val alerts: StateFlow<List<Alert>> = alertsFlow.asStateFlow()
    val totalAnalyzed: StateFlow<Int> = totalAnalyzedFlow.asStateFlow()
    val scamsAvoided: StateFlow<Int> = scamsAvoidedFlow.asStateFlow()

    private val lock = Any()
    private var appContext: Context? = null
    private const val PREFS = "sentinel_contadores"
    private const val KEY_ALERTS = "alertas_json"
    private const val MAX_ALERTS = 50
    private const val REPEAT_WINDOW_MS = 6 * 60 * 60 * 1000L
    private const val MAX_SEEN = 300

    private val seen = LinkedHashMap<String, Long>()
    private val sentAlerts = LinkedHashMap<String, Long>()

    fun initialize(context: Context) {
        synchronized(lock) {
            if (appContext != null) return
            val appCtx = context.applicationContext
            appContext = appCtx
            val prefs = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            totalAnalyzedFlow.value = prefs.getInt("total_analisadas", 0)
            scamsAvoidedFlow.value = prefs.getInt("golpes_evitados", 0)
            alertsFlow.value = deserialize(prefs.getString(KEY_ALERTS, null))
        }
    }

    fun recordAnalysis() {
        synchronized(lock) {
            totalAnalyzedFlow.update { it + 1 }
            save()
        }
    }

    fun repeated(content: String): Boolean = synchronized(lock) {
        !markIfNew(seen, hash(content.trim().lowercase()))
    }

    fun canAlert(source: String = "geral", key: String): Boolean = synchronized(lock) {
        markIfNew(sentAlerts, hash("$source:${key.trim().lowercase()}"))
    }

    fun record(
        app: String,
        snippet: String,
        reasons: List<String>,
        status: AlertStatus,
        level: RiskLevel = RiskLevel.SUSPICIOUS
    ): String {
        val formatPattern = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val alert = Alert(
            app = app,
            snippet = snippet.take(150),
            reasons = reasons,
            timeLabel = formatPattern.format(Date()),
            status = status,
            level = level
        )
        synchronized(lock) {
            alertsFlow.update { current -> (listOf(alert) + current).take(MAX_ALERTS) }

            if (status == AlertStatus.CONFIRMED_LOCAL || status == AlertStatus.CONFIRMED_AI) {
                scamsAvoidedFlow.update { it + 1 }
            }
            save()
        }
        return alert.id
    }

    private fun markIfNew(cache: LinkedHashMap<String, Long>, key: String): Boolean {
        val now = System.currentTimeMillis()

        val iteratorValue = cache.entries.iterator()
        while (iteratorValue.hasNext()) {
            if (now - iteratorValue.next().value > REPEAT_WINDOW_MS) iteratorValue.remove() else break
        }

        if (cache.containsKey(key)) return false

        cache[key] = now
        while (cache.size > MAX_SEEN) {
            val first = cache.entries.iterator()
            first.next()
            first.remove()
        }
        return true
    }

    private fun hash(content: String): String = MessageDigest.getInstance("SHA-256")
        .digest(content.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun save() {
        val prefs = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return
        prefs.edit {
            putInt("total_analisadas", totalAnalyzedFlow.value)
            putInt("golpes_evitados", scamsAvoidedFlow.value)
            putString(KEY_ALERTS, serialize())
        }
    }

    private fun serialize(): String {
        val array = JSONArray()
        alertsFlow.value.forEach { alert ->
            array.put(
                JSONObject().apply {
                    put("id", alert.id)
                    put("app", alert.app)
                    put("trecho", alert.snippet)
                    put("motivos", JSONArray(alert.reasons))
                    put("horario", alert.timeLabel)
                    put("status", alert.status.name)
                    put("nivel", alert.level.name)
                }
            )
        }
        return array.toString()
    }

    private fun deserialize(json: String?): List<Alert> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                val reasonsJson = item.optJSONArray("motivos")
                val reasons = (0 until (reasonsJson?.length() ?: 0)).map { reasonsJson!!.getString(it) }
                Alert(
                    id = item.getString("id"),
                    app = item.getString("app"),
                    snippet = item.getString("trecho"),
                    reasons = reasons,
                    timeLabel = item.getString("horario"),
                    status = parseStatus(item.getString("status")),
                    level = parseLevel(item.optString("nivel", RiskLevel.SUSPICIOUS.name))
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível ler o histórico salvo: ${e.message}")
            emptyList()
        }
    }

    private val legacyStatus = mapOf(
        "CONFIRMADO_LOCAL" to AlertStatus.CONFIRMED_LOCAL,
        "ANALISANDO" to AlertStatus.ANALYZING,
        "CONFIRMADO_IA" to AlertStatus.CONFIRMED_AI,
        "DESCARTADO" to AlertStatus.DISMISSED,
        "OFICIAL" to AlertStatus.OFFICIAL
    )
    private val legacyLevel = mapOf(
        "SEGURO" to RiskLevel.SAFE,
        "DUVIDOSO" to RiskLevel.DOUBTFUL,
        "SUSPEITO" to RiskLevel.SUSPICIOUS
    )

    private fun parseStatus(name: String): AlertStatus = legacyStatus[name] ?: AlertStatus.valueOf(name)

    private fun parseLevel(name: String): RiskLevel = legacyLevel[name] ?: RiskLevel.valueOf(name)
}
