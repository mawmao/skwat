package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humayapp.scout.core.database.model.SyncQueueEntity
import com.humayapp.scout.core.database.model.SyncStateEntity
import com.humayapp.scout.core.database.model.SyncStatus
import kotlin.time.Instant

@Dao
interface SyncStateDao {
    @Query("SELECT * FROM sync_state WHERE key = :key")
    suspend fun get(key: String): SyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncStateEntity)

    @Query("SELECT lastSync FROM sync_state WHERE key = :key")
    suspend fun getLastSync(key: String): Instant?
}

@Dao
interface SyncQueueDao {

    @Insert
    suspend fun insert(item: SyncQueueEntity)

    @Query(
        """
            SELECT * FROM sync_queue 
            WHERE status IN ('PENDING', 'FAILED') 
            AND attempts < 3  
            ORDER BY createdAt ASC
        """
    )
    suspend fun getPending(): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SyncStatus)

    @Query("UPDATE sync_queue SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: Long)
}