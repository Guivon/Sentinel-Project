package com.example.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

suspend fun marcarOnboardingConcluido(context: Context) {
    context.dataStore.edit { it[ONBOARDING_DONE] = true }
}

suspend fun isOnboardingConcluido(context: Context): Boolean {
    return context.dataStore.data.first()[ONBOARDING_DONE] ?: false
}