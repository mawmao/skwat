package com.humayapp.scout.feature.auth.data

import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.humayapp.scout.core.util.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock

@Serializable
data class OfflineUser(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,

    @SerialName("user_metadata")
    val userMetadata: JsonObject? = null,
)

class OfflineAuthDataStore(
    private val dataStore: DataStore<Preferences>,
    private val aead: Aead
) {

    private val passwordHashKey = stringPreferencesKey("offline_password_hash")
    private val passwordSaltKey = stringPreferencesKey("offline_password_salt")
    private val lastOnlineLoginKey = longPreferencesKey("last_online_login")
    private val lastUserIdKey = stringPreferencesKey("offline_user_id")
    private val offlineUserKey = stringPreferencesKey("offline_user_json")

    val hasOfflineCredentials: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[passwordHashKey] != null && prefs[lastUserIdKey] != null
    }

    val offlineUserFlow: Flow<OfflineUser?> = dataStore.data.map { prefs ->
        val encoded = prefs[offlineUserKey] ?: return@map null
        try {
            val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
            val decryptedBytes = aead.decrypt(decodedBytes, null)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            Json.decodeFromString<OfflineUser>(jsonString)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Auth] Error decrypting/parsing offline user flow", e)
            null
        }
    }

    suspend fun saveOfflineUser(user: OfflineUser) {
        Log.v(LOG_TAG, "    Saving offline user with ID: ${user.id}")
        val json = Json.encodeToString(user)
        val encrypted = aead.encrypt(json.toByteArray(Charsets.UTF_8), null)
        val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)

        dataStore.edit { prefs ->
            prefs[offlineUserKey] = encoded
        }
        Log.v(LOG_TAG, "    Offline user saved with ID: ${user.id}")
    }

    suspend fun saveOfflineCredentials(userId: String, password: String) {
        Log.v(LOG_TAG, "    Saving offline credentials for user: $userId")
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(password, salt)

        val encryptedHash = aead.encrypt(hash.toByteArray(Charsets.UTF_8), null)
        val encodedHash = Base64.encodeToString(encryptedHash, Base64.DEFAULT)
        val encodedSalt = Base64.encodeToString(salt, Base64.DEFAULT)

        dataStore.edit { prefs ->
            prefs[passwordHashKey] = encodedHash
            prefs[passwordSaltKey] = encodedSalt
            prefs[lastOnlineLoginKey] = Clock.System.now().toEpochMilliseconds()
            prefs[lastUserIdKey] = userId
        }
        Log.v(LOG_TAG, "    Offline credentials saved for user: $userId")
    }

    suspend fun verifyPassword(userId: String, password: String): Boolean {
        val prefs = dataStore.data.first()
        val encodedHash = prefs[passwordHashKey] ?: return false
        val encodedSalt = prefs[passwordSaltKey] ?: return false
        val storedUserId = prefs[lastUserIdKey] ?: return false

        if (storedUserId != userId) return false // enforce last-user-only rule

        return try {
            val decryptedHashBytes = aead.decrypt(Base64.decode(encodedHash, Base64.DEFAULT), null)
            val storedHashString = String(decryptedHashBytes, Charsets.UTF_8)
            val saltBytes = Base64.decode(encodedSalt, Base64.DEFAULT)

            PasswordHasher.verify(password, saltBytes, storedHashString)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Auth] Decrypting locally stored credentials error.", e)
            false
        }
    }

    // could use when implementing global reset like resetting after 30 days and the user must relogin online
    suspend fun clearCredentials() {
        dataStore.edit { prefs ->
            prefs.remove(passwordHashKey)
            prefs.remove(passwordSaltKey)
            prefs.remove(lastOnlineLoginKey)
            prefs.remove(lastUserIdKey)
            prefs.remove(offlineUserKey)
        }
    }


    suspend fun getOfflineUser(): OfflineUser? {
        val prefs = dataStore.data.first()
        val encoded = prefs[offlineUserKey] ?: return null

        return try {
            val decrypted = aead.decrypt(Base64.decode(encoded, Base64.DEFAULT), null)
            Json.decodeFromString<OfflineUser>(String(decrypted, Charsets.UTF_8))
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Auth] Failed to decode offline user", e)
            null
        }
    }

    suspend fun getLastUserId(): String? {
        val prefs = dataStore.data.first()
        return prefs[lastUserIdKey]
    }

    companion object {
        private const val LOG_TAG = "Scout: OfflineAuthDataStore"
    }
}
