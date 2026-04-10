package com.humayapp.scout.feature.main.data.util

import com.humayapp.scout.core.network.util.SupabaseImageHelper
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageResolver(private val supabase: SupabaseClient) {
    suspend fun resolve(localImages: List<String>, remotePaths: List<String>): List<String> = withContext(Dispatchers.IO) {
        val local = localImages.mapNotNull { path ->
            val file = File(path)
            if (file.exists()) file.toURI().toString() else null
        }

        val remote = if (remotePaths.isNotEmpty()) {
            SupabaseImageHelper.generateSignedUrls(supabase, remotePaths)
        } else emptyList()

        local + remote
    }
}
