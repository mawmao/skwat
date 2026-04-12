package com.humayapp.scout.core.data.notification

import android.util.Log
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.dao.NotificationDao
import com.humayapp.scout.feature.auth.data.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    private val dao: NotificationDao,
) {
    suspend fun pullNotifications() {
        val userId = authRepository.getCurrentUserId() ?: unreachable("can never be null. if null, then bug wahaha.")
        val query = supabase.from("notifications").select {
            filter {
                and {
                    eq("user_id", userId)
                    eq("target_role", "data_collector")
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
            try {
                dao.insertAll(entities)
                Log.d(LOG_TAG, "Inserted ${entities.size} notifications into database")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to insert notifications", e)
            }
        }
    }

    fun getLocalNotifications(userId: String): Flow<List<Notification>> =
        dao.getNotifications(userId)
            .map { entities ->
                Log.d(LOG_TAG, "Room emitted ${entities.size} entities for user $userId")
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to convert entity ${entity.id}", e)
                        null
                    }
                }
            }

    suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
    }

    suspend fun markAllAsRead(userId: String) {
        dao.markAllAsRead(userId)
    }

    companion object {
        private const val LOG_TAG = "Scout: NotificationRepository"
    }
}
