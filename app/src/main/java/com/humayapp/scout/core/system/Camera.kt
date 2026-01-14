package com.humayapp.scout.core.system

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ExecutorService

class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("CAMERA") private val cameraExecutor: ExecutorService
) {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    private val _scannedBarcode = MutableStateFlow<String?>(null)
    val scannedBarcode = _scannedBarcode.asStateFlow()

    private val previewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    private val imageAnalysisUseCase = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    init {
        setupAnalyzer()
    }

    private fun setupAnalyzer() {

        //  Region Code: ^06 or ^60 [To be confirmed]
        //  Province Code: (04|06|19|30|45|79)
        //  City, Municipality Code:  \\d{2}
        //  Barangay Code: \\d{3}
        val region = "^60"
        val province = "(04|06|19|30|45|79)"
        val cityMunicipality = "\\d{2}"
        val barangay = "\\d{3}"
        val pattern = Regex("$region$province$cityMunicipality$barangay$")

        imageAnalysisUseCase.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
            val value = barcode.rawValue

            value?.let { v ->
                if (pattern.matches(v)) {
                    _scannedBarcode.value = v
                }
            }
        })
    }

    suspend fun startCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                imageAnalysisUseCase
            )
        } catch (e: Exception) {
            Log.e("Scout: CameraManager","Use case binding failed $e")
        }
    }

    fun resetBarcode() {
        _scannedBarcode.value = null
    }
}
