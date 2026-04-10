package com.humayapp.scout.core.data.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlin.time.Instant

class SyncDataStore(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val TASKS_SYNC = stringPreferencesKey("tasks_last_sync")
        val FORMS_SYNC = stringPreferencesKey("forms_last_sync")
    }

    suspend fun getTasksLastSyncTime(): Instant? {
        return dataStore.data.first()[Keys.TASKS_SYNC]?.let { Instant.parse(it) }
    }

    suspend fun setTasksLastSyncTime(time: Instant) {
        dataStore.edit {
            it[Keys.TASKS_SYNC] = time.toString()
        }
    }

    suspend fun getFormsLastSyncTime(): Instant? {
        return dataStore.data.first()[Keys.FORMS_SYNC]?.let { Instant.parse(it) }
    }

    suspend fun setFormsLastSyncTime(time: Instant) {
        dataStore.edit {
            it[Keys.FORMS_SYNC] = time.toString()
        }
    }
}