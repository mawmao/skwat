package com.humayapp.scout.core.data.sync

import com.humayapp.scout.core.database.dao.SyncQueueDao
import com.humayapp.scout.core.database.dao.SyncStateDao
import com.humayapp.scout.core.database.model.SyncQueueEntity
import com.humayapp.scout.core.database.model.SyncStateEntity
import com.humayapp.scout.core.database.model.SyncStatus
import kotlin.time.Instant

class SyncRepository(
    private val syncQueueDao: SyncQueueDao,
    private val syncStateDao: SyncStateDao
) {
    suspend fun queueSync(sync: SyncQueueEntity) {
        syncQueueDao.insert(sync)
    }

    suspend fun getPendingQueue(): List<SyncQueueEntity> {
        return syncQueueDao.getPending()
    }

    suspend fun markInProgress(id: Long) {
        syncQueueDao.updateStatus(id, SyncStatus.IN_PROGRESS)
    }

    suspend fun markDone(id: Long) {
        syncQueueDao.delete(id)
    }

    suspend fun markFailed(id: Long, error: String?) {
        syncQueueDao.incrementAttempts(id)
        syncQueueDao.updateStatus(id, SyncStatus.FAILED)
    }

    suspend fun getLastSync(key: String): Instant? {
        return syncStateDao.getLastSync(key)
    }

    suspend fun updateSyncState(key: String, lastSync: Instant) {
        syncStateDao.upsert(SyncStateEntity(key, lastSync))
    }
}
