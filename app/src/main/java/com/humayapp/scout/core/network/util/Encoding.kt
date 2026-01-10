package com.humayapp.scout.core.network.util

import com.humayapp.scout.core.network.FieldActivities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

typealias JsonTransformRule = (key: String, value: String) -> String

fun transformField(fieldName: String, transform: (String) -> String): JsonTransformRule = { key, value ->
    if (key.equals(fieldName, ignoreCase = true)) transform(value) else value
}

fun Map<String, Any?>.asJson(rules: List<JsonTransformRule> = emptyList()): JsonObject =
    JsonObject(this.mapValues { (k, v) -> v.asJson(key = k, rules = rules) })

fun Any?.asJson(key: String? = null, rules: List<JsonTransformRule> = emptyList()): JsonElement = when (this) {
    is Map<*, *> -> JsonObject(
        this.mapNotNull { (k, v) ->
            val mapKey = k as? String ?: return@mapNotNull null
            mapKey to v.asJson(key = mapKey, rules = rules)
        }.toMap()
    )

    is List<*> -> JsonArray(this.map { it.asJson(rules = rules) })

    is String -> {
        val transformed = rules.fold(this) { acc, rule ->
            key?.let { rule(it, acc) } ?: acc
        }
        JsonPrimitive(transformed.toString())
    }

    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    null -> JsonNull
    else -> JsonPrimitive(this.toString())
}

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

