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
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.SyncStatus
import com.humayapp.scout.core.network.util.getFieldIdByMfid
import com.humayapp.scout.core.network.util.getLatestStartedSeasonId
import com.humayapp.scout.core.network.util.getPlantingSeasonIdForHarvest
import com.humayapp.scout.core.network.util.getSeasonIdByDate
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import com.humayapp.scout.feature.form.impl.data.sync.uploadForm
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.time.Clock

@HiltWorker
class FormSyncWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val formRepository: FormRepository,
    private val supabase: SupabaseClient,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        Log.i(LOG_TAG, "[Sync] Doing sync work.")

        return@withContext try {
            val entryId = inputData.getLong("ENTRY_ID", -1L)
            val pending = if (entryId != -1L) {
                listOfNotNull(formRepository.getEntryById(entryId))
            } else {
                formRepository.getPendingSyncOnce()
            }

            if (pending.isEmpty()) {
                Log.i(LOG_TAG, "[Sync] No pending forms found. Exiting sync.")
                return@withContext Result.success()
            }

            Log.i(LOG_TAG, "[Sync] Trying to sync ${pending.size} ${if (pending.size > 1) "entries" else "entry"}.")

            var hasError = false
            val imageBucket = supabase.storage.from("form-images")

            for (entry in pending) {

                val formType = FormType.fromActivityType(entry.activityType)
                val activityType = entry.activityType
                val mfid = entry.mfid
                val timestamp = Clock.System.now()

                Log.d(LOG_TAG, "[Sync] Starting upload for entry MFID ${entry.mfid}.")
                Log.d(LOG_TAG, "[Sync] Entry data = ${entry}.")

                try {
                    val fieldId = try {
                        supabase.getFieldIdByMfid(entry.mfid)
                    } catch (e: Exception) {
                        Log.w(LOG_TAG, "[Sync] Failed to get field ID for MFID ${entry.mfid}, assuming no existing record.")
                        null
                    }

                    Log.w(LOG_TAG, "[Sync] Field id found = $fieldId")
                    val seasonId = computeSeasonIdForEntry(entry, fieldId)

                    if (fieldId != null) {
                        val existing = supabase.from("field_activities").select(Columns.list("verification_status")) {
                            filter {
                                eq("field_id", fieldId)
                                eq("season_id", seasonId)
                                eq("activity_type", activityType)
                            }
                        }.decodeSingleOrNull<JsonObject>()

                        if (existing?.get("verification_status")?.jsonPrimitive?.content == "approved") {
                            Log.i(LOG_TAG, "[Sync] Entry for MFID ${entry.mfid} already approved on server. Skipping upload.")
                            formRepository.markAsSyncedWithStatus(entry.id, timestamp, SyncStatus.DUPLICATE)
                            continue
                        }
                    }
                    val images = formRepository.getImagesOfEntryById(entry.id)
                    val updatedImages = images.map { image ->
                        val file = File(image.localPath)
                        val remotePath = "${seasonId}/${activityType}/${mfid}/${timestamp}/${file.name}"
                        imageBucket.upload(remotePath, file) { upsert = false }
                        formRepository.updateImageRemotePath(image.id, remotePath)
                        image.copy(remotePath = remotePath)
                    }

                    Log.d(LOG_TAG, "[Sync] Trying to upload final entry copy = ${entry.copy(
                        imageUrls = buildImagePathsArray(updatedImages),
                        syncedAt = timestamp
                    )}")

                    supabase.uploadForm(
                        entry = entry.copy(
                            imageUrls = buildImagePathsArray(updatedImages),
                            syncedAt = timestamp
                        )
                    )

                    Log.d(LOG_TAG, "[Sync] uploadForm returned normally")
                    Log.i(LOG_TAG, "[Sync] Upload successful for MFID ${entry.mfid} - ${formType.label}.")

                } catch (e: Exception) {
                    hasError = true
                    Log.e(LOG_TAG, "[Sync] Upload failed for MFID ${entry.mfid} - ${formType.label}: ${e.message}.")
                    continue
                }

                try {
                    formRepository.markAsSyncedWithStatus(entry.id, timestamp, SyncStatus.SYNCED)
                    Log.i(LOG_TAG, "[Sync] Local sync mark successful for MFID ${entry.mfid} - ${formType.label}.")
                } catch (e: Exception) {
                    hasError = true
                    Log.e(LOG_TAG, "[Sync] Local sync mark FAILED for MFID ${entry.mfid} - ${formType.label}: ${e.message}")
                }
            }

            if (hasError) {
                Log.w(LOG_TAG, "[Sync] Partial sync failure detected. Requesting retry.")
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

    private suspend fun computeSeasonIdForEntry(entry: FormEntryEntity, fieldId: Int?): Int {
        return when (entry.activityType) {
            "production" -> {
                if (fieldId == null) {
                    supabase.getLatestStartedSeasonId()
                } else {
                    val payload = Json.parseToJsonElement(entry.payloadJson)
                    val harvestDate = payload.jsonObject["harvest_date"]?.jsonPrimitive?.content
                    if (harvestDate != null) {
                        supabase.getPlantingSeasonIdForHarvest(fieldId, harvestDate) ?: supabase.getLatestStartedSeasonId()
                    } else {
                        supabase.getLatestStartedSeasonId()
                    }
                }
            }
            "nutrient-management" -> {
                if (fieldId == null) {
                    supabase.getLatestStartedSeasonId()
                } else {
                    val payload = Json.parseToJsonElement(entry.payloadJson)
                    val applicationDate = payload.jsonObject["application_date"]?.jsonPrimitive?.content
                    if (applicationDate != null) {
                        supabase.getSeasonIdByDate(applicationDate) ?: supabase.getLatestStartedSeasonId()
                    } else {
                        supabase.getLatestStartedSeasonId()
                    }
                }
            }
            else -> supabase.getLatestStartedSeasonId()   // field-data, etc. always use latest started season
        }
    }

    companion object {
        private const val LOG_TAG = "Scout: Sync"
        private fun buildImagePathsArray(images: List<FormImageEntity>): List<String> = images.mapNotNull { it.remotePath }
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .build()
    }
}
