package com.humayapp.scout.core.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.humayapp.scout.R

private const val SYNC_NOTIFICATION_ID = 0
private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

fun Context.syncForegroundInfo() = ForegroundInfo(
    SYNC_NOTIFICATION_ID,
    syncWorkNotification(),
)

fun Context.enqueueSyncWork(entryId: Int? = null) {

    val builder = OneTimeWorkRequestBuilder<FormSyncWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setConstraints(SyncConstraints)

    if (entryId != null) {
        builder.setInputData(workDataOf("ENTRY_ID" to entryId))
        Log.i("Scout: Sync", "[Sync] Queuing sync work for form with id $entryId.")
    } else {
        Log.i("Scout: Sync", "[Sync] Queuing sync work for all pending entries.")
    }

    WorkManager.getInstance(this).enqueue(builder.build())
}


private fun Context.syncWorkNotification(): Notification {
    val channel = NotificationChannel(
        SYNC_NOTIFICATION_CHANNEL_ID,
        "Sync",
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = "Background tasks for Scout"
    }
    val notificationManager: NotificationManager? =
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    notificationManager?.createNotificationChannel(channel)

    return NotificationCompat.Builder(
        this,
        SYNC_NOTIFICATION_CHANNEL_ID,
    )
        .setSmallIcon(R.drawable.logo)
        .setContentTitle("Scout")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
}
