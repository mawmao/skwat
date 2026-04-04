package com.humayapp.scout.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.humayapp.scout.core.common.dispatcher.Dispatcher
import com.humayapp.scout.core.common.dispatcher.ScoutDispatchers
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.SyncStatus
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.form.api.FormType
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
        fun fromEntry(entry: FormEntryEntity, imageUrls: List<String>): UploadDataRequest = UploadDataRequest(
            mfid = entry.mfid,
            collection_task_id = entry.collectionTaskId,
            activity_type = entry.activityType,
            season_id = entry.seasonId,
            collected_by = entry.collectedBy,
            collected_at = entry.collectedAt.toString(),
            synced_at = entry.syncedAt?.toString(),
            image_urls = imageUrls,
            payload = Json.parseToJsonElement(entry.payloadJson)
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
        if (!authRepository.isAuthenticated()) {
            Log.i(LOG_TAG, "[Sync] No active session. Skipping sync.")
            return false
        }
        if (!networkMonitor.isOnline.first()) {
            Log.i(LOG_TAG, "[Sync] Device is offline. Doing nothing.")
            return false
        }
        return true
    }

    private suspend fun getPendingEntries(): List<FormEntryEntity> {
        val entryId = inputData.getLong("ENTRY_ID", -1L)
        return if (entryId != -1L) {
            listOfNotNull(formRepository.getEntryById(entryId))
        } else {
            formRepository.getPendingSyncOnce()
        }
    }

    private suspend fun syncEntries(entries: List<FormEntryEntity>): Boolean {
        var hasError = false
        for (entry in entries) {
            val success = syncEntry(entry)
            if (!success) hasError = true
        }
        return hasError
    }

    private suspend fun syncEntry(entry: FormEntryEntity): Boolean {
        val formType = FormType.fromActivityType(entry.activityType)
        val timestamp = Clock.System.now()

        Log.d(LOG_TAG, "[Sync] Starting upload for entry MFID ${entry.mfid}.")
        return try {
            val imageUrls = uploadImages(entry, timestamp)
            val request = UploadDataRequest.fromEntry(entry, imageUrls)
            supabase.postgrest.rpc("upload_form_data", mapOf("data" to Json.encodeToJsonElement(request)))
            Log.d(LOG_TAG, "[Sync] uploadForm returned normally")
            Log.i(LOG_TAG, "[Sync] Upload successful for MFID ${entry.mfid} - ${formType.label}.")
            markSynced(entry.id, timestamp)
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Sync] Upload failed for MFID ${entry.mfid} - ${formType.label}: ${e.message}.")
            false
        }
    }

    private suspend fun uploadImages(entry: FormEntryEntity, timestamp: Instant): List<String> {
        val images = formRepository.getImagesOfEntryById(entry.id)
        val imageBucket = supabase.storage.from("form-images")
        return images.map { image ->
            val file = File(image.localPath)
            val remotePath = "${entry.seasonId}/${entry.activityType}/${entry.mfid}/$timestamp/${file.name}"
            imageBucket.upload(remotePath, file) { upsert = false }
            formRepository.updateImageRemotePath(image.id, remotePath)
            remotePath
        }
    }

    private suspend fun markSynced(entryId: Long, timestamp: Instant) {
        try {
            formRepository.markAsSyncedWithStatus(entryId, timestamp, SyncStatus.SYNCED)
            Log.i(LOG_TAG, "[Sync] Local sync mark successful for entry $entryId.")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Sync] Local sync mark FAILED for entry $entryId: ${e.message}")
            throw e
        }
    }

    companion object {
        private const val LOG_TAG = "Scout: Sync"
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .build()
    }
}
