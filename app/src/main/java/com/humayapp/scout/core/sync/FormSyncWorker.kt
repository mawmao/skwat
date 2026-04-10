package com.humayapp.scout.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.humayapp.scout.core.common.dispatcher.Dispatcher
import com.humayapp.scout.core.common.dispatcher.ScoutDispatchers
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.data.sync.SyncRepository
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.SyncQueueEntity
import com.humayapp.scout.core.database.model.SyncType
import com.humayapp.scout.core.database.model.TaskWithFormRelation
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.ScoutAuthState
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.main.data.CollectionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class UploadDataRequest(
    val mfid: String,

    @SerialName("collection_task_id")
    val collectionTaskId: Int,

    @SerialName("activity_type")
    val activityType: String,

    @SerialName("season_id")
    val seasonId: Int,

    @SerialName("collected_by")
    val collectedBy: String,

    @SerialName("collected_at")
    val collectedAt: String,

    @SerialName("synced_at")
    val syncedAt: String?,

    @SerialName("image_urls")
    val imageUrls: List<String>,

    val payload: JsonElement
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun fromQueue(task: TaskWithFormRelation, imageUrls: List<String>, payload: String?): UploadDataRequest =
            UploadDataRequest(
                mfid = task.task.mfid,
                collectionTaskId = task.task.id,
                activityType = task.task.activityType,
                seasonId = task.task.seasonId,
                collectedBy = task.task.collectedBy.toString(),
                collectedAt = task.task.collectedAt.toString(),
                syncedAt = Clock.System.now().toString(),
                imageUrls = imageUrls,
                payload = Json.parseToJsonElement(payload ?: unreachable("payload in this context must be available"))
            )
    }
}

@HiltWorker
class FormSyncWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor,
    private val syncRepository: SyncRepository,
    private val collectionRepository: CollectionRepository,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        if (!isReadyToSync()) return@withContext Result.success()

        Log.i(LOG_TAG, "[Sync] Doing sync work.")

        return@withContext try {
            val queue = syncRepository.getPendingQueue()

            if (queue.isEmpty()) {
                Log.i(LOG_TAG, "[Sync] No pending forms found. Exiting sync.")
                return@withContext Result.success()
            }

            var hasError = false
            Log.i(LOG_TAG, "[Sync] Trying to sync ${queue.size} entries.")

            for (item in queue) {
                val success = processQueueItem(item)
                if (!success) hasError = true
            }

            if (hasError) {
                Log.w(LOG_TAG, "[Sync] Partial sync failure. Requesting retry.")
                Result.retry()
            } else {
                Log.i(LOG_TAG, "[Sync] All entries synced successfully.")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Sync] Fatal sync error", e)
            Result.failure()
        }
    }

    private suspend fun isReadyToSync(): Boolean {
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

    private suspend fun processQueueItem(item: SyncQueueEntity): Boolean {
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

    private suspend fun handleFormSubmission(item: SyncQueueEntity) {
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

    private suspend fun uploadImages(
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
        private const val LOG_TAG = "Scout: Sync"
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .build()

        fun start(context: Context) {
            Log.i(LOG_TAG, "[Sync] Starting form sync worker.")

            val request = OneTimeWorkRequestBuilder<FormSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(SyncConstraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "FormSyncWork",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
