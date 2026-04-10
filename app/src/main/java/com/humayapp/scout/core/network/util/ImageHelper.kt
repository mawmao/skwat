package com.humayapp.scout.core.network.util

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.hours

sealed class ImageSource {
    data class Local(val path: String) : ImageSource()
    data class Remote(val path: String) : ImageSource()
    data class Failed(val path: String, val error: String?) : ImageSource()
}

object SupabaseImageHelper {
    suspend fun generateSignedUrls(
        supabase: SupabaseClient,
        remotePaths: List<String>
    ): List<String> = coroutineScope {
        remotePaths.map { path ->
            async {
                try {
                    supabase.storage.from("form-images").createSignedUrl(path, 1.hours)
                } catch (e: Exception) {
                    Log.e("Scout: SupabaseImageHelper", "Failed to generate signed URL for $path", e)
                    path // fallback
                }
            }
        }.awaitAll()
    }
}