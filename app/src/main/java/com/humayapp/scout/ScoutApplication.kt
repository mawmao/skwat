package com.humayapp.scout

import android.app.Application
import androidx.compose.runtime.Composer
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.Auto)
    }
}