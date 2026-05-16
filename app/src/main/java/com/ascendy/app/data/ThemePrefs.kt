package com.ascendy.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ascendy.app.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore by preferencesDataStore(name = "theme_prefs")
private val THEME_KEY = stringPreferencesKey("variant")
private val ONBOARDED_KEY = booleanPreferencesKey("onboarded")
private val THEMES_INTRO_KEY = booleanPreferencesKey("themes_intro_seen")
private val MAX_SESSION_KEY = intPreferencesKey("max_session_minutes")
const val MAX_SESSION_DEFAULT_MIN = 480   // 8h

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

    val maxSessionMinutes: Flow<Int> = context.themeStore.data.map { prefs ->
        prefs[MAX_SESSION_KEY] ?: MAX_SESSION_DEFAULT_MIN
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

    suspend fun setMaxSessionMinutes(min: Int) {
        context.themeStore.edit { it[MAX_SESSION_KEY] = min.coerceIn(60, 24 * 60) }
    }
}
