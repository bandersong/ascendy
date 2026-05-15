package com.ascendy.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ascendy.app.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore by preferencesDataStore(name = "theme_prefs")
private val THEME_KEY = stringPreferencesKey("variant")
private val ONBOARDED_KEY = booleanPreferencesKey("onboarded")
private val THEMES_INTRO_KEY = booleanPreferencesKey("themes_intro_seen")

class ThemePrefs(private val context: Context) {
    val variant: Flow<ThemeVariant> = context.themeStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            "Kawaii" -> ThemeVariant.Kawaii
            "Tough" -> ThemeVariant.Tough
            else -> ThemeVariant.Neutral
        }
    }

    val onboarded: Flow<Boolean> = context.themeStore.data.map { prefs ->
        prefs[ONBOARDED_KEY] ?: false
    }

    val themesIntroSeen: Flow<Boolean> = context.themeStore.data.map { prefs ->
        prefs[THEMES_INTRO_KEY] ?: false
    }

    suspend fun set(variant: ThemeVariant) {
        context.themeStore.edit { it[THEME_KEY] = variant.name }
    }

    suspend fun markOnboarded() {
        context.themeStore.edit { it[ONBOARDED_KEY] = true }
    }

    suspend fun markThemesIntroSeen() {
        context.themeStore.edit { it[THEMES_INTRO_KEY] = true }
    }
}
