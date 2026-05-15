package com.ascendy.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ascendy.app.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore by preferencesDataStore(name = "theme_prefs")
private val THEME_KEY = stringPreferencesKey("variant")

class ThemePrefs(private val context: Context) {
    val variant: Flow<ThemeVariant> = context.themeStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            "Tough" -> ThemeVariant.Tough
            "Neutral" -> ThemeVariant.Neutral
            else -> ThemeVariant.Kawaii
        }
    }

    suspend fun set(variant: ThemeVariant) {
        context.themeStore.edit { it[THEME_KEY] = variant.name }
    }
}
