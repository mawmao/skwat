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
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

typealias JsonTransformRule = (key: String, value: String) -> String

fun transformField(fieldName: String, transform: (String) -> String): JsonTransformRule = { key, value ->
    if (key.equals(fieldName, ignoreCase = true)) transform(value) else value
}

fun Map<String, Any?>.asJson(
    rules: List<JsonTransformRule> = emptyList(),
    includeKey: (String) -> Boolean = { true }
): JsonObject = JsonObject(
    this
        .filterKeys(includeKey)
        .mapValues { (k, v) -> v.asJson(key = k, rules = rules, includeKey = includeKey) }
)

fun Any?.asJson(
    key: String? = null,
    rules: List<JsonTransformRule> = emptyList(),
    includeKey: (String) -> Boolean = { true }
): JsonElement = when (this) {
    is Map<*, *> -> JsonObject(
        this.mapNotNull { (k, v) ->
            val mapKey = k as? String ?: return@mapNotNull null
            if (!includeKey(mapKey)) return@mapNotNull null
            mapKey to v.asJson(key = mapKey, rules = rules, includeKey = includeKey)
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


fun String.asFieldData(): Map<String, Any?> {

    val jsonElement = try {
        Json.parseToJsonElement(this)
    } catch (e: Exception) {
        return emptyMap()
    }

    fun JsonElement.toMap(): Map<String, Any?> {
        if (this !is JsonObject) return emptyMap()
        val map = mutableMapOf<String, Any?>()

        this.forEach { (key, value) ->
            map[key] = when (value) {
                is JsonPrimitive -> {
                    when {
                        value.isString -> value.content
                        value.booleanOrNull != null -> value.boolean
                        value.longOrNull != null -> value.long
                        value.doubleOrNull != null -> value.double
                        else -> null
                    }
                }

                is JsonObject -> value.toMap()
                is JsonArray -> value.map { it.jsonPrimitive.contentOrNull }
            }
        }

        return map
    }

    return jsonElement.toMap()
}

suspend fun Json.encodeFieldActivities(
    fieldId: Int,
    seasonId: Int,
    activityType: String,
    collectedBy: String,
    collectedAt: Instant,
    imageUrls: List<String>,
    syncedAt: Instant
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

