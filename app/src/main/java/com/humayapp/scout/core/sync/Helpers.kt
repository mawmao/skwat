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
import com.humayapp.scout.R

private const val SYNC_NOTIFICATION_ID = 0
private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

val SyncConstraints get() = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

fun Context.syncForegroundInfo() = ForegroundInfo(
    SYNC_NOTIFICATION_ID,
    syncWorkNotification(),
)

fun Context.enqueueSyncWork() {
    Log.d("Scout: enqueueSyncWork()", "queuing sync work")
    WorkManager.getInstance(this).enqueue(
        FormSyncWorker.startUpSyncWork()
    )
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
