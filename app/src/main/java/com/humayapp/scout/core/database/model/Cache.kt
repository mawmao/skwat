package com.humayapp.scout.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(tableName = "cached_form_details")
data class CachedFormDetailsEntity(
    @PrimaryKey val id: Int = 0,
    val activityId: Int? = null,
    val collectionTaskId: Int? = null,
    val rawDetailsJson: String,
    val formDataJson: String,
    val activityType: String,
    val syncedAt: Instant = Clock.System.now()
)
