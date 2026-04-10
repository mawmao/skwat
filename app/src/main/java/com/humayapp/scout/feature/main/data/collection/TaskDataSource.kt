package com.humayapp.scout.feature.main.data.collection

import android.util.Log
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.dto.CollectionTaskDto
import com.humayapp.scout.core.database.dto.IdDto
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.main.data.DatabaseViews
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlin.time.Instant

class TaskNetworkDataSource(
    private val authRepository: AuthRepository,
    private val supabase: SupabaseClient
) {
    suspend fun getAllTaskIds(): List<Int> {
        val userId = authRepository.getCurrentUserId() ?: unreachable("can never be null. if null, then bug wahaha.")
        val query = supabase.from(DatabaseViews.COLLECTION_DETAILS).select(columns = Columns.list("id")) {
            filter {
                eq("collector_id", userId)
            }
        }
        return query.decodeList<IdDto>().map { it.id }
    }

    suspend fun getTasks(updatedAfter: Instant?, limit: Long = -1L): List<CollectionTaskDto> {
        val userId = authRepository.getCurrentUserId() ?: unreachable("can never be null. if null, then bug wahaha.")

        Log.v(LOG_TAG, "[Fetch] Trying to fetch new tasks from the server.")

        val query = supabase.from(DatabaseViews.COLLECTION_DETAILS).select() {
            filter {
                eq("collector_id", userId)
                if (updatedAfter != null) {
                    gt("updated_at", updatedAfter.toString())
                }
            }
            order("updated_at", Order.ASCENDING)
            if (limit != -1L) {
                limit(count = limit)
            }
        }

        val result = query.decodeList<CollectionTaskDto>()

        if (result.size > 1) {
            Log.i(LOG_TAG, "    Fetched ${result.size} tasks successfully.")
        } else {
            Log.i(LOG_TAG, "    No new tasks found.")
        }

        return result
    }

    companion object {
        private const val LOG_TAG = "Scout: CollectionTaskDataSource"
    }
}