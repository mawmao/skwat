package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface FormEntryDao {

    @Transaction
    suspend fun insertFormWithImages(entry: FormEntryEntity, images: List<FormImageEntity>): Long {
        val formId = insertEntry(entry)
        images.forEach { insertImage(it.copy(formId = formId)) }
        return formId
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(activity: FormEntryEntity): Long

    @Query("SELECT * FROM form_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): FormEntryEntity

    @Query("SELECT * FROM form_entries WHERE syncedAt IS NOT NULL")
    suspend fun getPendingSyncOnce(): List<FormEntryEntity>

    @Query("SELECT * FROM form_entries WHERE syncedAt IS NOT NULL")
    fun getPendingSync(): Flow<List<FormEntryEntity>>

    @Query("SELECT * FROM form_entries ORDER BY collectedAt DESC")
    fun getAll(): Flow<List<FormEntryEntity>>

    @Query("UPDATE form_entries SET syncedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: Long, timestamp: Instant)

    // NOTE: development only
    @Query("DELETE FROM form_entries")
    suspend fun clearAll()

    // =======
    // images


    @Insert
    suspend fun insertImage(image: FormImageEntity)

    @Query("SELECT * FROM form_images WHERE formId = :formId")
    suspend fun getImagesOfEntryById(formId: Long): List<FormImageEntity>

    @Query("SELECT * FROM form_images WHERE formId = :formId")
    fun getImagesOfEntryByIdFlow(formId: Long): Flow<List<FormImageEntity>>

    @Query("UPDATE form_images SET remotePath = :remotePath WHERE id = :id")
    suspend fun updateImageRemotePath(id: Long, remotePath: String)
}
