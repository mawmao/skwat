package com.humayapp.scout.feature.form.impl.data.mapper

import android.util.Log
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.core.network.util.encodeFieldActivities
import com.humayapp.scout.core.network.util.getFieldIdByMfid
import com.humayapp.scout.core.network.util.getLatestStartedSeasonId
import com.humayapp.scout.core.network.util.getPlantingSeasonIdForHarvest
import com.humayapp.scout.core.network.util.getSeasonIdByDate
import com.humayapp.scout.core.network.util.upsert
import com.humayapp.scout.core.network.util.upsertAndGetId
import com.humayapp.scout.feature.form.impl.data.util.appendId
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive



@Serializable
abstract class FormMapper {

    abstract suspend fun upload(entry: FormEntryEntity, client: SupabaseClient)

    protected suspend fun defaultMapping(
        table: String,
        entry: FormEntryEntity,
        client: SupabaseClient
    ) {
        val parentId = upsertParent(entry, client)
        val payload = Json
            .parseToJsonElement(entry.payloadJson)
            .appendId(parentId)

        client.upsert(
            table = table,
            item = Json.encodeToJsonElement(payload),
            onConflict = "id"
        )
    }

    protected suspend fun upsertParent(
        entry: FormEntryEntity,
        client: SupabaseClient,
        fieldId: Int? = null
    ): Int {
        val parentData = buildParentData(client = client, entry = entry, fieldId = fieldId)

        return try {
            val id = client.upsertAndGetId(
                table = SupabaseDBTables.FIELD_ACTIVITIES,
                item = parentData,
            )
            id
        } catch (e: Exception) {
            Log.e(LOG_TAG, "upsertAndGetId failed for mfid=${entry.mfid}. ParentData: $parentData", e)
            throw e
        }
    }

    protected suspend fun buildParentData(
        client: SupabaseClient,
        entry: FormEntryEntity,
        fieldId: Int? = null,
    ): JsonElement {
        return withContext(Dispatchers.IO) {
            val resolvedFieldId = fieldId ?: run { client.getFieldIdByMfid(entry.mfid) }
            val seasonId = when (entry.activityType) {
                "production" -> {
                    val payload = Json.parseToJsonElement(entry.payloadJson)
                    val harvestDate = payload.jsonObject["harvest_date"]?.jsonPrimitive?.content
                    if (harvestDate != null) {
                        Log.d(
                            LOG_TAG,
                            "Production record for MFID ${entry.mfid} (field $resolvedFieldId) has harvest date: $harvestDate"
                        )
                        val plantingSeason = client.getPlantingSeasonIdForHarvest(resolvedFieldId, harvestDate)
                        if (plantingSeason != null) {
                            Log.d(LOG_TAG, "Found planting season $plantingSeason for harvest date $harvestDate")
                            plantingSeason
                        } else {
                            Log.w(
                                LOG_TAG,
                                "No planting season found for harvest date $harvestDate, falling back to latest started season"
                            )
                            client.getLatestStartedSeasonId()
                        }
                    } else {
                        Log.w(
                            LOG_TAG,
                            "Production record for MFID ${entry.mfid} missing harvest_date, using latest started season"
                        )
                        client.getLatestStartedSeasonId()
                    }
                }

                "nutrient-management" -> {
                    val payload = Json.parseToJsonElement(entry.payloadJson)
                    val applicationDate = payload.jsonObject["application_date"]?.jsonPrimitive?.content
                    if (applicationDate != null) {
                        Log.d(
                            LOG_TAG,
                            "Nutrient management record for MFID ${entry.mfid} (field $resolvedFieldId) has application date: $applicationDate"
                        )
                        client.getSeasonIdByDate(applicationDate) ?: run {
                            Log.w(
                                LOG_TAG,
                                "No season found for application date $applicationDate, falling back to latest started season"
                            )
                            client.getLatestStartedSeasonId()
                        }
                    } else {
                        Log.w(
                            LOG_TAG,
                            "Nutrient management record for MFID ${entry.mfid} missing application_date, using latest started season"
                        )
                        client.getLatestStartedSeasonId()
                    }
                }

                else -> client.getLatestStartedSeasonId()
            }

            Log.d(LOG_TAG, "Final season ID for MFID ${entry.mfid}: $seasonId")

            Json.encodeFieldActivities(
                fieldId = resolvedFieldId,
                seasonId = seasonId,
                activityType = entry.activityType,
                collectedBy = entry.collectedBy,
                collectedAt = entry.collectedAt,
                imageUrls = entry.imageUrls,
                syncedAt = entry.syncedAt ?: unreachable("synced at is non-null at this point")
            )
        }
    }

    companion object {
        const val LOG_TAG = "Scout: FormMapper"
    }
}

