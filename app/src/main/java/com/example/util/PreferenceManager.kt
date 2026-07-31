package com.example.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MASTER_PASSWORD = stringPreferencesKey("master_password")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_IMAGE_URI = stringPreferencesKey("user_image_uri")
        val DEVELOPER_MODE_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("developer_mode_enabled")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "auto"
    }

    val masterPassword: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MASTER_PASSWORD] ?: "1234"
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME] ?: "Guest User"
    }

    val userImageUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_IMAGE_URI]
    }

    val developerModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DEVELOPER_MODE_ENABLED] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setMasterPassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[MASTER_PASSWORD] = password
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    suspend fun setUserImageUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(USER_IMAGE_URI)
            } else {
                preferences[USER_IMAGE_URI] = uri
            }
        }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEVELOPER_MODE_ENABLED] = enabled
        }
    }
}
