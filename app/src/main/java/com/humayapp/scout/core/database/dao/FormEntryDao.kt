package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface FormEntryDao {

//    @Transaction
//    suspend fun insertFormWithImages(entry: FormEntryEntity, images: List<FormImageEntity>): Long {
//        val formId = insertEntry(entry)
//        images.forEach { insertImage(it.copy(formId = formId)) }
//        return formId
//    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(activity: FormEntryEntity): Long

    @Query("SELECT * FROM form_entries WHERE collectionTaskId = :collectionTaskId LIMIT 1")
    suspend fun getEntryByCollectionTaskId(collectionTaskId: Int): FormEntryEntity?

    @Query("SELECT * FROM form_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): FormEntryEntity

    @Query("SELECT * FROM form_entries WHERE id = :id")
    fun getEntryByIdFlow(id: Long): Flow<FormEntryEntity>

    @Query("SELECT * FROM form_entries WHERE syncStatus = 'PENDING' ORDER BY id ASC")
    suspend fun getPendingSyncOnce(): List<FormEntryEntity>

    @Query("SELECT * FROM form_entries WHERE syncStatus = :status AND collectedBy = :userId ORDER BY id ASC LIMIT 1")
    suspend fun getPendingSyncForUser(userId: String, status: SyncStatus = SyncStatus.PENDING): List<FormEntryEntity>

    @Query("SELECT * FROM form_entries WHERE syncedAt IS NOT NULL")
    fun getPendingSync(): Flow<List<FormEntryEntity>>

    @Query("SELECT * FROM form_entries ORDER BY collectedAt DESC")
    fun getAll(): Flow<List<FormEntryEntity>>

    @Query("UPDATE form_entries SET syncedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: Long, timestamp: Instant)

    @Query("UPDATE form_entries SET syncedAt = :timestamp, syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, timestamp: Instant, status: SyncStatus)

    // NOTE: development only
    @Query("DELETE FROM form_entries")
    suspend fun clearAll()

    // =======
    // images


    @Insert
    suspend fun insertImage(image: FormImageEntity)

//    @Query("SELECT * FROM form_images WHERE formId = :formId")
//    suspend fun getImagesOfEntryById(formId: Long): List<FormImageEntity>
//
//    @Query("SELECT * FROM form_images WHERE formId = :formId")
//    fun getImagesOfEntryByIdFlow(formId: Long): Flow<List<FormImageEntity>>

    @Query("UPDATE form_images SET remotePath = :remotePath WHERE id = :id")
    suspend fun updateImageRemotePath(id: Long, remotePath: String)
}
