package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humayapp.scout.core.database.model.FormEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: FormEntryEntity): Long

    @Query("SELECT * FROM form_entries WHERE synced = 0")
    suspend fun getPendingSyncOnce(): List<FormEntryEntity>

    @Query("SELECT * FROM form_entries WHERE synced = 0")
    fun getPendingSync(): Flow<List<FormEntryEntity>>

    @Query("SELECT * FROM form_entries ORDER BY collectedAt DESC")
    fun getAll(): Flow<List<FormEntryEntity>>

    @Query("UPDATE form_entries SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    // NOTE: development only
    @Query("DELETE FROM form_entries")
    suspend fun clearAll()
}
