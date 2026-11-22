package br.com.sentinel.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.data.isOnboardingCompleted

class SessionViewModel : ViewModel() {

    suspend fun startDestination(context: Context): SentinelScreen = when {
        !AuthRepository.isLoggedIn() -> SentinelScreen.WELCOME
        !isOnboardingCompleted(context) -> SentinelScreen.ONBOARDING_PERMISSIONS
        else -> SentinelScreen.HOME
    }

    suspend fun destinationAfterLogin(context: Context): SentinelScreen =
        if (isOnboardingCompleted(context)) SentinelScreen.HOME else SentinelScreen.ONBOARDING_PERMISSIONS

    suspend fun signOut() {
        AuthRepository.signOut()
    }
}
