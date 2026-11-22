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

    suspend fun signUp(name: String, email: String, password: String, consentAt: String): Result<Boolean> =
        runCatching {
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

    fun currentName(): String? = auth.currentUserOrNull()?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull

    fun currentEmail(): String? = auth.currentUserOrNull()?.email

    fun googleErrorMessage(e: Throwable): String? {
        val name = e.javaClass.simpleName
        return when {
            name.contains("Cancellation") -> null
            name.contains("NoCredential") -> "Nenhuma conta Google encontrada neste aparelho."
            else -> errorMessage(e)
        }
    }

    private val errorRules: List<Pair<(String) -> Boolean, String>> = listOf(
        { m: String -> "invalid login credentials" in m } to "E-mail ou senha incorretos.",
        { m: String -> "already registered" in m || "already exists" in m } to "Este e-mail já está cadastrado.",
        { m: String -> "at least" in m && "password" in m } to "A senha precisa ter pelo menos 6 caracteres.",
        { m: String -> "email not confirmed" in m } to
            "Confirme seu e-mail para entrar. Crie a conta de novo para receber outro código.",
        { m: String -> "provider_disabled" in m || "is not enabled" in m } to
            "O login com Google ainda não está ativado no servidor.",
        { m: String -> "otp_expired" in m || "has expired or is invalid" in m } to
            "Código inválido ou vencido. Peça um novo código.",
        { m: String -> "rate limit" in m || "too many" in m || "seconds" in m } to
            "Muitas tentativas. Aguarde um minuto e tente de novo.",
        { m: String -> "invalid" in m && "email" in m } to "E-mail inválido.",
        { m: String -> "unable to resolve" in m || "timeout" in m || "connect" in m } to
            "Não foi possível conectar ao servidor."
    )

    fun errorMessage(e: Throwable): String {
        val msg = e.message?.lowercase() ?: ""
        return errorRules.firstOrNull { it.first(msg) }?.second ?: "Erro ao autenticar. Tente novamente."
    }
}
