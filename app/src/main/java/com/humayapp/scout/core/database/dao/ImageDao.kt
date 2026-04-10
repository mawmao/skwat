package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import com.humayapp.scout.core.database.model.FormImageEntity

@Dao
interface ImagesDao {

    @Insert
    suspend fun insertImage(image: FormImageEntity)

    @Query("SELECT * FROM form_images WHERE collectionTaskId = :taskId")
    suspend fun getImagesById(taskId: Int): List<FormImageEntity>

    @Insert(onConflict = IGNORE)
    suspend fun insertAll(images: List<FormImageEntity>)

    @Query("UPDATE form_images SET remotePath = :remotePath WHERE id = :id")
    suspend fun updateRemotePath(id: Long, remotePath: String)
}
