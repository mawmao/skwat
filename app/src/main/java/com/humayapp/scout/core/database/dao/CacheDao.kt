package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humayapp.scout.core.database.model.CachedFormDetailsEntity
import kotlin.time.Instant

@Dao
interface CachedFormDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedFormDetailsEntity)

    @Query("SELECT * FROM cached_form_details WHERE activityId = :activityId")
    suspend fun getByActivityId(activityId: Int): CachedFormDetailsEntity?

    @Query("SELECT * FROM cached_form_details WHERE collectionTaskId = :collectionTaskId")
    suspend fun getByCollectionTaskId(collectionTaskId: Int): CachedFormDetailsEntity?
}
