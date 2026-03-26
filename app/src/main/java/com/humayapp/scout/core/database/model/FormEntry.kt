package com.humayapp.scout.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
@Entity(
    tableName = "form_entries",
    indices = [Index(value = ["mfid", "activityType"])]
)
data class FormEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mfid: String,
    val activityType: String, // equivalent to [FormType.id]
    val payloadJson: String,
    val imageUrls: List<String> = emptyList(),
    val syncedAt: Instant? = null,
    val collectedBy: String,
    val collectedAt: Instant = Clock.System.now(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class SyncStatus {
    PENDING,
    SYNCED,
    DUPLICATE
}

@Entity(tableName = "form_images")
data class FormImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val formId: Long,
    val localPath: String,
    val remotePath: String? = null
)

