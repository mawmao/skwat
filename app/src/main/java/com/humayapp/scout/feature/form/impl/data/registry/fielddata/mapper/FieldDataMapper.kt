package com.humayapp.scout.feature.form.impl.data.registry.fielddata.mapper


import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.core.network.util.getSingleId
import com.humayapp.scout.core.network.util.upsert
import com.humayapp.scout.core.network.util.upsertAndGetId
import com.humayapp.scout.feature.form.impl.data.mapper.FormMapper
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisitPayload
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.Instant

// todo update form entry on farmer and field creation

object FieldDataMapper : FormMapper() {
    override suspend fun upload(entry: FormEntryEntity, client: SupabaseClient) {
        val payload = Json.decodeFromString<FieldDataPayload>(entry.payloadJson)

        val farmerId = client.upsertAndGetId(
            table = SupabaseDBTables.FARMERS,
            item = Farmers(
                firstName = payload.firstName,
                lastName = payload.lastName,
                gender = payload.gender,
                dateOfBirth = payload.dateOfBirth,
                cellPhoneNo = payload.cellPhoneNo
            ),
            onConflict = "first_name,last_name"
        )

        val barangayId = client.getSingleId("barangays") { filter { eq("name", payload.barangay) } }

        val mfidId = client.upsertAndGetId(
            table = "mfids",
            item = Mfids(
                mfid = entry.mfid,
                userAt = entry.syncedAt
            ),
            onConflict = "mfid"
        )

        val fieldId = client.upsertAndGetId(
            table = SupabaseDBTables.FIELDS,
            item = Fields(
                farmerId = farmerId,
                barangayId = barangayId,
                mfidId = mfidId,
                location = payload.location,
            ),
            onConflict = "mfid_id"
        )

        val parentId = upsertParent(
            fieldId = fieldId,
            entry = entry,
            client = client
        )

        client.upsert(
            table = SupabaseDBTables.FIELD_PLANNINGS,
            item = Json.encodeToJsonElement(
                FieldPlannings(
                    id = parentId,
                    landPreparationStartDate = payload.landPreparationStartDate,
                    estCropEstablishmentDate = payload.estCropEstablishmentDate,
                    estMethodOfEstablishment = payload.estMethodOfEstablishment,
                    totalFieldAreaHa = payload.totalFieldAreaHa,
                    soilType = payload.soilType,
                    currentFieldCondition = payload.currentFieldCondition,
                )
            )
        )

//        val monitoringVisitId = client.upsertAndGetId(
//            table = SupabaseDBTables.MONITORING_VISITS,
//            item = MonitoringVisits(
//                id = parentId,
//                dateMonitored = payload.dateMonitored,
//                cropStage = payload.cropStage,
//                soilMoistureStatus = payload.soilMoistureStatus,
//                avgPlantHeight = payload.avgPlantHeight
//            ),
//        )

        client.upsert(
            table = SupabaseDBTables.FIELD_ACTIVITIES,
            item = Json.encodeToJsonElement(
                mapOf(
                    "id" to parentId,
                    "monitoring_visit_id" to -1
                )
            ),
        )
    }
}

@Serializable
data class FieldDataPayload(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val gender: String,
    @SerialName("date_of_birth") val dateOfBirth: String,
    @SerialName("cellphone_no") val cellPhoneNo: String,
    @SerialName("land_preparation_start_date") val landPreparationStartDate: String,
    @SerialName("est_crop_establishment_date") val estCropEstablishmentDate: String,
    @SerialName("est_crop_establishment_method") val estMethodOfEstablishment: String? = null,
    @SerialName("total_field_area_ha") val totalFieldAreaHa: Double,
    @SerialName("soil_type") val soilType: String,
    @SerialName("current_field_condition") val currentFieldCondition: String,
    val province: String,
    @SerialName("municipality_or_city") val municipalityOrCity: String,
    val barangay: String,
    val location: String,

    /**
     * Hardcoded for now
     *
     * See [MonitoringVisitPayload]
     */
//    @SerialName("date_monitored") val dateMonitored: String,
//    @SerialName("crop_stage") val cropStage: String,
//    @SerialName("soil_moisture_status") val soilMoistureStatus: String,
//    @SerialName("avg_plant_height") val avgPlantHeight: String = "N/A"
)

@Serializable
private data class Farmers(
    val id: Int? = null, // used only for DB

    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val gender: String,
    @SerialName("date_of_birth") val dateOfBirth: String,
    @SerialName("cellphone_no") val cellPhoneNo: String,
)

@Serializable
private data class Fields(
    val id: Int? = null, // used only for DB

    @SerialName("farmer_id") val farmerId: Int? = null,
    @SerialName("barangay_id") val barangayId: Int? = null,
    @SerialName("mfid_id") val mfidId: Int? = null,
    val location: String,
)

@Serializable
private data class Mfids(
    val id: Int? = null, // used only for DB

    val mfid: String,
    @SerialName("used_at") val userAt: Instant? = null
)

@Serializable
private data class FieldPlannings(
    val id: Int? = null, // used only for DB
    @SerialName("land_preparation_start_date") val landPreparationStartDate: String,
    @SerialName("est_crop_establishment_date") val estCropEstablishmentDate: String,
    @SerialName("est_crop_establishment_method") val estMethodOfEstablishment: String? = null,
    @SerialName("total_field_area_ha") val totalFieldAreaHa: Double,
    @SerialName("soil_type") val soilType: String,
    @SerialName("current_field_condition") val currentFieldCondition: String,
)
