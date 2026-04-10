package com.humayapp.scout.core.sync

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class SyncOrchestrator {
    private val mutex = Mutex()
    suspend fun <T> runSync(block: suspend () -> T): T {
        if (!mutex.tryLock()) {
            Log.i("SyncOrchestrator", "[Sync] Already running. Skipping.")
            throw CancellationException("Sync already running.")
        }

        return try {
            block()
        } finally {
            mutex.unlock()
        }
    }
}