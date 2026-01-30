package com.humayapp.scout.core.system.di

import android.content.Context
import com.humayapp.scout.core.common.dispatcher.Dispatcher
import com.humayapp.scout.core.common.dispatcher.ScoutDispatchers
import com.humayapp.scout.core.system.CameraManager
import com.humayapp.scout.core.system.ConnectivityManagerNetworkMonitor
import com.humayapp.scout.core.system.LocationMonitor
import com.humayapp.scout.core.system.LocationProviderMonitor
import com.humayapp.scout.core.system.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Module
@InstallIn(SingletonComponent::class)
object SystemModule {

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context,
        @Dispatcher(ScoutDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    ): NetworkMonitor = ConnectivityManagerNetworkMonitor(context, ioDispatcher)

    @Provides
    @Singleton
    fun provideLocationMonitor(
        @ApplicationContext context: Context,
        @Dispatcher(ScoutDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    ): LocationMonitor = LocationProviderMonitor(context, ioDispatcher)

    @Provides
    @Singleton
    @Named("CAMERA")
    fun provideCameraExecutor(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }

    @Provides
    @Singleton
    fun provideCameraManager(
        @ApplicationContext context: Context,
        @Named("CAMERA") cameraExecutor: ExecutorService
    ): CameraManager = CameraManager(context, cameraExecutor)
}
