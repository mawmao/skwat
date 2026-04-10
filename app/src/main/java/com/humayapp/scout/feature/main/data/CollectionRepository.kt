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
import com.humayapp.scout.core.sync.SyncOrchestrator
import com.humayapp.scout.core.system.saveImagesToFolder
import com.humayapp.scout.feature.form.impl.model.toFormImages
import com.humayapp.scout.feature.main.data.collection.FormNetworkDataSource
import com.humayapp.scout.feature.main.data.collection.TaskNetworkDataSource
import com.humayapp.scout.feature.main.data.util.ImageResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val syncOrchestrator: SyncOrchestrator,
    private val imageResolver: ImageResolver,
) {

    fun observeTasks() = taskDao.observeTasks().map { relations -> relations.map { it.toUiModel() } }

    fun observeTaskById(id: Int) = taskDao.observeTaskById(id).map { it.toUiModel() }

    fun observeTaskWithImages(taskId: Int) = observeTaskById(taskId)
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

    suspend fun saveTaskWithImages(
        context: Context,
        images: Map<String, String>,
        collectionTaskId: Int,
        userId: String,
        formData: String,
        activityType: String,
    ): Boolean = withContext(Dispatchers.IO) {
        val folder = File(context.filesDir, "forms/${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val imagesEntities = context
                .saveImagesToFolder(images, folder)
                .toFormImages(collectionTaskId)

            return@withContext database.withTransaction {
                imagesDao.insertAll(imagesEntities)
                formDao.insert(
                    form = CollectionFormEntity(
                        activityType = activityType,
                        taskId = collectionTaskId,
                        payload = formData,
                        updatedAt = Clock.System.now(),
                        verificationStatus = "pending"
                    )
                )
                val updated = taskDao.markTaskCompleted(collectionTaskId, userId, Clock.System.now())
                updated == 1
            }
        } catch (e: Exception) {
            folder.deleteRecursively()
            throw e
        }
    }

    suspend fun markFormAsSynced(id: Int): Int {
        return formDao.markSynced(id)
    }

    suspend fun fullSync() {
        Log.i("Scout: CollectionRepository", "[Sync] Pulling new data...")
        syncOrchestrator.run {
            pullTasks()
            reconcileTasks()
            pullForms()
        }
    }

    private suspend fun pullTasks() {
        val lastSync = syncRepository.getLastSync("tasks")

        val remote = taskDataSource.getTasks(updatedAfter = lastSync)

        val maxUpdated = remote
            .mapNotNull { it.updatedAt.toInstantSafeOrNull() }
            .maxOrNull()

        database.withTransaction {
            taskDao.upsert(remote.map { it.toTaskEntity() })

            val newSync = listOfNotNull(lastSync, maxUpdated).maxOrNull()

            if (newSync != null) {
                syncRepository.updateSyncState(key = "tasks", lastSync = newSync)
            }
        }
    }

    private suspend fun reconcileTasks() {
        val remoteIds = taskDataSource.getAllTaskIds()

        database.withTransaction {
            if (remoteIds.isNotEmpty()) {
                taskDao.deleteTasksNotIn(remoteIds)
            }
        }
    }

    private suspend fun pullForms() {
        val lastSync = syncRepository.getLastSync("forms")

        val remote = formDataSource.getForms(updatedAfter = lastSync)

        val maxUpdated = remote
            .mapNotNull { it.updatedAt.toInstantSafeOrNull() }
            .maxOrNull()

        database.withTransaction {
            formDao.upsert(remote.map { it.toFormEntity() })

            val newSync = listOfNotNull(lastSync, maxUpdated).maxOrNull()

            if (newSync != null) {
                syncRepository.updateSyncState(key = "forms", lastSync = newSync)
            }
        }
    }
}