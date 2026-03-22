package com.humayapp.scout.core.data.settings

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataSource: SettingsDataSource
) {
    fun getAutoSync(): Flow<Boolean> = dataSource.getAutoSync()
    suspend fun setAutoSync(enabled: Boolean) = dataSource.setAutoSync(enabled)
}
