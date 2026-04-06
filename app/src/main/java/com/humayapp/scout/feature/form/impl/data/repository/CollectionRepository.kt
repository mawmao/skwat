package com.humayapp.scout.feature.form.impl.data.repository

import android.content.Context
import com.humayapp.scout.core.database.dao.CachedFormDetailsDao
import android.util.Log
import com.humayapp.scout.core.common.dispatcher.Dispatcher
import com.humayapp.scout.core.common.dispatcher.ScoutDispatchers
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.converters.toDomain
import com.humayapp.scout.core.database.converters.toEntity
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.model.CachedFormDetailsEntity
import com.humayapp.scout.core.database.model.CollectionTaskEntity
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.core.network.util.SupabaseImageHelper
import com.humayapp.scout.core.system.saveImagesToFolder
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.form.impl.model.CulturalManagementForm
import com.humayapp.scout.feature.form.impl.model.DamageAssessmentForm
import com.humayapp.scout.feature.form.impl.model.FieldActivityDetails
import com.humayapp.scout.feature.form.impl.model.FieldDataForm
import com.humayapp.scout.feature.form.impl.model.FormData
import com.humayapp.scout.feature.form.impl.model.NutrientManagementForm
import com.humayapp.scout.feature.form.impl.model.ProductionForm
import com.humayapp.scout.feature.form.impl.model.formDataJson
import com.humayapp.scout.feature.form.impl.model.toFormImages
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.ktor.utils.io.ioDispatcher
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File
import java.util.UUID
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

interface CollectionRepository {
    fun getAllCollectionTasks(): Flow<List<CollectionTask>>
    suspend fun getCollectionTaskById(taskId: Int): CollectionTask?
    suspend fun pullTasksFromSupabaseForCurrentUser()
    suspend fun getRetakeTaskByOriginalId(
        originalId: Int,
        status: String,
        verificationStatus: String? = null
    ): CollectionTask?

    suspend fun cacheFormDetails(activityId: Int, rawDetails: FieldActivityDetails, formDataElement: JsonElement)
    suspend fun cacheFormDetailsByTaskId(
        collectionTaskId: Int,
        rawDetails: FieldActivityDetails,
        formDataElement: JsonElement
    )

    suspend fun getCachedFormDetails(activityId: Int): CachedFormDetailsEntity?
    suspend fun getCachedFormDetailsByTaskId(collectionTaskId: Int): CachedFormDetailsEntity?

    suspend fun saveTaskWithImages(
        context: Context,
        answers: Map<String, Any?>,
        collectionTaskId: Int,
        userId: String,
        formData: String
    ): Int

    suspend fun getUnsyncedTasks(): List<CollectionTask>

    suspend fun getImagesById(collectionTaskId: Int): List<FormImageEntity>

    suspend fun markSynced(collectionTaskId: Int): Int

    suspend fun getDetailsFromSupabase(collectionTaskId: Int): FieldActivityDetails

    suspend fun getCollectionTaskFromSupabase(taskId: Int): CollectionTask?
    suspend fun upsertCollectionTask(task: CollectionTask)
}

class CollectionRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository,
    private val collectionTaskDao: CollectionTaskDao,
    private val cacheDao: CachedFormDetailsDao,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : CollectionRepository {

    override suspend fun getUnsyncedTasks(): List<CollectionTask> =
        collectionTaskDao.getUnsyncedTasks().map { it.toDomain() }

    override suspend fun getImagesById(collectionTaskId: Int): List<FormImageEntity> =
        collectionTaskDao.getImagesById(collectionTaskId)

    override suspend fun markSynced(collectionTaskId: Int): Int {
        return collectionTaskDao.markSynced(collectionTaskId)
    }

    override suspend fun saveTaskWithImages(
        context: Context,
        answers: Map<String, Any?>,
        collectionTaskId: Int,
        userId: String,
        formData: String
    ): Int = withContext(ioDispatcher) {

        val folder = File(context.filesDir, "forms/${UUID.randomUUID()}").apply { mkdirs() }

        try {
            val localAnswers = context.saveImagesToFolder(answers, folder)

            collectionTaskDao.completeTaskWithImages(
                collectionTaskId = collectionTaskId,
                collectorId = userId,
                collectedAt = Clock.System.now(),
                formData = formData,
                images = localAnswers.toFormImages(collectionTaskId)
            )

            collectionTaskId
        } catch (e: Exception) {
            folder.deleteRecursively()
            throw e
        }
    }

    override fun getAllCollectionTasks(): Flow<List<CollectionTask>> =
        collectionTaskDao.getAllTasks()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getCollectionTaskById(taskId: Int): CollectionTask? {
        val entity = collectionTaskDao.getTaskById(taskId)
        return entity?.toDomain()
    }

    override suspend fun cacheFormDetails(
        activityId: Int,
        rawDetails: FieldActivityDetails,
        formDataElement: JsonElement
    ) {
        val entity = CachedFormDetailsEntity(
            activityId = activityId,
            rawDetailsJson = Json.encodeToString(rawDetails),
            formDataJson = formDataElement.toString(),
            activityType = rawDetails.activityType
        )
        cacheDao.insert(entity)
    }

    override suspend fun cacheFormDetailsByTaskId(
        collectionTaskId: Int,
        rawDetails: FieldActivityDetails,
        formDataElement: JsonElement
    ) {
        val entity = CachedFormDetailsEntity(
            collectionTaskId = collectionTaskId,
            rawDetailsJson = Json.encodeToString(rawDetails),
            formDataJson = formDataElement.toString(),
            activityType = rawDetails.activityType
        )
        cacheDao.insert(entity)
    }


    override suspend fun getCachedFormDetails(activityId: Int): CachedFormDetailsEntity? {
        return cacheDao.getByActivityId(activityId)
    }

    override suspend fun getCachedFormDetailsByTaskId(collectionTaskId: Int): CachedFormDetailsEntity? {
        return cacheDao.getByCollectionTaskId(collectionTaskId)
    }


    @OptIn(ExperimentalUuidApi::class)
    override suspend fun pullTasksFromSupabaseForCurrentUser() {
        val userId = authRepository.getCurrentUserId() ?: return
        val remoteTasks = supabaseClient
            .from("collection_details")
            .select { filter { eq("collector_id", userId) } }
            .decodeList<CollectionTask>()

        val localTasks = collectionTaskDao.getAllTasksList()
        val localTaskMap = localTasks.associateBy { it.id }

        // 1. Upsert tasks that are present remotely (or keep local completed if remote is pending)
        val tasksToUpsert = mutableListOf<CollectionTaskEntity>()
        for (remote in remoteTasks) {
            val local = localTaskMap[remote.id]
            // If local is completed and remote is pending, keep local completed version
            if (local != null && local.status == "completed" && remote.status == "pending") {
                continue
            }
            val entity = remote.toEntity(false).copy(
                formData = remote.formData ?: local?.formData
            )
            tasksToUpsert.add(entity)
        }
        if (tasksToUpsert.isNotEmpty()) {
            collectionTaskDao.insertAll(tasksToUpsert)
        }

        // 2. Delete local tasks that are no longer assigned to this user (not in remote list) AND are not completed
        val remoteIds = remoteTasks.map { it.id }.toSet()
        val tasksToDelete = localTasks.filter { local ->
            local.id !in remoteIds && local.status != "completed"
        }
        if (tasksToDelete.isNotEmpty()) {
            val idsToDelete = tasksToDelete.map { it.id }
            collectionTaskDao.deleteTasksByIds(idsToDelete)
            // Optionally delete associated images and files
            deleteAssociatedImagesForTasks(idsToDelete)
        }
    }

    // Helper to delete images (implement as needed)
    private suspend fun deleteAssociatedImagesForTasks(taskIds: List<Int>) {
        // Delete image entries from DB
        collectionTaskDao.deleteImagesByTaskIds(taskIds)
        // Delete actual image files from storage
        val images = collectionTaskDao.getImagesByTaskIds(taskIds)
        images.forEach { image ->
            File(image.localPath).delete()
        }
    }

    override suspend fun getDetailsFromSupabase(collectionTaskId: Int): FieldActivityDetails {
        val userId =
            authRepository.getCurrentUserId() ?: unreachable("there should be a user when calling this function")
        val fa = supabaseClient
            .from("field_activity_details")
            .select { filter { eq("collection_task_id", userId) } }
            .decodeSingle<FieldActivityDetails>()
        return fa
    }

    override suspend fun getRetakeTaskByOriginalId(
        originalId: Int,
        status: String,
        verificationStatus: String?
    ): CollectionTask? {
        val entity = if (verificationStatus != null) {
            collectionTaskDao.getRetakeTaskByOriginalIdAndStatusAndVerification(originalId, status, verificationStatus)
        } else {
            collectionTaskDao.getRetakeTaskByOriginalIdAndStatus(originalId, status)
        }
        return entity?.toDomain()
    }

    override suspend fun getCollectionTaskFromSupabase(taskId: Int): CollectionTask? = withContext(ioDispatcher) {
        try {
            supabaseClient.from("collection_details")
                .select {
                    filter { eq("id", taskId) }
                }
                .decodeSingle<CollectionTask>()
        } catch (e: Exception) {
            Log.e("Scout: CollectionRepository", "Error fetching task from Supabase", e)
            null
        }
    }

    override suspend fun upsertCollectionTask(task: CollectionTask) = withContext(ioDispatcher) {
        collectionTaskDao.insertOrUpdate(task.toEntity())
    }
}
