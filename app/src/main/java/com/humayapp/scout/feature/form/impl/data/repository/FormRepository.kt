package com.humayapp.scout.feature.form.impl.data.repository

import android.content.Context
import android.util.Log
import com.humayapp.scout.core.common.dispatcher.Dispatcher
import com.humayapp.scout.core.common.dispatcher.ScoutDispatchers
import com.humayapp.scout.core.database.dao.FormEntryDao
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.SyncStatus
import com.humayapp.scout.core.system.saveImagesToFolder
import com.humayapp.scout.feature.form.impl.model.toFormImages
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.time.Instant

interface FormRepository {

    val syncEvents: SharedFlow<String>

    fun getAllEntries(): Flow<List<FormEntryEntity>>

    suspend fun getEntryByCollectionTaskId(collectionTaskId: Int): FormEntryEntity?

    suspend fun getEntryById(id: Long): FormEntryEntity
    fun getEntryByIdFlow(id: Long): Flow<FormEntryEntity>

    suspend fun getPendingSyncOnce(): List<FormEntryEntity>
    suspend fun getPendingSyncOnceForUser(userId: String): List<FormEntryEntity>
    suspend fun markAsSyncedWithStatus(id: Long, timestamp: Instant, status: SyncStatus)

//    suspend fun getImagesOfEntryById(formId: Long): List<FormImageEntity>
//    fun getImagesOfEntryByIdFlow(formId: Long): Flow<List<FormImageEntity>>
    suspend fun updateImageRemotePath(id: Long, remotePath: String)

    suspend fun saveFormEntry(entry: FormEntryEntity): Long
//    suspend fun saveFormWithImages(
//        context: Context,
//        answers: Map<String, Any?>,
//        initialEntry: FormEntryEntity,
//        serializerFn: (Map<String, Any?>) -> String
//    ): Long


    // NOTE: development only
    suspend fun clearDatabase()
}

class FormRepositoryImpl @Inject constructor(
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val formEntryDao: FormEntryDao
) : FormRepository {

    private val _syncEvents = MutableSharedFlow<String>()
    override val syncEvents = _syncEvents.asSharedFlow()

    override suspend fun getEntryByCollectionTaskId(collectionTaskId: Int): FormEntryEntity? {
        return formEntryDao.getEntryByCollectionTaskId(collectionTaskId)
    }

    override suspend fun getEntryById(id: Long): FormEntryEntity {
        Log.d(TAG, "getEntryById(id = $id)")
        return formEntryDao.getEntryById(id)
    }

    override fun getEntryByIdFlow(id: Long): Flow<FormEntryEntity> {
        Log.d(TAG, "getEntryByIdFlow(id = $id)")
        return formEntryDao.getEntryByIdFlow(id)
    }

//    override suspend fun getImagesOfEntryById(formId: Long): List<FormImageEntity> {
//        Log.d(TAG, "getImagesOfEntryById(formId = $formId)")
//        return formEntryDao.getImagesOfEntryById(formId)
//    }
//
//    override fun getImagesOfEntryByIdFlow(formId: Long): Flow<List<FormImageEntity>> {
//        Log.d(TAG, "getImagesOfEntryByIdFlow(formId = $formId)")
//        return formEntryDao.getImagesOfEntryByIdFlow(formId)
//    }

    override suspend fun updateImageRemotePath(id: Long, remotePath: String) {
        return formEntryDao.updateImageRemotePath(id, remotePath)
    }

    override fun getAllEntries(): Flow<List<FormEntryEntity>> {
        Log.d(TAG, "getAllEntries()")
        return formEntryDao.getAll()
    }

    override suspend fun saveFormEntry(entry: FormEntryEntity): Long {
        Log.d(TAG, "saveFormEntry(): $entry")
        return formEntryDao.insertEntry(entry)
    }

    // todo: review this sheit
//    override suspend fun saveFormWithImages(
//        context: Context,
//        answers: Map<String, Any?>,
//        initialEntry: FormEntryEntity,
//        serializerFn: (Map<String, Any?>) -> String
//    ): Long = withContext(ioDispatcher) {
//
//        val folder = File(context.filesDir, "forms/${UUID.randomUUID()}").apply { mkdirs() }
//
//        try {
//            val localAnswers = context.saveImagesToFolder(answers, folder)
//            val insertedId = formEntryDao.insertFormWithImages(
//                entry = initialEntry.copy(payloadJson = serializerFn(localAnswers)),
//                images = localAnswers.toFormImages()
//            )
//
//            insertedId
//        } catch (e: Exception) {
//            folder.deleteRecursively()
//            throw e
//        }
//    }

    override suspend fun getPendingSyncOnce(): List<FormEntryEntity> {
        Log.d(TAG, "getPendingSyncOnce()")
        return formEntryDao.getPendingSyncOnce()
    }

    override suspend fun getPendingSyncOnceForUser(userId: String): List<FormEntryEntity> {
        return formEntryDao.getPendingSyncForUser(userId)
    }

    override suspend fun markAsSyncedWithStatus(id: Long, timestamp: Instant, status: SyncStatus) {
        formEntryDao.updateSyncStatus(id, timestamp, status)
    }

    override suspend fun clearDatabase() {
        Log.w(TAG, "clearDatabase() called")
        formEntryDao.clearAll()
    }

    companion object {
        private const val TAG = "Scout: FormRepository"
    }
}
