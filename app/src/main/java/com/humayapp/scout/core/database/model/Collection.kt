package com.humayapp.scout.core.database.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

data class TaskWithFormRelation(
    @Embedded val task: CollectionTaskEntity,
    @Relation(parentColumn = "id", entityColumn = "taskId")
    val form: CollectionFormEntity?
)

@Entity(tableName = "collection_tasks")
data class CollectionTaskEntity(
    @PrimaryKey val id: Int,
    val collectorId: String,
    val activityId: Int? = null,
    val retakeOf: Int? = null,

    val seasonId: Int,
    val seasonStartDate: LocalDate,
    val seasonEndDate: LocalDate,

    val mfidId: Int,
    val activityType: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,

    val status: String,

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

    // all fields from field_activity_details are to be removed
    val remarks: String? = null,
    val collectedBy: String? = null,
    val collectedAt: Instant? = null,
    val imageUrls: List<String>? = null,

    val createdAt: Instant,
    val updatedAt: Instant,

    val dependencyData: String? = null,
)

@Entity(
    tableName = "collection_forms",
    foreignKeys = [
        ForeignKey(
            entity = CollectionTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class CollectionFormEntity(
    @PrimaryKey val taskId: Int,
    val payload: String?,

    val verificationStatus: String, // PENDING, APPROVED, REJECTED
    val activityType: String,

    val verifiedBy: String? = null,
    val verifiedAt: Instant? = null,

    val updatedAt: Instant,
    val synced: Boolean = false
)


data class CollectionTaskUiModel(
    val id: Int,
    val collectorId: String,
    val activityId: Int?,
    val retakeOf: Int?,

    val seasonId: Int,
    val seasonStartDate: LocalDate,
    val seasonEndDate: LocalDate,

    val mfidId: Int,
    val activityType: String,

    val startDate: LocalDate,
    val endDate: LocalDate?,

    val status: String,

    val assignedAt: Instant?,

    val mfid: String,
    val collectorName: String,
    val farmerName: String?,

    val province: String,
    val cityMunicipality: String,
    val barangay: String?,
    val fullAddress: String?,

    val isOverdue: Boolean,
    val canRetake: Boolean,
    val isRetake: Boolean,

    val remarks: String?,
    val collectedBy: String?,
    val collectedAt: Instant?,
    val imageUrls: List<String>?,

    val createdAt: Instant,
    val updatedAt: Instant,

    val dependencyData: String?,

    val payload: String?,
    val verificationStatus: String?,
    val verifiedBy: String?, // uuid
    val verifiedAt: Instant?,
    val formUpdatedAt: Instant?,
    val synced: Boolean?
)

fun TaskWithFormRelation.toUiModel(): CollectionTaskUiModel = CollectionTaskUiModel(
    id = task.id,
    collectorId = task.collectorId,
    activityId = task.activityId,
    retakeOf = task.retakeOf,

    seasonId = task.seasonId,
    seasonStartDate = task.seasonStartDate,
    seasonEndDate = task.seasonEndDate,

    mfidId = task.mfidId,
    activityType = task.activityType,

    startDate = task.startDate,
    endDate = task.endDate,

    status = task.status,

    assignedAt = task.assignedAt,

    mfid = task.mfid,
    collectorName = task.collectorName,
    farmerName = task.farmerName,

    province = task.province,
    cityMunicipality = task.cityMunicipality,
    barangay = task.barangay,
    fullAddress = task.fullAddress,

    isOverdue = task.isOverdue,
    canRetake = task.canRetake,
    isRetake = task.retakeOf != null,

    remarks = task.remarks,
    collectedBy = task.collectedBy,
    collectedAt = task.collectedAt,
    imageUrls = task.imageUrls,

    createdAt = task.createdAt,
    updatedAt = task.updatedAt,

    dependencyData = task.dependencyData,

    payload = form?.payload,
    verificationStatus = form?.verificationStatus,
    verifiedBy = form?.verifiedBy,
    verifiedAt = form?.verifiedAt,
    formUpdatedAt = form?.updatedAt,
    synced = form?.synced
)

fun List<TaskWithFormRelation>.toUiModels(): List<CollectionTaskUiModel> =
    map { it.toUiModel() }