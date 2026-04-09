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
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.ScoutAuthState
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepository
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
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
    val collection_task_id: Int,
    val activity_type: String,
    val season_id: Int,
    val collected_by: String,
    val collected_at: String,
    val synced_at: String?,
    val image_urls: List<String>,
    val payload: JsonElement
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun fromEntry(entry: CollectionTask, imageUrls: List<String>): UploadDataRequest = UploadDataRequest(
            mfid = entry.mfid,
            collection_task_id = entry.id,
            activity_type = entry.activityType,
            season_id = entry.seasonId,
            collected_by = entry.collectedBy.toString(),
            collected_at = entry.collectedAt.toString(),
            synced_at = Clock.System.now().toString(),
            image_urls = imageUrls,
            payload = Json.parseToJsonElement(entry.formData!!)
        )
    }
}

@HiltWorker
class FormSyncWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val formRepository: FormRepository,
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor,
    private val collectionRepository: CollectionRepository,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        if (!isReadyToSync()) return@withContext Result.success()

        Log.i(LOG_TAG, "[Sync] Doing sync work.")
        return@withContext try {

            val pending = getPendingEntries()
            if (pending.isEmpty()) {
                Log.i(LOG_TAG, "[Sync] No pending forms found. Exiting sync.")
                return@withContext Result.success()
            }

            Log.i(LOG_TAG, "[Sync] Trying to sync ${pending.size} entries.")
            val hasError = syncEntries(pending)

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

    private suspend fun getPendingEntries(): List<CollectionTask> {
        val entryId = inputData.getInt("ENTRY_ID", -1)
        return if (entryId != -1) {
            listOfNotNull(collectionRepository.getCollectionTaskById(entryId))
        } else {
            collectionRepository.getUnsyncedTasks()
        }
    }

    private suspend fun syncEntries(entries: List<CollectionTask>): Boolean {
        var hasError = false
        for (entry in entries) {
            val success = syncEntry(entry)
            if (!success) hasError = true
        }
        return hasError
    }

    private suspend fun syncEntry(task: CollectionTask): Boolean {
        val formType = FormType.fromActivityType(task.activityType)
        val timestamp = Clock.System.now()

        Log.i(LOG_TAG, "[Sync] Starting sync for task MFID ${task.mfid}.")
        return try {
            val imageUrls = uploadImages(task, timestamp)
            val request = UploadDataRequest.fromEntry(task, imageUrls)
            supabase.postgrest.rpc("upload_form_data", mapOf("data" to Json.encodeToJsonElement(request)))
            Log.i(LOG_TAG, "[Sync] Upload successful for MFID ${task.mfid} - ${formType.label}.")
            markSynced(task.id, timestamp)
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Sync] Upload failed for MFID ${task.mfid} - ${formType.label}: ${e.message}.")
            false
        }
    }

    private suspend fun uploadImages(entry: CollectionTask, timestamp: Instant): List<String> {
        val images = collectionRepository.getImagesById(entry.id)
        val imageBucket = supabase.storage.from("form-images")
        return images.map { image ->
            val file = File(image.localPath)
            val remotePath = "${entry.seasonId}/${entry.activityType}/${entry.mfid}/$timestamp/${file.name}"
            imageBucket.upload(remotePath, file) { upsert = true }
            formRepository.updateImageRemotePath(image.id, remotePath)
            remotePath
        }
    }

    private suspend fun markSynced(entryId: Int, timestamp: Instant) {
        val rowsUpdated = collectionRepository.markSynced(entryId)
        if (rowsUpdated > 0) {
            Log.i(LOG_TAG, "[Sync] Local sync mark successful for entry $entryId.")
        } else {
            Log.e(LOG_TAG, "[Sync] Local sync mark FAILED for entry $entryId: no rows updated")
            throw Exception("Mark synced failed")
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
