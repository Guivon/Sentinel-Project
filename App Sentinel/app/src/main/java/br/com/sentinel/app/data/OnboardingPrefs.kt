package br.com.sentinel.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

suspend fun markOnboardingCompleted(context: Context) {
    context.dataStore.edit { it[ONBOARDING_DONE] = true }
}

suspend fun isOnboardingCompleted(context: Context): Boolean = context.dataStore.data.first()[ONBOARDING_DONE] ?: false
