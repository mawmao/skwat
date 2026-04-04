package com.humayapp.scout.feature.form.impl.data.repository

import com.humayapp.scout.core.database.converters.toDomain
import com.humayapp.scout.core.database.converters.toEntity
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.model.CollectionTaskEntity
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.feature.auth.data.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
}

class CollectionRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository,
    private val collectionTaskDao: CollectionTaskDao
) : CollectionRepository {

    override fun getAllCollectionTasks(): Flow<List<CollectionTask>> =
        collectionTaskDao.getAllTasks()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getCollectionTaskById(taskId: Int): CollectionTask? {
        val entity = collectionTaskDao.getTaskById(taskId)
        return entity?.toDomain()
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
}
