package com.humayapp.scout.core.network.service

import com.humayapp.scout.feature.form.api.FormType
import io.github.jan.supabase.SupabaseClient
import jakarta.inject.Inject

class SupabaseService @Inject constructor(private val client: SupabaseClient) {
//    suspend fun uploadForm(entry: FormEntryEntity) {
//        FormType.fromActivityType(entry.activityType)
//            .mapper
//            .upload(entry, client)
//    }
}

