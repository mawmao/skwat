package com.humayapp.scout.feature.main.data

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.humayapp.scout.core.data.sync.SyncRepository
import com.humayapp.scout.core.database.ScoutDatabase
import com.humayapp.scout.core.database.dao.CollectionFormDao
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.dao.ImagesDao
import com.humayapp.scout.core.database.dto.toFormEntity
import com.humayapp.scout.core.database.dto.toTaskEntity
import com.humayapp.scout.core.database.model.CollectionFormEntity
import com.humayapp.scout.core.database.model.CollectionTaskUiModel
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.TaskWithFormRelation
import com.humayapp.scout.core.database.model.toUiModel
import com.humayapp.scout.core.database.util.toInstantSafeOrNull
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.core.system.saveImagesToFolder
import com.humayapp.scout.feature.form.impl.model.toFormImages
import com.humayapp.scout.feature.main.data.collection.FormNetworkDataSource
import com.humayapp.scout.feature.main.data.collection.TaskNetworkDataSource
import com.humayapp.scout.feature.main.data.util.ImageResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionRepository(
    private val database: ScoutDatabase,
    private val taskDao: CollectionTaskDao,
    private val formDao: CollectionFormDao,
    private val imagesDao: ImagesDao,
    private val syncRepository: SyncRepository,
    private val taskDataSource: TaskNetworkDataSource,
    private val formDataSource: FormNetworkDataSource,
    private val imageResolver: ImageResolver,
    private val networkMonitor: NetworkMonitor
) {

    fun observeTasks() = taskDao.observeTasks().map { relations -> relations.map { it.toUiModel() } }

    fun observeTaskById(id: Int) = taskDao.observeTaskById(id).map { it.toUiModel() }

    fun observeTaskWithImages(taskId: Int) = observeTaskById(taskId)
        .distinctUntilChanged()
        .mapLatest { task ->
            val localImages = imagesDao.getImagesById(taskId).map { it.localPath }
            val remotePaths = task.imageUrls.orEmpty()

            val finalImages = imageResolver.resolve(
                localImages = localImages,
                remotePaths = remotePaths
            )

            task to finalImages
        }

    suspend fun getUiTaskById(id: Int): CollectionTaskUiModel = taskDao.getTaskById(id).toUiModel()

    suspend fun getTaskById(id: Int): TaskWithFormRelation = taskDao.getTaskById(id)

    suspend fun getImagesById(id: Int): List<FormImageEntity> = imagesDao.getImagesById(id)

    suspend fun updateImageRemotePath(id: Long, remotePath: String) {
        return imagesDao.updateRemotePath(id, remotePath)
    }

    suspend fun saveTaskLocally(
        context: Context,
        images: Map<String, String>,
        collectionTaskId: Int,
        userId: String,
        formData: String,
        activityType: String,
    ): Boolean = withContext(Dispatchers.IO) {
        val folder = File(context.filesDir, "forms/${UUID.randomUUID()}").apply { mkdirs() }

        Log.d(LOG_TAG, "[START] taskId=$collectionTaskId userId=$userId images=${images.size}")

        try {
            Log.d(LOG_TAG, "[STEP] Saving images locally...")

            val imagesEntities = context
                .saveImagesToFolder(images, folder)
                .toFormImages(collectionTaskId)

            Log.d(LOG_TAG, "[SUCCESS] Saved ${imagesEntities.size} images")

            return@withContext database.withTransaction {
                Log.d(LOG_TAG, "[STEP] Inserting images into DB...")
                imagesDao.insertAll(imagesEntities)
                Log.d(LOG_TAG, "[SUCCESS] Images inserted")

                Log.d(LOG_TAG, "[STEP] Inserting form...")
                formDao.insert(
                    form = CollectionFormEntity(
                        activityType = activityType,
                        taskId = collectionTaskId,
                        payload = formData,
                        updatedAt = Clock.System.now(),
                        verificationStatus = "pending"
                    )
                )
                Log.d(LOG_TAG, "[SUCCESS] Form inserted")

                Log.d(LOG_TAG, "[STEP] Marking task as completed with userId = $userId, timestamp = ${Clock.System.now()}")
                val updated = taskDao.markTaskCompleted(collectionTaskId, userId, Clock.System.now())

                val success = updated == 1

                if (success) {
                    Log.d(LOG_TAG, "[SUCCESS] Task marked completed (rowsUpdated=$updated)")
                } else {
                    Log.e(LOG_TAG, "[FAIL] Task NOT marked completed (rowsUpdated=$updated)")
                }

                success
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[ERROR] Failed to save task. Cleaning up folder.", e)
            folder.deleteRecursively()
            throw e
        }
    }

    suspend fun markFormAsSynced(id: Int): Int {
        return formDao.markSynced(id)
    }

    suspend fun fullSync() {
        if (!networkMonitor.isOnline.drop(1).first()) {
            Log.i(LOG_TAG, "[Sync] Offline – skipping sync to preserve local data")
            return
        }

        Log.i(LOG_TAG, "[Sync] Starting unified pipeline")

        val remoteTasks = try {
            taskDataSource.getTasks(updatedAfter = null)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "[Sync] Task fetch failed. Aborting pipeline.")
            return
        }

        val remoteForms = try {
            formDataSource.getForms(updatedAfter = null)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "[Sync] Form fetch failed. Continuing with tasks only.")
            emptyList()
        }

        val remoteTaskIds = remoteTasks.map { it.id }

        database.withTransaction {
            if (remoteTasks.isNotEmpty()) {
                taskDao.upsert(remoteTasks.map { it.toTaskEntity() })
                val maxTaskUpdated = remoteTasks.mapNotNull { it.updatedAt.toInstantSafeOrNull() }.maxOrNull()
                if (maxTaskUpdated != null) {
                    syncRepository.updateSyncState("tasks", maxTaskUpdated)
                }
            }

            if (remoteForms.isNotEmpty()) {
                formDao.upsert(remoteForms.map { it.toFormEntity() })
                val maxFormUpdated = remoteForms.mapNotNull { it.updatedAt.toInstantSafeOrNull() }.maxOrNull()
                if (maxFormUpdated != null) {
                    syncRepository.updateSyncState("forms", maxFormUpdated)
                }
            }

            Log.i(LOG_TAG, "[Sync] Reconciling tasks (${remoteTaskIds.size} ids)")
            taskDao.deleteWhereNotIn(remoteTaskIds)
        }

        Log.i(LOG_TAG, "[Sync] Pipeline completed")
    }

    companion object {
        private const val LOG_TAG = "Scout: CollectionRepository"
    }
}