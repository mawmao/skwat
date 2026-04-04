package com.humayapp.scout.core.data.notification

import com.humayapp.scout.core.database.model.NotificationEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Notification(
    @SerialName("id") val id: Int,
    @SerialName("user_id") val userId: String?,
    @SerialName("title") val title: String,
    @SerialName("message") val message: String,
    @SerialName("type") val type: String,
    @SerialName("target_role") val targetRole: String,
    @SerialName("related_entity_id") val relatedEntityId: String?,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: Instant
)

fun NotificationEntity.toDomain() = Notification(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    targetRole = targetRole,
    relatedEntityId = relatedEntityId,
    isRead = isRead,
    createdAt = createdAt
)

fun Notification.toEntity() = NotificationEntity(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    targetRole = targetRole,
    relatedEntityId = relatedEntityId,
    isRead = isRead,
    createdAt = createdAt
)
