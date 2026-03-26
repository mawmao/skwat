package com.humayapp.scout.feature.auth.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private companion object {
        val LAST_EMAIL_KEY = stringPreferencesKey("last_used_email")
    }

    fun getLastUsedEmail(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[LAST_EMAIL_KEY]
    }

    suspend fun saveLastUsedEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[LAST_EMAIL_KEY] = email
        }
    }
}
