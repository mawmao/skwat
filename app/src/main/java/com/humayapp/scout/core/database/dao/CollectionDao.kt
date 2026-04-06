package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.humayapp.scout.core.database.model.CollectionTaskEntity
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.network.CollectionTask
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Instant

@Dao
interface CollectionTaskDao {

    @Query("SELECT * FROM collection_tasks WHERE retakeOf = :originalId AND status = :status LIMIT 1")
    suspend fun getRetakeTaskByOriginalIdAndStatus(originalId: Int, status: String): CollectionTaskEntity?

    @Query("SELECT * FROM collection_tasks WHERE retakeOf = :originalId AND status = :status AND verificationStatus = :verificationStatus LIMIT 1")
    suspend fun getRetakeTaskByOriginalIdAndStatusAndVerification(
        originalId: Int,
        status: String,
        verificationStatus: String
    ): CollectionTaskEntity?

    @Query("SELECT * FROM collection_tasks")
    suspend fun getAllTasksList(): List<CollectionTaskEntity>

    @Query("SELECT * FROM collection_tasks ORDER BY collectedAt DESC")
    fun getAllTasks(): Flow<List<CollectionTaskEntity>>

    @Query("SELECT * FROM collection_tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): CollectionTaskEntity?

    @Query("SELECT * FROM collection_tasks WHERE mfid = :mfid AND activityType = :activityType LIMIT 1")
    suspend fun getTaskByMfidAndActivity(mfid: String, activityType: String): CollectionTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<CollectionTaskEntity>)

    @Transaction
    suspend fun updateVerification(
        id: Int,
        status: String,
        verifiedBy: String?,
        remarks: String?
    ) {
        val task = getTaskById(id)?.copy(
            verificationStatus = status,
            verifiedBy = verifiedBy,
            verifiedAt = Clock.System.now(),
            remarks = remarks
        ) ?: return
        insertAll(listOf(task)) // triggers Flow
    }

    @Transaction
    suspend fun updateCollected(
        mfid: String,
        activityType: String,
        collectorId: String,
        collectedAt: Instant = Clock.System.now()
    ) {
        val task = getTaskByMfidAndActivity(mfid, activityType)?.copy(
            collectedBy = collectorId,
            collectedAt = collectedAt,
            status = "completed"
        ) ?: return
        insertAll(listOf(task))
    }

    @Query("UPDATE collection_tasks SET status = 'completed', collectedBy = :collectorId, collectedAt = :collectedAt WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: Int, collectorId: String, collectedAt: Instant)

    @Transaction
    suspend fun completeTaskWithImages(
        collectionTaskId: Int,
        collectorId: String,
        collectedAt: Instant,
        formData: String,
        images: List<FormImageEntity>
    ) {
        images.forEach { insertImage(it) }
        markComplete(collectionTaskId, collectorId, collectedAt, formData)
    }

    @Insert
    suspend fun insertImage(image: FormImageEntity)


    @Query(
        """
            UPDATE collection_tasks 
            SET status = 'completed', 
                collectedBy = :collectorId, 
                collectedAt = :collectedAt,
                formData = :formData,
                synced = 0
            WHERE id = :taskId
        """
    )
    suspend fun markComplete(
        taskId: Int,
        collectorId: String,
        collectedAt: Instant,
        formData: String
    )


    @Query(
        """
        UPDATE collection_tasks 
        SET synced = 1
        WHERE id = :taskId
    """
    )
    suspend fun markSynced(taskId: Int): Int

    @Query("SELECT * FROM collection_tasks WHERE synced = 0 AND status = 'completed'")
    suspend fun getUnsyncedTasks(): List<CollectionTaskEntity>

    @Query("SELECT * FROM form_images WHERE collectionTaskId = :collectionTaskId")
    suspend fun getImagesById(collectionTaskId: Int): List<FormImageEntity>
}
