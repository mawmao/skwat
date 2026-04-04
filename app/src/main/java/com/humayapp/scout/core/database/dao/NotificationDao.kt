package com.humayapp.scout.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humayapp.scout.core.database.model.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("SELECT MAX(createdAt) FROM notifications WHERE userId = :userId")
    suspend fun getLatestCreatedAt(userId: String): Instant?

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}
