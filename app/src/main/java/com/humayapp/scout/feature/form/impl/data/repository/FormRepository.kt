package com.humayapp.scout.feature.form.impl.data.repository

import android.util.Log
import com.humayapp.scout.core.database.dao.FormEntryDao
import com.humayapp.scout.core.database.model.FormEntryEntity
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface FormRepository {

    val syncEvents: SharedFlow<String>

    fun getAllEntries(): Flow<List<FormEntryEntity>>

    suspend fun saveFormEntry(entry: FormEntryEntity): Long
    suspend fun getPendingSyncOnce(): List<FormEntryEntity>

    suspend fun markAsSynced(id: Int): Unit

    // NOTE: development only
    suspend fun clearDatabase()
}

class FormRepositoryImpl @Inject constructor(
    private val formEntryDao: FormEntryDao
) : FormRepository {

    private val _syncEvents = MutableSharedFlow<String>()
    override val syncEvents = _syncEvents.asSharedFlow()

    override fun getAllEntries(): Flow<List<FormEntryEntity>> {
        Log.d(TAG, "getAllEntries()")
        return formEntryDao.getAll()
    }

    override suspend fun saveFormEntry(entry: FormEntryEntity): Long {
        Log.d(TAG, "saveFormEntry(): $entry")
        return formEntryDao.insert(entry)
    }

    override suspend fun getPendingSyncOnce(): List<FormEntryEntity> {
        Log.d(TAG, "getPendingSyncOnce()")
        return formEntryDao.getPendingSyncOnce()
    }

    override suspend fun markAsSynced(id: Int) {
        Log.d(TAG, "markAsSynced(): id=$id")
        formEntryDao.markAsSynced(id)
        _syncEvents.emit("Form synced successfully")
        Log.d(TAG, "Sync event emitted for id=$id")
    }

    override suspend fun clearDatabase() {
        Log.w(TAG, "clearDatabase() called")
        formEntryDao.clearAll()
    }

    companion object {
        private const val TAG = "Scout: FormRepository"
    }
}
