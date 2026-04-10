package com.humayapp.scout.core.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncOrchestrator {
    private val mutex = Mutex()
    suspend fun run(block: suspend () -> Unit) = mutex.withLock { block() }
}