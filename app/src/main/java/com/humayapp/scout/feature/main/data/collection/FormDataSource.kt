package com.humayapp.scout.feature.main.data.collection

import android.util.Log
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.dto.CollectionFormDto
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.main.data.DatabaseViews
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlin.time.Instant

class FormNetworkDataSource(
    private val authRepository: AuthRepository,
    private val supabase: SupabaseClient
) {

    suspend fun getForms(updatedAfter: Instant?, limit: Long = -1L): List<CollectionFormDto> {
        val userId = authRepository.getCurrentUserId() ?: unreachable("can never be null. if null, then bug wahaha.")

        // Log.v(LOG_TAG, "[Fetch] Trying to fetch new forms from the server.")

        val query = supabase.from(DatabaseViews.FIELD_ACTIVITY_DETAILS).select() {
            filter {
                eq("collected_by->>id", userId)
                if (updatedAfter != null) {
                    gt("updated_at", updatedAfter.toString())
                }
            }
            order("updated_at", Order.ASCENDING)
            if (limit != -1L) {
                limit(count = limit)
            }
        }

        val result = query.decodeList<CollectionFormDto>()

//        if (result.size > 1) {
//            Log.i(LOG_TAG, "    Fetched ${result.size} forms successfully.")
//        } else {
//            Log.i(LOG_TAG, "    No new forms found.")
//        }

        return result
    }

    companion object {
        private const val LOG_TAG = "Scout: CollectionFormDataSource"
    }
}
