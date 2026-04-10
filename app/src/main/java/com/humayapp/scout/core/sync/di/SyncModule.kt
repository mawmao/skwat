package com.humayapp.scout.core.sync.di

import com.humayapp.scout.core.data.notification.NotificationRepository
import com.humayapp.scout.core.data.sync.SyncRepository
import com.humayapp.scout.core.sync.SyncManager
import com.humayapp.scout.core.sync.SyncOrchestrator
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.main.data.CollectionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import jakarta.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun providesSyncOrchestrator(): SyncOrchestrator = SyncOrchestrator()

    @Provides
    @Singleton
    fun providesSyncManager(
        networkMonitor: NetworkMonitor,
        authRepository: AuthRepository,
        syncRepository: SyncRepository,
        collectionRepository: CollectionRepository,
        notificationRepository: NotificationRepository,
        syncOrchestrator: SyncOrchestrator,
        supabaseClient: SupabaseClient,
    ): SyncManager = SyncManager(
        networkMonitor,
        authRepository,
        syncRepository,
        collectionRepository,
        notificationRepository,
        syncOrchestrator,
        supabaseClient
    )
}