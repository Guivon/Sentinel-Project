package com.example.myapplication

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object AuthRepository {
    private val auth get() = SupabaseProvider.client.auth

    suspend fun cadastrar(nome: String, email: String, senha: String): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = senha
            data = buildJsonObject { put("name", nome) }
        }
        if (auth.currentSessionOrNull() == null) {
            error("Confirme seu e-mail para ativar a conta.")
        }
    }

    suspend fun entrar(email: String, senha: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = senha
        }
    }

    suspend fun sair(): Result<Unit> = runCatching { auth.signOut() }

    suspend fun estaLogado(): Boolean {
        val status = auth.sessionStatus.first { it !is SessionStatus.Initializing }
        return status is SessionStatus.Authenticated
    }

    fun nomeAtual(): String? =
        auth.currentUserOrNull()?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull

    fun emailAtual(): String? = auth.currentUserOrNull()?.email

    fun mensagemErro(e: Throwable): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            "invalid login credentials" in msg -> "E-mail ou senha incorretos."
            "already registered" in msg || "already exists" in msg -> "Este e-mail já está cadastrado."
            "at least" in msg && "password" in msg -> "A senha precisa ter pelo menos 6 caracteres."
            "confirme seu e-mail" in msg -> "Confirme seu e-mail para ativar a conta."
            "invalid" in msg && "email" in msg -> "E-mail inválido."
            "unable to resolve" in msg || "timeout" in msg || "connect" in msg ->
                "Não foi possível conectar ao servidor."
            else -> "Erro ao autenticar. Tente novamente."
        }
    }
}