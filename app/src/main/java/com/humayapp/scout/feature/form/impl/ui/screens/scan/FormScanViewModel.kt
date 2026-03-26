package com.humayapp.scout.feature.form.impl.ui.screens.scan

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.system.CameraManager
import com.humayapp.scout.feature.form.api.mfidPattern
import com.humayapp.scout.feature.form.impl.data.repository.LocationRepository
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
    private val cameraManager: CameraManager,
    private val locationRepository: LocationRepository
) : ViewModel() {

    val isTorchOn = cameraManager.isTorchOn
    val surfaceRequest = cameraManager.surfaceRequest
    val scannedBarcode = cameraManager.scannedBarcode
    val isCameraReady = cameraManager.isCameraReady

    suspend fun parseMfid(mfid: String): Pair<String, String>? {
        val match = mfidPattern.matchEntire(mfid) ?: return null
        val (regionCode, provinceCode, cityMunicipalityCode, barangayCode) = match.destructured

        val city = locationRepository.getCityMunicipalityByCode(
            "60$provinceCode$cityMunicipalityCode"
        ) ?: return null

        val province = locationRepository.getProvinceById(city.provinceId) ?: return null

        return province.name to city.name
    }

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

