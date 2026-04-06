package com.humayapp.scout.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Entity(tableName = "collection_tasks")
data class CollectionTaskEntity(
    @PrimaryKey val id: Int,
    val collectorId: String,
    val activityId: Int? = null,
    val retakeOf: Int? = null,
    val seasonId: Int,
    val mfidId: Int,
    val activityType: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val assignedAt: Instant? = null,
    val mfid: String,
    val collectorName: String,
    val farmerName: String? = null,
    val province: String,
    val cityMunicipality: String,
    val barangay: String? = null,
    val fullAddress: String? = null,
    val isOverdue: Boolean,
    val canRetake: Boolean = false,

    // verification fields
    val verificationStatus: String? = null,
    val verifiedBy: String? = null,
    val verifiedAt: Instant? = null,
    val remarks: String? = null,
    val collectedBy: String? = null,
    val collectedAt: Instant? = null,
    val imageUrls: List<String>? = null,
    val synced: Boolean = false,

    val dependencyData: String? = null,
    val formData: String? = null
)
