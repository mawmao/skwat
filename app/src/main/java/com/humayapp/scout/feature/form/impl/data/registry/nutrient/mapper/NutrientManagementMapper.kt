package com.humayapp.scout.feature.form.impl.data.registry.nutrient.mapper

import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.core.network.util.upsert
import com.humayapp.scout.core.network.util.upsertAndGetId
import com.humayapp.scout.feature.form.impl.data.mapper.FormMapper
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

object NutrientManagementMapper : FormMapper() {
    override suspend fun upload(entry: FormEntryEntity, client: SupabaseClient) {
        val payload = Json.decodeFromString<FertilizationRecord>(entry.payloadJson)
        val parentId = upsertParent(entry, client)
        val detailParentId = client.upsertAndGetId(
            table = SupabaseDBTables.FERTILIZATION_RECORDS,
            item = Json.encodeToJsonElement(
                FertilizationRecord(
                    id = parentId,
                    appliedAreaSqM = payload.appliedAreaSqM
                )
            ),
            onConflict = "id"
        )

        payload.fertilizerApplication?.forEach { payload ->
            client.upsert(
                table = SupabaseDBTables.FERTILIZER_APPLICATIONS,
                item = Json.encodeToJsonElement(
                    FertilizerApplications(
                        fertilizationRecordId = detailParentId,
                        fertilizerType = payload.fertilizerType,
                        brandName = payload.brandName,
                        nitrogenContentPercent = payload.nitrogenContentPercent,
                        phosphorusContentPercent = payload.phosphorusContentPercent,
                        potassiumContentPercent = payload.potassiumContentPercent,
                        amountApplied = payload.amountApplied,
                        amountUnit = payload.amountUnit,
                        cropStageOnApplication = payload.cropStageOnApplication,
                    )
                ),
                onConflict = "id"
            )
        }
    }
}

@Serializable
private data class FertilizationRecord(
    val id: Int? = null, // used only for DB
    @SerialName("applied_area_sqm") val appliedAreaSqM: Double,
    @SerialName("fertilizer_application") val fertilizerApplication: List<FertilizerApplications>? = null
)

@Serializable
private data class FertilizerApplications(
    @SerialName("fertilization_record_id") val fertilizationRecordId: Int? = null,
    @SerialName("fertilizer_type") val fertilizerType: String,
    @SerialName("brand") val brandName: String,
    @SerialName("nitrogen_content_pct") val nitrogenContentPercent: Double,
    @SerialName("phosphorus_content_pct") val phosphorusContentPercent: Double,
    @SerialName("potassium_content_pct") val potassiumContentPercent: Double,
    @SerialName("amount_applied") val amountApplied: Double,
    @SerialName("amount_unit") val amountUnit: String,
    @SerialName("crop_stage_on_application") val cropStageOnApplication: String
)
