package com.humayapp.scout.core.data.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.humayapp.scout.core.data.notification.NotificationRepository
import com.humayapp.scout.core.data.sync.SyncDataStore
import com.humayapp.scout.core.data.sync.SyncRepository
import com.humayapp.scout.core.database.dao.NotificationDao
import com.humayapp.scout.core.database.dao.SyncQueueDao
import com.humayapp.scout.core.database.dao.SyncStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

private val Context.syncDataStore by preferencesDataStore(name = "sync_data_store")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSyncDataStore(@ApplicationContext context: Context): SyncDataStore {
        return SyncDataStore(context.syncDataStore)
    }

    @Provides
    @Singleton
    fun providesSyncRepository(syncQueueDao: SyncQueueDao, syncStateDao: SyncStateDao): SyncRepository {
        return SyncRepository(syncQueueDao, syncStateDao)
    }

    @Provides
    @Singleton
    fun providesNotificationRepository(
        supabase: SupabaseClient,
        dao: NotificationDao,
    ): NotificationRepository {
        return NotificationRepository(supabase, dao)
    }
}