package com.humayapp.scout.core.network.util

import com.humayapp.scout.core.network.FieldActivities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

suspend fun Json.encodeFieldActivities(
    fieldId: Int,
    seasonId: Int,
    activityType: String,
    collectedBy: String,
    collectedAt: String,
    imageUrls: List<String>,
    syncedAt: String
): JsonElement = withContext(Dispatchers.Default) {
    this@encodeFieldActivities.encodeToJsonElement(
        FieldActivities(
            fieldId = fieldId,
            seasonId = seasonId,
            activityType = activityType,
            collectedBy = collectedBy,
            collectedAt = collectedAt,
            imageUrls = imageUrls,
            syncedAt = syncedAt
        )
    )
}

