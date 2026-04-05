package com.drmindit.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore instance
val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.themeDataStore

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val THEME_SOURCE_KEY = stringPreferencesKey("theme_source")
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val themeSource: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_SOURCE_KEY] ?: "system"
    }

    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
            preferences[THEME_SOURCE_KEY] = "user"
        }
    }

    suspend fun resetToSystemTheme() {
        dataStore.edit { preferences ->
            preferences[THEME_SOURCE_KEY] = "system"
        }
    }
}
