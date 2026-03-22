package com.humayapp.scout.core.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsDataStore {
    val AUTO_SYNC_KEY = booleanPreferencesKey("auto_sync")
}

interface SettingsDataSource {
    fun getAutoSync(): Flow<Boolean>
    suspend fun setAutoSync(enabled: Boolean)
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataSourceImpl(context: Context) : SettingsDataSource {
    private val dataStore = context.dataStore

    override fun getAutoSync(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[SettingsDataStore.AUTO_SYNC_KEY] ?: true
        }

    override suspend fun setAutoSync(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsDataStore.AUTO_SYNC_KEY] = enabled
        }
    }
}