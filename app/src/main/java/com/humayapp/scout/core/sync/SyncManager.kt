package com.humayapp.scout.core.sync

import android.util.Log
import com.humayapp.scout.core.data.notification.NotificationRepository
import com.humayapp.scout.core.data.sync.SyncRepository
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.SyncQueueEntity
import com.humayapp.scout.core.database.model.SyncType
import com.humayapp.scout.core.database.model.TaskWithFormRelation
import com.humayapp.scout.core.sync.model.UploadDataRequest
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.ScoutAuthState
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.main.data.CollectionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.collections.map
import kotlin.collections.mapOf
import kotlin.time.Clock
import kotlin.time.Instant

class SyncManager(
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val collectionRepository: CollectionRepository,
    private val notificationRepository: NotificationRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val supabase: SupabaseClient
) {

    suspend fun syncNow() = syncOrchestrator.runSync {

        if (!networkMonitor.isOnline.first()) return@runSync
        if (!authRepository.isAuthReady.first { it }) return@runSync

        processPendingQueue()

        if (syncRepository.hasPendingQueue()) {
            Log.i(LOG_TAG, "[Sync] Queue not empty. Skipping pull.")
            return@runSync
        }

        collectionRepository.fullSync()
        notificationRepository.pullNotifications()
    }

    private suspend fun processPendingQueue() {
        val queue = syncRepository.getPendingQueue()
        for (item in queue) {
            try {
                processQueueItem(item)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "[Sync] queue item failed", e)
            }
        }
    }


    suspend fun isReadyToSync(): Boolean {
        Log.i(LOG_TAG, "[Sync] Checking if device can sync.")

        if (!networkMonitor.isOnline.first()) {
            Log.i(LOG_TAG, "    Device is offline. Skipping sync.")
            return false
        }
        if (!authRepository.isAuthReady.first { it }) {
            Log.i(LOG_TAG, "    Auth not ready. Skipping sync.")
            return false
        }

        val authState = authRepository.authState.first {
            it !is ScoutAuthState.Initializing
        }

        when (authState) {
            is ScoutAuthState.AuthenticatedOnline -> {
                Log.i(LOG_TAG, "    Authenticated online. Proceeding.")
                return true
            }

            is ScoutAuthState.AuthenticatedOffline -> {
                Log.i(LOG_TAG, "    Offline session. Skipping sync.")
                return false
            }

            is ScoutAuthState.SessionExpired -> {
                Log.i(LOG_TAG, "    Session expired. Skipping sync.")
                return false
            }

            else -> {
                Log.i(LOG_TAG, "    Not authenticated. Skipping sync.")
                return false
            }
        }
    }

    suspend fun processQueueItem(item: SyncQueueEntity): Boolean {
        return try {
            syncRepository.markInProgress(item.id)

            when (item.type) {
                SyncType.FORM_SUBMISSION -> {
                    handleFormSubmission(item)
                }

                SyncType.TASK_UPDATE -> {
                    // future
                }
            }

            syncRepository.markDone(item.id)
            true

        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Sync] Failed queue item ${item.id}", e)
            syncRepository.markFailed(item.id, e.message)
            false
        }
    }

    suspend fun handleFormSubmission(item: SyncQueueEntity) {
        val timestamp = Clock.System.now()

        val taskId = item.refId.toInt()
        val task = collectionRepository.getTaskById(taskId)
        val images = collectionRepository.getImagesById(taskId)

        val formType = FormType.fromActivityType(task.task.activityType)

        Log.i(LOG_TAG, "[Sync] Starting sync for task MFID ${task.task.mfid}.")

        val imageUrls = uploadImages(task, images, timestamp)

        val request = UploadDataRequest.fromQueue(
            task = task,
            imageUrls = imageUrls,
            payload = item.payload
        )

        supabase.postgrest.rpc(
            function = "upload_form_data",
            parameters = mapOf("data" to Json.encodeToJsonElement(request))
        )

        collectionRepository.markFormAsSynced(taskId)
        Log.i(LOG_TAG, "[Sync] Upload successful for MFID ${task.task.mfid} - ${formType.label}.")
    }

    suspend fun uploadImages(
        task: TaskWithFormRelation,
        images: List<FormImageEntity>,
        timestamp: Instant
    ): List<String> {
        val imageBucket = supabase.storage.from("form-images")
        return images.map { image ->
            val file = File(image.localPath)
            val remotePath = "${task.task.seasonId}/${task.task.activityType}/${task.task.mfid}/$timestamp/${file.name}"
            imageBucket.upload(remotePath, file) { upsert = true }
            collectionRepository.updateImageRemotePath(image.id, remotePath)
            remotePath
        }
    }

    companion object {
        private const val LOG_TAG = "Scout: SyncManager"
    }
}
