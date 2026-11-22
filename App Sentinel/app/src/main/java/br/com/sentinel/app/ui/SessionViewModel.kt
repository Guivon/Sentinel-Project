package br.com.sentinel.app.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.data.isOnboardingCompleted
import kotlin.coroutines.cancellation.CancellationException

class SessionViewModel : ViewModel() {

    suspend fun startDestination(context: Context): SentinelScreen = try {
        when {
            !AuthRepository.isLoggedIn() -> SentinelScreen.WELCOME
            !isOnboardingCompleted(context) -> SentinelScreen.ONBOARDING_PERMISSIONS
            else -> SentinelScreen.HOME
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("Sentinel", "Falha ao verificar a sessão: ${e.message}", e)
        SentinelScreen.WELCOME
    }

    suspend fun destinationAfterLogin(context: Context): SentinelScreen =
        if (isOnboardingCompleted(context)) SentinelScreen.HOME else SentinelScreen.ONBOARDING_PERMISSIONS

    suspend fun signOut() {
        AuthRepository.signOut()
    }
}
