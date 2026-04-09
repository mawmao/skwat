package com.humayapp.scout

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composer
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.humayapp.scout.core.sync.Sync
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.concurrent.ExecutorService

@HiltAndroidApp
class ScoutApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject
    @Named("CAMERA")
    lateinit var cameraExecutor: ExecutorService

    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()

    override fun onCreate() {
        super.onCreate()
        Log.i("Scout: Application", "[Core] Launching app.")

        Sync.initialize(context = this)

        Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.Auto)
    }

    override fun onTerminate() {
        super.onTerminate()
        cameraExecutor.shutdown()
    }
}