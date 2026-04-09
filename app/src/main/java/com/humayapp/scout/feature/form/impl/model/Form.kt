package com.humayapp.scout.feature.form.impl.model

import com.humayapp.scout.feature.auth.data.ScoutUser
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

val formDataJson = Json {
    ignoreUnknownKeys = true
}

@Serializable
sealed class FormData

interface Monitorable {
}

@Serializable
data class MonitoringVisit(
    @SerialName("date_monitored") val dateMonitored: String,
    @SerialName("crop_stage") val cropStage: String,
    @SerialName("soil_moisture_status") val soilMoistureStatus: String,
    @SerialName("avg_plant_height") val avgPlantHeight: Double? = null,
)

@Serializable
data class FieldDataForm(
    @SerialName("land_preparation_start_date") val landPreparationStartDate: String,
    @SerialName("est_crop_establishment_date") val estCropEstablishmentDate: String,
    @SerialName("est_crop_establishment_method") val estCropEstablishmentMethod: String,
    @SerialName("total_field_area_ha") val totalFieldAreaHa: Double,
    @SerialName("soil_type") val soilType: String?,
    @SerialName("current_field_condition") val currentFieldCondition: String,
    @SerialName("monitoring_visit") val monitoringVisit: MonitoringVisit? = null,
) : FormData()

@Serializable
data class CulturalManagementForm(
    @SerialName("ecosystem") val ecosystem: String,
    @SerialName("monitoring_field_area_sqm") val monitoringFieldAreaSqm: Double,
    @SerialName("actual_crop_establishment_date") val actualCropEstablishmentDate: LocalDate,
    @SerialName("actual_crop_establishment_method") val actualCropEstablishmentMethod: String,
    @SerialName("sowing_date") val sowingDate: LocalDate?,
    @SerialName("seedling_age_at_transplanting") val seedlingAgeAtTransplanting: Int?,
    @SerialName("distance_between_plant_row_1") val distanceBetweenPlantRow1: Double?,
    @SerialName("distance_between_plant_row_2") val distanceBetweenPlantRow2: Double?,
    @SerialName("distance_between_plant_row_3") val distanceBetweenPlantRow3: Double?,
    @SerialName("distance_within_plant_row_1") val distanceWithinPlantRow1: Double?,
    @SerialName("distance_within_plant_row_2") val distanceWithinPlantRow2: Double?,
    @SerialName("distance_within_plant_row_3") val distanceWithinPlantRow3: Double?,
    @SerialName("seeding_rate_kg_ha") val seedingRateKgHa: Double?,
    @SerialName("direct_seeding_method") val directSeedingMethod: String?,
    @SerialName("num_plants_1") val numPlants1: Int?,
    @SerialName("num_plants_2") val numPlants2: Int?,
    @SerialName("num_plants_3") val numPlants3: Int?,
    @SerialName("rice_variety") val riceVariety: String,
    @SerialName("rice_variety_no") val riceVarietyNo: String?,
    @SerialName("rice_variety_maturity_duration") val riceVarietyMaturityDuration: Int,
    @SerialName("seed_class") val seedClass: String,
    @SerialName("monitoring_visit") val monitoringVisit: MonitoringVisit?,
) : FormData()

@Serializable
data class NutrientManagementForm(
    @SerialName("id") val id: Int,
    @SerialName("applied_area_sqm") val appliedAreaSqm: Double,
    @SerialName("applications") val applications: List<FertilizerApplication>,
    @SerialName("monitoring_visit") val monitoringVisit: MonitoringVisit?,
) : FormData(), Monitorable

@Serializable
data class FertilizerApplication(
    @SerialName("fertilizer_type") val fertilizerType: String,
    @SerialName("brand") val brand: String,
    @SerialName("nitrogen_content_pct") val nitrogenContentPct: Double,
    @SerialName("phosphorus_content_pct") val phosphorusContentPct: Double,
    @SerialName("potassium_content_pct") val potassiumContentPct: Double,
    @SerialName("amount_applied") val amountApplied: Double,
    @SerialName("amount_unit") val amountUnit: String,
    @SerialName("crop_stage_on_application") val cropStageOnApplication: String,
)

@Serializable
data class ProductionForm(
    @SerialName("harvest_date") val harvestDate: LocalDate,
    @SerialName("harvesting_method") val harvestingMethod: String,
    @SerialName("bags_harvested") val bagsHarvested: Int,
    @SerialName("avg_bag_weight_kg") val avgBagWeightKg: Double,
    @SerialName("area_harvested_ha") val areaHarvestedHa: Double,
    @SerialName("irrigation_supply") val irrigationSupply: String,
    @SerialName("monitoring_visit") val monitoringVisit: MonitoringVisit?,
) : FormData()

@Serializable
data class DamageAssessmentForm(
    @SerialName("cause") val cause: String,
    @SerialName("crop_stage") val cropStage: String,
    @SerialName("soil_type") val soilType: String,
    @SerialName("severity") val severity: String,
    @SerialName("affected_area_ha") val affectedAreaHa: Double,
    @SerialName("observed_pest") val observedPest: String?,
) : FormData()

@Serializable
data class FieldActivityDetails(
    @SerialName("id") val id: Int? = null,
    @SerialName("mfid") val mfid: String,
    @SerialName("season_year") val seasonYear: String? = null,
    @SerialName("collection_task_id") val collectionTaskId: Int? = null,
    @SerialName("semester") val semester: String? = null,
    @SerialName("field_id") val fieldId: Int? = null,
    @SerialName("season_id") val seasonId: Int,
    @SerialName("activity_type") val activityType: String,
    @SerialName("collected_by") val collectedBy: ScoutUser? = null,
    @SerialName("verified_by") val verifiedBy: ScoutUser? = null,
    @SerialName("remarks") val remarks: String? = null,
    @SerialName("verification_status") val verificationStatus: String? = null,
    @SerialName("collected_at") val collectedAt: Instant?,
    @SerialName("verified_at") val verifiedAt: Instant? = null,
    @SerialName("synced_at") val syncedAt: Instant? = null,
    @SerialName("image_urls") val imageUrls: List<String>? = null,
    @SerialName("farmer_name") val farmerName: String,
    @SerialName("barangay") val barangay: String,
    @SerialName("municipality") val municipality: String,
    @SerialName("province") val province: String,
    @SerialName("is_retake") val isRetake: Boolean,
    @SerialName("original_activity_id") val originalActivityId: Int? = null,
    @SerialName("form_data") val formData: JsonElement,
)
