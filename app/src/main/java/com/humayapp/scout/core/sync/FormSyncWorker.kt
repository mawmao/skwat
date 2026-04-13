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
import com.humayapp.scout.core.system.SnackbarManager
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


@HiltWorker
class FormSyncWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val syncManager: SyncManager,
    private val snackbarManager: SnackbarManager,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        if (!syncManager.isReadyToSync()) return@withContext Result.success()

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
                val success = syncManager.processQueueItem(item)
                if (!success) hasError = true
            }

            if (hasError) {
                snackbarManager.show("Sync failed - Retrying")
                Log.w(LOG_TAG, "[Sync] Partial sync failure. Requesting retry.")
                Result.retry()
            } else {
                snackbarManager.show("Sync complete")
                Log.i(LOG_TAG, "[Sync] All entries synced successfully.")
                Result.success()
            }
        } catch (e: Exception) {
            snackbarManager.show("Sync failed")
            Log.e(LOG_TAG, "[Sync] Fatal sync error", e)
            Result.failure()
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
