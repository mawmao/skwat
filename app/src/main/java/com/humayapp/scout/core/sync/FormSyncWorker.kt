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
import com.humayapp.scout.core.network.service.SupabaseService
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
class FormSyncWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val formRepository: FormRepository,
    private val supabaseService: SupabaseService
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        Log.i(LOG_TAG, "[Sync] Doing sync work.")

        return@withContext try {
            val pending = formRepository.getPendingSyncOnce()

            if (pending.isEmpty()) {
                Log.i(LOG_TAG, "[Sync] No pending forms found. Exiting sync.")
                return@withContext Result.success()
            }

            Log.i(LOG_TAG, "[Sync] Found ${pending.size} pending entries to sync")

            Log.d(LOG_TAG, "Entries queued\n")
            pending.forEach { entry ->
                Log.d(LOG_TAG, "  Type ${entry.activityType.uppercase()} MFID ${entry.mfid}")
            }

            var hasError = false

            for (entry in pending) {

                Log.d(LOG_TAG, "[Sync] Starting upload for entry MFID ${entry.mfid}")

                try {
                    supabaseService.uploadForm(entry)
                    Log.i(LOG_TAG, "[Sync] Upload successful for MFID ${entry.mfid}")
                } catch (e: Exception) {
                    hasError = true
                    Log.e(LOG_TAG, "[Sync] Upload FAILED for MFID ${entry.mfid}: ${e.message}")
                    continue
                }

                try {
                    formRepository.markAsSynced(entry.id)
                    Log.i(LOG_TAG, "[Sync] Local sync mark successful for MFID ${entry.mfid}")
                } catch (e: Exception) {
                    hasError = true
                    Log.e(LOG_TAG, "[Sync] Local sync mark FAILED for MFID ${entry.mfid}: ${e.message}")
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
            Log.e(LOG_TAG, "[Sync] Fatal sync error")
            Result.failure()
        }
    }

    companion object {

        private const val LOG_TAG = "Scout: FormSyncWorker"

        fun startUpSyncWork() = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .build()
    }
}
