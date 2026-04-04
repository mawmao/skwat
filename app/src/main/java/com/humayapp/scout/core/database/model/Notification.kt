package com.humayapp.scout.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val userId: String?,
    val title: String,
    val message: String,
    val targetRole: String,
    val type: String,
    val relatedEntityId: String?,
    val isRead: Boolean,
    val createdAt: Instant
)