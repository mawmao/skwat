package com.humayapp.scout.feature.auth.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.subtle.Base64
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException


class SessionDataStore(
    private val dataStore: DataStore<Preferences>,
    private val aead: Aead
) {

    private val sessionJson = stringPreferencesKey("session_json")

    val sessionFlow: Flow<UserSession?> = dataStore.data.map { prefs ->
        val encryptedJson = prefs[sessionJson] ?: return@map null
        try {
            val decoded = Base64.decode(encryptedJson, Base64.DEFAULT)
            val decrypted = aead.decrypt(decoded, null)
            Json.decodeFromString<UserSession>(String(decrypted, Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveSession(session: UserSession) {
        Log.v(LOG_TAG, "    Saving session locally. State must change after.")
        val json = Json.encodeToString(session)
        val encrypted = aead.encrypt(json.toByteArray(Charsets.UTF_8), null)
        val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)

        dataStore.edit { it[sessionJson] = encoded }
        Log.v(LOG_TAG, "    Session saved locally.")
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private const val LOG_TAG = "Scout: SessionDataStore"
    }
}

suspend fun <T> ensureSession(
    onSessionExpired: suspend () -> Unit,
    block: suspend () -> T
): T? {
    return try {
        block()
    } catch (e: PostgrestRestException) {
        if (e.code == "PGRST303" || e.message?.contains("JWT expired", ignoreCase = true) == true) {
            Log.w("Scout: Session", "[Auth] JWT expired detected from API")
            withContext(NonCancellable) {
                onSessionExpired()
            }
            return null
        } else {
            throw e
        }
    }
}
