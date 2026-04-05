package com.humayapp.scout.core.network.util

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.hours

object SupabaseImageHelper {
    suspend fun generateSignedUrls(supabase: SupabaseClient, remotePaths: List<String>): List<String> {
        return remotePaths.map { remotePath ->
            try {
                supabase.storage.from("form-images")
                    .createSignedUrl(remotePath, 1.hours)
            } catch (e: Exception) {
                Log.e("Scout: ImageHelper", "Failed to generate signed URL for $remotePath", e)
                remotePath // fallback to raw path
            }
        }
    }
}