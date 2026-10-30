package com.example.myapplication

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class StatusAlerta { CONFIRMADO_LOCAL, ANALISANDO, CONFIRMADO_IA, DESCARTADO, OFICIAL }

data class Alerta(
    val id: String = UUID.randomUUID().toString(),
    val app: String,
    val trecho: String,
    var motivos: List<String>,
    val horario: String,
    var status: StatusAlerta,
    val nivel: NivelRisco = NivelRisco.SUSPEITO
)

object SentinelState {
    val alertas = mutableStateListOf<Alerta>()

    var totalAnalisadas by mutableStateOf(0)
        private set

    var golpesEvitados by mutableStateOf(0)
        private set

    private var appContext: Context? = null
    private const val PREFS = "sentinel_contadores"
    private const val CHAVE_ALERTAS = "alertas_json"
    private const val MAX_ALERTAS = 50
    private const val JANELA_REPETIDO_MS = 6 * 60 * 60 * 1000L
    private const val MAX_VISTOS = 300

    private val vistos = LinkedHashMap<Int, Long>()
    private val alertasEnviados = LinkedHashMap<Int, Long>()

    fun inicializar(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        totalAnalisadas = prefs.getInt("total_analisadas", 0)
        golpesEvitados = prefs.getInt("golpes_evitados", 0)
        alertas.clear()
        alertas.addAll(desserializar(prefs.getString(CHAVE_ALERTAS, null)))
    }

    fun registrarAnalise() {
        totalAnalisadas += 1
        salvar()
    }

    @Synchronized
    fun repetido(texto: String): Boolean {
        val chave = texto.trim().lowercase().hashCode()
        val agora = System.currentTimeMillis()

        val iterador = vistos.entries.iterator()
        while (iterador.hasNext()) {
            if (agora - iterador.next().value > JANELA_REPETIDO_MS) iterador.remove() else break
        }

        if (vistos.containsKey(chave)) return true

        vistos[chave] = agora
        while (vistos.size > MAX_VISTOS) {
            val primeiro = vistos.entries.iterator()
            primeiro.next()
            primeiro.remove()
        }
        return false
    }

    @Synchronized
    fun podeAlertar(origem: String = "geral", chave: String): Boolean {
        val idChave = "$origem:${chave.trim().lowercase()}".hashCode()
        val agora = System.currentTimeMillis()

        val iterador = alertasEnviados.entries.iterator()
        while (iterador.hasNext()) {
            if (agora - iterador.next().value > JANELA_REPETIDO_MS) iterador.remove() else break
        }

        if (alertasEnviados.containsKey(idChave)) return false

        alertasEnviados[idChave] = agora
        while (alertasEnviados.size > MAX_VISTOS) {
            val primeiro = alertasEnviados.entries.iterator()
            primeiro.next()
            primeiro.remove()
        }
        return true
    }

    fun registrar(
        app: String,
        trecho: String,
        motivos: List<String>,
        status: StatusAlerta,
        nivel: NivelRisco = NivelRisco.SUSPEITO
    ): String {
        val formato = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val alerta = Alerta(
            app = app,
            trecho = trecho.take(150),
            motivos = motivos,
            horario = formato.format(Date()),
            status = status,
            nivel = nivel
        )
        alertas.add(0, alerta)
        if (alertas.size > MAX_ALERTAS) alertas.removeAt(alertas.size - 1)

        if (status == StatusAlerta.CONFIRMADO_LOCAL || status == StatusAlerta.CONFIRMADO_IA) {
            golpesEvitados += 1
        }
        salvar()

        return alerta.id
    }

    private fun salvar() {
        val prefs = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return
        prefs.edit {
            putInt("total_analisadas", totalAnalisadas)
            putInt("golpes_evitados", golpesEvitados)
            putString(CHAVE_ALERTAS, serializar())
        }
    }

    private fun serializar(): String {
        val array = JSONArray()
        alertas.forEach { a ->
            array.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("app", a.app)
                    put("trecho", a.trecho)
                    put("motivos", JSONArray(a.motivos))
                    put("horario", a.horario)
                    put("status", a.status.name)
                    put("nivel", a.nivel.name)
                }
            )
        }
        return array.toString()
    }

    private fun desserializar(json: String?): List<Alerta> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                val motivosJson = o.optJSONArray("motivos")
                val motivos = (0 until (motivosJson?.length() ?: 0)).map { motivosJson!!.getString(it) }
                Alerta(
                    id = o.getString("id"),
                    app = o.getString("app"),
                    trecho = o.getString("trecho"),
                    motivos = motivos,
                    horario = o.getString("horario"),
                    status = StatusAlerta.valueOf(o.getString("status")),
                    nivel = NivelRisco.valueOf(o.optString("nivel", NivelRisco.SUSPEITO.name))
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}