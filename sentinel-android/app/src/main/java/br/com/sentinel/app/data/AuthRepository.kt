package br.com.sentinel.app.data

import br.com.sentinel.app.ui.screens.CONSENT_VERSION
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object AuthRepository {
    private val auth get() = SupabaseProvider.client.auth

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        consentAt: String
    ): Result<Boolean> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("name", name)
                put("consent_version", CONSENT_VERSION)
                put("consent_at", consentAt)
            }
        }
        auth.currentSessionOrNull() != null
    }

    suspend fun verifySignupCode(email: String, code: String): Result<Unit> = runCatching {
        auth.verifyEmailOtp(type = OtpType.Email.SIGNUP, email = email, token = code)
    }

    suspend fun resendSignupCode(email: String): Result<Unit> = runCatching {
        auth.resendEmail(type = OtpType.Email.SIGNUP, email = email)
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signInWithGoogle(googleIdToken: String, rawNonce: String): Result<Unit> = runCatching {
        auth.signInWith(IDToken) {
            idToken = googleIdToken
            provider = Google
            nonce = rawNonce
        }
    }

    suspend fun saveConsent(consentAt: String): Result<Unit> = runCatching {
        auth.updateUser {
            data = buildJsonObject {
                put("consent_version", CONSENT_VERSION)
                put("consent_at", consentAt)
            }
        }
    }

    fun currentUserId(): String? = auth.currentUserOrNull()?.id

    fun currentAvatarUrl(): String? {
        val metadata = auth.currentUserOrNull()?.userMetadata ?: return null
        return (metadata["avatar_url"] ?: metadata["picture"])?.jsonPrimitive?.contentOrNull
    }

    suspend fun signOut(): Result<Unit> = runCatching { auth.signOut() }

    suspend fun isLoggedIn(): Boolean {
        val status = auth.sessionStatus.first { it !is SessionStatus.Initializing }
        return status is SessionStatus.Authenticated
    }

    fun currentName(): String? =
        auth.currentUserOrNull()?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull

    fun currentEmail(): String? = auth.currentUserOrNull()?.email

    fun googleErrorMessage(e: Throwable): String? {
        val name = e.javaClass.simpleName
        return when {
            name.contains("Cancellation") -> null
            name.contains("NoCredential") -> "Nenhuma conta Google encontrada neste aparelho."
            else -> errorMessage(e)
        }
    }

    fun errorMessage(e: Throwable): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            "invalid login credentials" in msg -> "E-mail ou senha incorretos."
            "already registered" in msg || "already exists" in msg -> "Este e-mail já está cadastrado."
            "at least" in msg && "password" in msg -> "A senha precisa ter pelo menos 6 caracteres."
            "email not confirmed" in msg ->
                "Confirme seu e-mail para entrar. Crie a conta de novo para receber outro código."
            "provider_disabled" in msg || "is not enabled" in msg ->
                "O login com Google ainda não está ativado no servidor."
            "otp_expired" in msg || "has expired or is invalid" in msg ->
                "Código inválido ou vencido. Peça um novo código."
            "rate limit" in msg || "too many" in msg || "seconds" in msg ->
                "Muitas tentativas. Aguarde um minuto e tente de novo."
            "invalid" in msg && "email" in msg -> "E-mail inválido."
            "unable to resolve" in msg || "timeout" in msg || "connect" in msg ->
                "Não foi possível conectar ao servidor."
            else -> "Erro ao autenticar. Tente novamente."
        }
    }
}
