package com.humayapp.scout.feature.form.impl.data.sync

import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.feature.form.api.FormType
import io.github.jan.supabase.SupabaseClient

suspend fun SupabaseClient.uploadForm(entry: FormEntryEntity) =
    FormType.fromActivityType(entry.activityType).mapper.upload(entry, this)