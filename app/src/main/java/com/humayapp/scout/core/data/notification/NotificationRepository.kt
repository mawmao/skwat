package com.humayapp.scout.core.data.notification

import android.util.Log
import com.humayapp.scout.core.database.dao.NotificationDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val dao: NotificationDao,
) {

    suspend fun pullNotifications(userId: String) {
        val latestTimestamp = dao.getLatestCreatedAt(userId)
        val query = supabase.from("notifications").select {
            filter {
                and {
                    eq("user_id", userId)
                    eq("target_role", "data_collector")
                }
                if (latestTimestamp != null) {
                    gt("created_at", latestTimestamp.toString())
                }
            }
            order(column = "created_at", order = Order.ASCENDING)
        }

        val newNotifications = try {
            query.decodeList<Notification>()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error decoding notifications", e)
            emptyList()
        }

        if (newNotifications.isNotEmpty()) {
            val entities = newNotifications.map { notif ->
                notif.copy(userId = userId).toEntity()
            }
            dao.insertAll(entities)
        }
    }

    fun getLocalNotifications(userId: String): Flow<List<Notification>> =
        dao.getNotifications(userId).map { list -> list.map { it.toDomain() } }

    suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
        // no remote update – role‑based notifications are not user‑owned
    }

    suspend fun markAllAsRead(userId: String) {
        dao.markAllAsRead(userId)
    }

    companion object {
        private const val LOG_TAG = "Scout: NotificationRepository"
    }
}
