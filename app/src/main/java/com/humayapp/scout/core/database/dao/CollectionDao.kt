package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.humayapp.scout.core.database.model.CollectionFormEntity
import com.humayapp.scout.core.database.model.CollectionTaskEntity
import com.humayapp.scout.core.database.model.TaskWithFormRelation
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface CollectionTaskDao {

    @Transaction
    @Query("SELECT * FROM collection_tasks")
    fun observeTasks(): Flow<List<TaskWithFormRelation>>

    @Transaction
    @Query("SELECT * FROM collection_tasks WHERE id = :id")
    fun observeTaskById(id: Int): Flow<TaskWithFormRelation>

    @Transaction
    @Query("SELECT * FROM collection_tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskWithFormRelation

    @Query("SELECT * FROM collection_tasks WHERE id IN (:ids)")
    suspend fun getTasksByIds(ids: List<Int>): List<CollectionTaskEntity>

    @Upsert
    suspend fun upsert(tasks: List<CollectionTaskEntity>)

    @Upsert
    suspend fun upsert(task: CollectionTaskEntity)

    @Query("UPDATE collection_tasks SET status = 'completed', collectedBy = :collectorId, collectedAt = :collectedAt WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: Int, collectorId: String, collectedAt: Instant): Int

    @Query("""
        DELETE FROM collection_tasks
        WHERE id NOT IN (:ids)
        """)
    suspend fun deleteTasksNotIn(ids: List<Int>)

}


@Dao
interface CollectionFormDao {
    @Upsert
    suspend fun upsert(tasks: List<CollectionFormEntity>)

    @Upsert
    suspend fun upsert(task: CollectionFormEntity)

    @Query("UPDATE collection_forms SET synced = 1 WHERE taskId = :taskId")
    suspend fun markSynced(taskId: Int): Int

    @Insert
    suspend fun insert(form: CollectionFormEntity)

    @Query("SELECT * FROM collection_forms WHERE synced = 0")
    suspend fun getUnsyncedForms(): List<CollectionFormEntity>
}