package com.humayapp.scout.core.database.dto

import com.humayapp.scout.core.database.model.CollectionFormEntity
import com.humayapp.scout.core.database.util.toInstantSafe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
data class CollectionFormDto(

    @SerialName("id")
    val id: Int,

    @SerialName("collection_task_id")
    val collectionTaskId: Int,

    @SerialName("field_id")
    val fieldId: Int?,

    @SerialName("season_id")
    val seasonId: Int,

    @SerialName("season_year")
    val seasonYear: Int,

    @SerialName("semester")
    val semester: String,

    @SerialName("activity_type")
    val activityType: String,

    @SerialName("form_data")
    val formData: JsonElement,

    @SerialName("mfid")
    val mfid: String,

    @SerialName("farmer_name")
    val farmerName: String?,

    @SerialName("barangay")
    val barangay: String?,

    @SerialName("municipality")
    val municipality: String,

    @SerialName("province")
    val province: String,

    @SerialName("collected_by")
    val collectedBy: UserDetailsDto?,

    @SerialName("verified_by")
    val verifiedBy: UserDetailsDto?,

    @SerialName("remarks")
    val remarks: String?,

    @SerialName("verification_status")
    val verificationStatus: String?,

    @SerialName("collected_at")
    val collectedAt: String?,

    @SerialName("verified_at")
    val verifiedAt: String?,

    @SerialName("synced_at")
    val syncedAt: String?,

    @SerialName("image_urls")
    val imageUrls: List<String>?,

    @SerialName("is_retake")
    val isRetake: Boolean,

    @SerialName("original_activity_id")
    val originalActivityId: Int?,

    @SerialName("updated_at")
    val updatedAt: String
)

fun CollectionFormDto.toFormEntity(): CollectionFormEntity {
    return CollectionFormEntity(
        taskId = collectionTaskId,
        activityType = activityType,
        verificationStatus = verificationStatus ?: "pending",
        verifiedBy = verifiedBy?.id,
        verifiedAt = verifiedAt?.toInstantSafe(),
        updatedAt = updatedAt.toInstantSafe(),
        payload = Json.encodeToString(formData),
        synced = true,
    )
}
