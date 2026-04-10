package com.humayapp.scout.core.sync.di

import com.humayapp.scout.core.sync.SyncOrchestrator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun providesSyncOrchestrator(): SyncOrchestrator = SyncOrchestrator()
}