package com.humayapp.scout.core.data.notification

import android.util.Log
import com.humayapp.scout.core.database.dao.NotificationDao
import com.humayapp.scout.feature.auth.data.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificationRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val dao: NotificationDao,
    private val authRepository: AuthRepository
) {
    private var pollingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                fetchNewNotifications()
                delay(10000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun fetchNewNotifications() {

        if (!authRepository.isOnline()) {
            Log.d(LOG_TAG, "Device offline, skipping fetch")
            return
        }
        val userId = authRepository.getCurrentUserId() ?: run {
            Log.e(LOG_TAG, "fetchNewNotifications: userId is null")
            return
        }

        val latestTimestamp = dao.getLatestCreatedAt(userId)
        Log.d(LOG_TAG, "fetchNewNotifications: userId=$userId, latestTimestamp=$latestTimestamp")

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

        Log.d(LOG_TAG, "Fetched ${newNotifications.size} new notifications")
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

    suspend fun markAllAsRead() {
        val userId = authRepository.getCurrentUserId() ?: return
        dao.markAllAsRead(userId)
    }

    companion object {
        private const val LOG_TAG = "Scout: NotificationRepository"
    }
}
