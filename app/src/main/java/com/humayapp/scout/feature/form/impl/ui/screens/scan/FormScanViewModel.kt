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

    val surfaceRequest = cameraManager.surfaceRequest
    val scannedBarcode = cameraManager.scannedBarcode

    fun resetScannedBarcode() {
        cameraManager.resetBarcode()
    }

    fun bindToCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraManager.startCamera(lifecycleOwner)
        }
    }

    companion object {
        private const val LOG_TAG = "Scout: ScanViewModel"
    }
}

