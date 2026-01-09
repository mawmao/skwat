package com.humayapp.scout.feature.form.impl.data.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun JsonElement.appendId(id: Int) = buildJsonObject {
    put("id", id)
    this@appendId.jsonObject.forEach { (k, v) -> put(k, v) }
}

fun JsonObject?.getInt(key: String): Int? {
    return this?.get("id")?.jsonPrimitive?.int
}
