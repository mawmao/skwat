package com.humayapp.scout.core.network.util

import com.humayapp.scout.feature.form.impl.data.util.getInt
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.request.SelectRequestBuilder
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

fun <T> T?.throwOnNull(message: String): T {
    if (this == null) {
        throw IllegalStateException(message)
    }
    return this
}

inline fun <reified T : Any> PostgrestResult.decodeOrNull(): T? {
    return try {
        decodeSingle<T>()
    } catch (e: NoSuchElementException) {
        null
    }
}

// run on Dispatchers.IO
suspend fun SupabaseClient.getSingleId(
    table: String,
    block: SelectRequestBuilder.() -> Unit = {}
): Int {
    val arr = from(table)
        .select(Columns.list("id"), block)
        .decodeSingleOrNull<JsonObject>()

    return arr
        .getInt("id")
        .throwOnNull("No ID found in table '$table'")
}

suspend inline fun <reified T : Any> SupabaseClient.upsert(
    table: String,
    item: T,
    onConflict: String = "id"
) = this.from(table).upsert(item) { this.onConflict = onConflict }


suspend inline fun <reified T : Any> SupabaseClient.upsertAndGet(
    table: String,
    item: T,
    onConflict: String = "id"
): T = this.from(table).upsert(item) { this.onConflict = onConflict }.decodeSingle<T>()


suspend inline fun <reified T> SupabaseClient.upsertAndGetId(
    table: String,
    item: T,
    onConflict: String = "id"
): Int {
    val jsonElement = Json.encodeToJsonElement(item)
    val result = from(table)
        .upsert(JsonArray(listOf(jsonElement))) {
            this.onConflict = onConflict
            select()
        }
        .decodeSingle<JsonObject>()

    return result["id"]?.jsonPrimitive?.int ?: error("Missing ID after upsert in table $table")
}
