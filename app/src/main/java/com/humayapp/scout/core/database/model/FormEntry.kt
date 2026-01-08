package com.humayapp.scout.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity(
    tableName = "form_entries",
    indices = [Index(value = ["mfid", "activityType"], unique = true)]
)
data class FormEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mfid: String,
    val activityType: String, // equivalent to [FormType.id]
    val collectedBy: String, // uuid
    val collectedAt: String = Instant.ofEpochMilli(System.currentTimeMillis())
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE),
    val synced: Boolean = false,
    val payloadJson: String,
)
