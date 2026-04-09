package com.humayapp.scout.feature.auth.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.humayapp.scout.feature.auth.model.ScoutUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureCredentialsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun saveCredentials(email: String, password: String, userId: String?) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString("email", email)
            putString("password", password)
            putString("userId", userId)
        }
    }

    suspend fun getStoredCredentials(): Triple<String?, String?, String?> = withContext(Dispatchers.IO) {
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)
        val userId = sharedPreferences.getString("userId", null)
        Triple(email, password, userId)
    }

    suspend fun clearCredentials() = withContext(Dispatchers.IO) {
        sharedPreferences.edit { clear() }
    }

    suspend fun setRequiresReauth(requires: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putBoolean("requires_reauth", requires) }
    }

    suspend fun getRequiresReauth(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean("requires_reauth", false)
    }

    suspend fun saveUser(user: ScoutUser) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString("user_json", Json.encodeToString(user))
        }
    }

    suspend fun getUser(): ScoutUser? = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString("user_json", null)
        json?.let { Json.decodeFromString<ScoutUser>(it) }
    }

    suspend fun clearUser() = withContext(Dispatchers.IO) {
        sharedPreferences.edit { remove("user_json") }
    }
}
