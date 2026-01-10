package com.humayapp.scout.feature.form.impl.data.mapper

import android.util.Log
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.core.network.util.encodeFieldActivities
import com.humayapp.scout.core.network.util.getSingleId
import com.humayapp.scout.core.network.util.upsert
import com.humayapp.scout.core.network.util.upsertAndGetId
import com.humayapp.scout.feature.form.impl.data.util.appendId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.time.OffsetDateTime

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

    protected suspend fun upsertParent(entry: FormEntryEntity, client: SupabaseClient): Int {
        val parentData = buildParentData(client = client, entry = entry)
        val parentId = client.upsertAndGetId(
            table = SupabaseDBTables.FIELD_ACTIVITIES,
            item = parentData,
            onConflict = "field_id, season_id, activity_type"
        )
        return parentId
    }

    protected suspend fun buildParentData(
        client: SupabaseClient,
        entry: FormEntryEntity,
    ): JsonElement {
        return withContext(Dispatchers.IO) {

            Log.v(LOG_TAG, "Fetching field ID for MFID ${entry.mfid}")
            val fieldId = client.getSingleId("fields") { filter { eq("mfid", entry.mfid) } }

            Log.v(LOG_TAG, "Fetching current season's ID")
            val seasonId = client.getSingleId("seasons") { order("id", Order.DESCENDING) }

            val fieldActivitiesJson =  Json.encodeFieldActivities(
                fieldId = fieldId,
                seasonId = seasonId,
                activityType = entry.activityType,
                collectedBy = entry.collectedBy,
                collectedAt = entry.collectedAt,
                imageUrls = emptyList(),
                syncedAt = OffsetDateTime.now().toString()
            )

            return@withContext fieldActivitiesJson
        }
    }

    companion object {
        private const val LOG_TAG = "Scout: FormMapper"
    }
}

