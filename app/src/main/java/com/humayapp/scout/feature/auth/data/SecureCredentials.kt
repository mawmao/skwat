package com.humayapp.scout.feature.auth.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

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
}
