package com.humayapp.scout.core.system

import android.content.Context
import android.util.Log
import android.util.Rational
import android.view.Surface
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.TorchState
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.AspectRatioStrategy.FALLBACK_RULE_AUTO
import androidx.camera.core.resolutionselector.ResolutionSelector
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

    private var lastScannedBarcode: String? = null
    private var lastScanTime: Long = 0
    private val scanCooldown = 2000L // 2 seconds between scans

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn = _isTorchOn.asStateFlow()

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    private val _scannedBarcode = MutableStateFlow<String?>(null)
    val scannedBarcode = _scannedBarcode.asStateFlow()

    private var camera: Camera? = null

    private var cameraProvider: ProcessCameraProvider? = null

    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady = _isCameraReady.asStateFlow()

    private val previewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    private val imageAnalysisUseCase = ImageAnalysis.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy(AspectRatio.RATIO_4_3, FALLBACK_RULE_AUTO))
                .build()
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .build()

    private val viewport = ViewPort.Builder(Rational(1, 1), Surface.ROTATION_0).build()

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
            val currentTime = System.currentTimeMillis()

            value?.let { v ->
                if (pattern.matches(v) &&
                    (v != lastScannedBarcode || currentTime - lastScanTime > scanCooldown)
                ) {
                    lastScannedBarcode = v
                    lastScanTime = currentTime
                    _scannedBarcode.value = v
                }
            }
        })
    }

    fun enableTorch(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }

    suspend fun startCamera(lifecycleOwner: LifecycleOwner) {
        _isCameraReady.value = false

        cameraProvider = ProcessCameraProvider.awaitInstance(context)

        try {

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(previewUseCase)
                .addUseCase(imageAnalysisUseCase)
                .setViewPort(viewport)
                .build()

            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                useCaseGroup
            )

            camera?.cameraInfo?.torchState?.observe(lifecycleOwner) { state ->
                _isTorchOn.value = state == TorchState.ON
            }

            _isCameraReady.value = true
        } catch (e: Exception) {
            Log.e("Scout: CameraManager", "Use case binding failed $e")
            _isCameraReady.value = false
        }
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        camera = null
        _surfaceRequest.value = null
        _isCameraReady.value = false
    }

    fun resetBarcode() {
        _scannedBarcode.value = null
        lastScannedBarcode = null
        lastScanTime = 0
    }

    fun pause() {
        cameraProvider?.unbind(imageAnalysisUseCase, previewUseCase)
    }

    suspend fun resume(lifecycleOwner: LifecycleOwner) {
        startCamera(lifecycleOwner)
    }
}
