package com.orienteering.hunt.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orienteering.hunt.data.api.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthManager(private val context: Context) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER = stringPreferencesKey("user_data")
    }
    
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.TOKEN]
    }
    
    val user: Flow<UserDto?> = context.dataStore.data.map { preferences ->
        preferences[Keys.USER]?.let { jsonString ->
            try {
                json.decodeFromString<UserDto>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.TOKEN] != null
    }
    
    suspend fun saveSession(token: String, user: UserDto) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TOKEN] = token
            preferences[Keys.USER] = json.encodeToString(user)
        }
    }
    
    suspend fun getToken(): String? {
        return context.dataStore.data.first()[Keys.TOKEN]
    }
    
    suspend fun getUser(): UserDto? {
        val jsonString = context.dataStore.data.first()[Keys.USER] ?: return null
        return try {
            json.decodeFromString<UserDto>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateUser(user: UserDto) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USER] = json.encodeToString(user)
        }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.TOKEN)
            preferences.remove(Keys.USER)
        }
    }
}
