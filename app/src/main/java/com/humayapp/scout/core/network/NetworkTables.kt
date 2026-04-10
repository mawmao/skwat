package com.humayapp.scout.core.network

import kotlinx.datetime.LocalDate
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object SupabaseDBTables {
    const val FARMERS = "farmers"
    const val FIELDS = "fields"
    const val FIELD_ACTIVITIES = "field_activities"
    const val FIELD_PLANNINGS = "field_plannings"
    const val CROP_ESTABLISHMENTS = "crop_establishments"
    const val FERTILIZATION_RECORDS = "fertilization_records"
    const val FERTILIZER_APPLICATIONS = "fertilizer_applications"
    const val HARVEST_RECORDS = "harvest_records"
    const val MONITORING_VISITS = "monitoring_visits"
    const val DAMAGE_ASSESSMENTS = "damage_assessments"
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class FieldActivities(

    @SerialName("field_id")
    val fieldId: Int,

    @SerialName("season_id")
    val seasonId: Int,

    @SerialName("activity_type")
    val activityType: String,

    @SerialName("collected_by")
    val collectedBy: String,

    @SerialName("collected_at")
    val collectedAt: Instant,

    @SerialName("image_urls")
    @EncodeDefault
    val imageUrls: List<String> = emptyList(),

    @SerialName("synced_at")
    val syncedAt: Instant
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class CollectionTask(
    val id: Int,

    @SerialName("activity_id")
    val activityId: Int? = null,

    @SerialName("collector_id")
    val collectorId: Uuid,

    @SerialName("season_id")
    val seasonId: Int,

    @SerialName("mfid_id")
    val mfidId: Int,

    @SerialName("activity_type")
    val activityType: String,

    @SerialName("start_date")
    val startDate: LocalDate,

    @SerialName("end_date")
    val endDate: LocalDate,

    @SerialName("can_retake")
    val canRetake: Boolean = false,

    @SerialName("retake_of")
    val retakeOf: Int? = null,

    val status: String, // task status: pending / collected

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("updated_at")
    val updatedAt: Instant,

    @SerialName("assigned_at")
    val assignedAt: Instant? = null,

    val mfid: String,

    @SerialName("collector_name")
    val collectorName: String,

    @SerialName("farmer_name")
    val farmerName: String? = null,

    val province: String,

    @SerialName("city_municipality")
    val cityMunicipality: String,

    val barangay: String? = null,

    @SerialName("full_address")
    val fullAddress: String? = null,

    @SerialName("is_overdue")
    val isOverdue: Boolean,

    @SerialName("collected_by")
    val collectedBy: Uuid? = null,

    @SerialName("collected_at")
    val collectedAt: Instant? = null,

    val remarks: String? = null,

    @SerialName("image_urls")
    val imageUrls: List<String>? = null,

    @SerialName("dependency_data")
    val dependencyData: JsonElement? = null,
)

