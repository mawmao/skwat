package com.humayapp.scout.feature.main.data.collection

import android.util.Log
import coil3.util.CoilUtils.result
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.dto.CollectionTaskDto
import com.humayapp.scout.core.database.dto.IdDto
import com.humayapp.scout.core.network.util.getLatestStartedSeasonId
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.main.data.DatabaseViews
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
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

//        val query = supabase.from(DatabaseViews.COLLECTION_DETAILS).select() {
//            filter {
//                eq("collector_id", userId)
//                if (updatedAfter != null) {
//                    gt("updated_at", updatedAfter.toString())
//                }
//            }
//            order("updated_at", Order.ASCENDING)
//            if (limit != -1L) {
//                limit(count = limit)
//            }
//        }
//
//        val result = query.decodeList<CollectionTaskDto>()

        val result = supabase.postgrest.rpc("get_available_tasks_for_collector", TaskParams(collectorId = userId))
            .decodeList<CollectionTaskDto>()

        Log.d(LOG_TAG, "[Task-DataSource] Data fetched $result")

        return result
    }

    companion object {
        private const val LOG_TAG = "Scout: CollectionTaskDataSource"

        @Serializable
        private data class TaskParams(
            @SerialName("p_collector_id")
            val collectorId: String,
        )
    }
}
