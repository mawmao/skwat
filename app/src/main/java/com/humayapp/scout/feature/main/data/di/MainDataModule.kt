package com.humayapp.scout.feature.main.data.di

import com.humayapp.scout.core.data.sync.SyncRepository
import com.humayapp.scout.core.database.ScoutDatabase
import com.humayapp.scout.core.database.dao.CollectionFormDao
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.dao.ImagesDao
import com.humayapp.scout.core.sync.SyncOrchestrator
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.main.data.CollectionRepository
import com.humayapp.scout.feature.main.data.collection.FormNetworkDataSource
import com.humayapp.scout.feature.main.data.collection.TaskNetworkDataSource
import com.humayapp.scout.feature.main.data.util.ImageResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainDataModule {

    @Provides
    @Singleton
    fun provideNewCollectionsRepository(
        database: ScoutDatabase,
        taskDao: CollectionTaskDao,
        formDao: CollectionFormDao,
        imagesDao: ImagesDao,
        syncRepository: SyncRepository,
        taskDataSource: TaskNetworkDataSource,
        formDataSource: FormNetworkDataSource,
        syncOrchestrator: SyncOrchestrator,
        imageResolver: ImageResolver,
        networkMonitor: NetworkMonitor
    ): CollectionRepository = CollectionRepository(
        database,
        taskDao,
        formDao,
        imagesDao,
        syncRepository,
        taskDataSource,
        formDataSource,
        imageResolver,
        networkMonitor
    )

    @Provides
    @Singleton
    fun provideCollectionTaskDataSource(
        authRepository: AuthRepository,
        supabase: SupabaseClient
    ): TaskNetworkDataSource = TaskNetworkDataSource(authRepository, supabase)


    @Provides
    @Singleton
    fun provideCollectionFormDataSource(
        authRepository: AuthRepository,
        supabase: SupabaseClient
    ): FormNetworkDataSource = FormNetworkDataSource(authRepository, supabase)


    @Provides
    @Singleton
    fun provideImageResolved(supabase: SupabaseClient): ImageResolver = ImageResolver(supabase)
}