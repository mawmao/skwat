package com.humayapp.scout.feature.form.impl.ui.screens.scan

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.system.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

/**
 * Prefixed with "Form" and might look like is created in Form navigation stack,
 * but is actually in the Root stack since this uses the `Root.Overlay` nav key and
 * is passed as the `content` parameter
 *
 * @see com.humayapp.scout.feature.main.home.impl.HomeScreen
 */
@HiltViewModel
class FormScanViewModel @Inject constructor(
    private val cameraManager: CameraManager
) : ViewModel() {

    val isTorchOn = cameraManager.isTorchOn
    val surfaceRequest = cameraManager.surfaceRequest
    val scannedBarcode = cameraManager.scannedBarcode
    val isCameraReady = cameraManager.isCameraReady

    fun resetScannedBarcode() {
        cameraManager.resetBarcode()
    }

    fun bindToCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraManager.startCamera(lifecycleOwner)
        }
    }

    fun enableTorch(value: Boolean) = cameraManager.enableTorch(value)

    override fun onCleared() {
        super.onCleared()
        cameraManager.stopCamera()
    }

    fun onPause() {
        cameraManager.pause()
    }

    fun onResume(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraManager.resume(lifecycleOwner)
        }
    }
}

