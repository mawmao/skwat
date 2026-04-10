package com.humayapp.scout.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val key: String,
    val lastSync: Instant
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: SyncType,

    val refId: String, // mfid / taskId / formId

    val createdAt: Instant = Clock.System.now(),

    val payload: String? = null, // optional serialized form

    val attempts: Int = 0,

    val lastError: String? = null,

    val status: SyncStatus = SyncStatus.PENDING
)

enum class SyncType {
    FORM_SUBMISSION,
    TASK_UPDATE
}

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    DONE,
    FAILED
}

