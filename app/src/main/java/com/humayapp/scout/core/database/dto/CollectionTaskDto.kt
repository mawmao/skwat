package com.humayapp.scout.core.database.dto

import com.humayapp.scout.core.database.model.CollectionTaskEntity
import com.humayapp.scout.core.database.util.json
import com.humayapp.scout.core.database.util.toInstantSafe
import com.humayapp.scout.core.database.util.toLocalDateSafe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CollectionTaskDto(

    @SerialName("id")
    val id: Int,

    @SerialName("season_id")
    val seasonId: Int,

    @SerialName("season_start_date")
    val seasonStartDate: String,

    @SerialName("season_end_date")
    val seasonEndDate: String,

    @SerialName("mfid_id")
    val mfidId: Int,

    @SerialName("collector_id")
    val collectorId: String,

    @SerialName("activity_type")
    val activityType: String,

    @SerialName("start_date")
    val startDate: String,

    @SerialName("end_date")
    val endDate: String?,

    @SerialName("status")
    val status: String,

    @SerialName("assigned_at")
    val assignedAt: String?,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("mfid")
    val mfid: String,

    @SerialName("collector_name")
    val collectorName: String,

    @SerialName("farmer_name")
    val farmerName: String?,

    @SerialName("city_municipality")
    val cityMunicipality: String,

    @SerialName("province")
    val province: String,

    @SerialName("barangay")
    val barangay: String?,

    @SerialName("full_address")
    val fullAddress: String?,

    @SerialName("verification_status")
    val verificationStatus: String?,

    @SerialName("activity_id")
    val activityId: Int?,

    @SerialName("collected_by")
    val collectedBy: String?,

    @SerialName("collected_at")
    val collectedAt: String?,

    @SerialName("verified_by")
    val verifiedBy: String?,

    @SerialName("verified_at")
    val verifiedAt: String?,

    @SerialName("remarks")
    val remarks: String?,

    @SerialName("image_urls")
    val imageUrls: List<String>?,

    @SerialName("dependency_data")
    val dependencyData: JsonElement?,

    @SerialName("is_overdue")
    val isOverdue: Boolean,

    @SerialName("can_retake")
    val canRetake: Boolean,

    @SerialName("retake_of")
    val retakeOf: Int?,
)


fun CollectionTaskDto.toTaskEntity(): CollectionTaskEntity {
    return CollectionTaskEntity(
        id = id,
        collectorId = collectorId,
        activityId = activityId,
        retakeOf = retakeOf,

        seasonId = seasonId,
        seasonStartDate = seasonStartDate.toLocalDateSafe(),
        seasonEndDate = seasonEndDate.toLocalDateSafe(),

        mfidId = mfidId,

        activityType = activityType,

        startDate = startDate.toLocalDateSafe(),
        endDate = endDate?.toLocalDateSafe(),

        status = status,

        assignedAt = assignedAt?.toInstantSafe(),

        mfid = mfid,
        collectorName = collectorName,
        farmerName = farmerName,

        province = province,
        cityMunicipality = cityMunicipality,
        barangay = barangay,
        fullAddress = fullAddress,

        isOverdue = isOverdue,
        canRetake = canRetake,

        remarks = remarks,
        collectedBy = collectedBy,
        collectedAt = collectedAt?.toInstantSafe(),

        imageUrls = imageUrls,

        createdAt = createdAt.toInstantSafe(),
        updatedAt = updatedAt.toInstantSafe(),

        dependencyData = dependencyData?.let { json.encodeToString(it) }
    )
}
