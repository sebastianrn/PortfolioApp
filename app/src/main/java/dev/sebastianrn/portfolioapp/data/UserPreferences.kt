package dev.sebastianrn.portfolioapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create the DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    // Key
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

    // Get the Flow (Default to True/Dark)
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: true
        }

    // Save the selection
    suspend fun toggleTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }
}