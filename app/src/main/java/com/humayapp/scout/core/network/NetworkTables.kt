package com.humayapp.scout.core.network

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val collectedAt: String,

    @SerialName("image_urls")
    @EncodeDefault
    val imageUrls: List<String> = emptyList(),

    @SerialName("synced_at")
    val syncedAt: String
)

