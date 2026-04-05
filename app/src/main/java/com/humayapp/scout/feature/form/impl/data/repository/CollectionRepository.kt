package com.humayapp.scout.feature.form.impl.data.repository

import com.humayapp.scout.core.database.dao.CachedFormDetailsDao
import android.util.Log
import com.humayapp.scout.core.database.converters.toDomain
import com.humayapp.scout.core.database.converters.toEntity
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.model.CachedFormDetailsEntity
import com.humayapp.scout.core.database.model.CollectionTaskEntity
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.form.impl.model.CulturalManagementForm
import com.humayapp.scout.feature.form.impl.model.DamageAssessmentForm
import com.humayapp.scout.feature.form.impl.model.FieldActivityDetails
import com.humayapp.scout.feature.form.impl.model.FieldDataForm
import com.humayapp.scout.feature.form.impl.model.FormData
import com.humayapp.scout.feature.form.impl.model.NutrientManagementForm
import com.humayapp.scout.feature.form.impl.model.ProductionForm
import com.humayapp.scout.feature.form.impl.model.formDataJson
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
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

    suspend fun cacheFormDetails(activityId: Int, rawDetails: FieldActivityDetails, formData: FormData)
    suspend fun getCachedFormDetails(activityId: Int): CachedFormDetailsEntity?
    suspend fun cacheFormDetailsByTaskId(collectionTaskId: Int, rawDetails: FieldActivityDetails, formData: FormData)
    suspend fun getCachedFormDetailsByTaskId(collectionTaskId: Int): CachedFormDetailsEntity?
}

class CollectionRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository,
    private val collectionTaskDao: CollectionTaskDao,
    private val cacheDao: CachedFormDetailsDao
) : CollectionRepository {

    override fun getAllCollectionTasks(): Flow<List<CollectionTask>> =
        collectionTaskDao.getAllTasks()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getCollectionTaskById(taskId: Int): CollectionTask? {
        val entity = collectionTaskDao.getTaskById(taskId)
        return entity?.toDomain()
    }

    override suspend fun cacheFormDetails(activityId: Int, rawDetails: FieldActivityDetails, formData: FormData) {
        val entity = CachedFormDetailsEntity(
            activityId = activityId,
            rawDetailsJson = Json.encodeToString(rawDetails),
            formDataJson = Json.encodeToString(formData),
            activityType = rawDetails.activityType
        )
        cacheDao.insert(entity)
    }

    override suspend fun cacheFormDetailsByTaskId(collectionTaskId: Int, rawDetails: FieldActivityDetails, formData: FormData) {
        val entity = CachedFormDetailsEntity(
            collectionTaskId = collectionTaskId,
            rawDetailsJson = Json.encodeToString(rawDetails),
            formDataJson = Json.encodeToString(formData),
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

        val tasksToUpsert = mutableListOf<CollectionTaskEntity>()

        for (remote in remoteTasks) {
            val local = localTaskMap[remote.id]
            if (local != null && local.status == "completed" && remote.status == "pending") {
                continue
            }
            tasksToUpsert.add(remote.toEntity())
        }

        if (tasksToUpsert.isNotEmpty()) {
            collectionTaskDao.insertAll(tasksToUpsert)
        }

        for (task in remoteTasks) {
            if (task.activityId != null) {
                try {
                    val details = supabaseClient.from("field_activity_details")
                        .select() {
                            filter {
                                eq("id", task.activityId)
                            }
                        }.decodeSingle<FieldActivityDetails>()
                    val formData = parseFormData(details.activityType, details.formData)
                    cacheFormDetails(task.activityId, details, formData)
                } catch (e: Exception) {
                    Log.e("CollectionRepo", "Failed to cache form details for activityId ${task.activityId}", e)
                }
            }
        }
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

    private fun parseFormData(activityType: String, formDataElement: JsonElement): FormData {
        return when (activityType) {
            "field-data" -> formDataJson.decodeFromJsonElement<FieldDataForm>(formDataElement)
            "cultural-management" -> formDataJson.decodeFromJsonElement<CulturalManagementForm>(formDataElement)
            "nutrient-management" -> formDataJson.decodeFromJsonElement<NutrientManagementForm>(formDataElement)
            "production" -> formDataJson.decodeFromJsonElement<ProductionForm>(formDataElement)
            "damage-assessment" -> formDataJson.decodeFromJsonElement<DamageAssessmentForm>(formDataElement)
            else -> throw IllegalArgumentException("Unknown activity type: $activityType")
        }
    }
}
