package com.humayapp.scout.feature.form.impl.data.registry.fielddata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.system.LocationMonitor
import com.humayapp.scout.feature.form.impl.data.repository.Coordinates
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesRepository
import com.humayapp.scout.feature.form.impl.data.repository.LocationRepository
import com.humayapp.scout.feature.form.impl.data.repository.emptyCoordinates
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FieldDataViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val coordinatesRepository: CoordinatesRepository,
    private val locationMonitor: LocationMonitor,
) : ViewModel() {

    private val _locationState = MutableStateFlow<LocationState>(LocationState())
    val locationState = _locationState.asStateFlow()

    val coordinatesState: StateFlow<CoordinatesState> =
        locationMonitor.isEnabled
            .flatMapLatest { enabled ->
                if (!enabled) {
                    flowOf(
                        CoordinatesState(
                            locationServicesDisabled = true,
                            coordinatesLoading = false
                        )
                    )
                } else {
                    coordinatesRepository.coordinates
                        .map { coords ->
                            CoordinatesState(
                                coordinates = coords,
                                locationServicesDisabled = false
                            )
                        }
                        .onStart {
                            emit(CoordinatesState(coordinatesLoading = true))
                        }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CoordinatesState()
            )

    private val _errors = MutableStateFlow<Errors>(Errors())
    val errors = _errors.asStateFlow()

    private var municipalitiesJob: Job? = null
    private var barangaysJob: Job? = null
    private var coordinatesJob: Job? = null

    private val municipalitiesByProvince = mutableMapOf<String, List<String>>()
    private val barangaysByMunicipality = mutableMapOf<String, List<String>>()

    fun clearError() {
        _errors.update { it.copy(location = null, coordinates = null) }
    }

    fun clearBarangays() {
        _locationState.update { it.copy(barangays = emptyList()) }
    }

    fun fetchMunicipalities(province: String) {
        val p = province.trim()
        if (p.isEmpty()) {
            _locationState.update { it.copy(municipalities = emptyList(), municipalitiesLoading = false) }
            return
        }

        val cached = municipalitiesByProvince[p]
        if (cached != null) {
            _locationState.update { it.copy(municipalities = cached, municipalitiesLoading = false) }
            return
        }

        municipalitiesJob?.cancel()
        municipalitiesJob = viewModelScope.launch {
            _locationState.update { it.copy(municipalitiesLoading = true) }

            runCatching {
                locationRepository.getMunicipalitiesByProvince(p) // suspend
            }.onSuccess { list ->
                _locationState.update {
                    it.copy(
                        municipalities = list,
                        municipalitiesLoading = false,
                    )
                }
                municipalitiesByProvince[p] = list
                clearBarangays()
            }.onFailure { err ->
                _locationState.update { it.copy(municipalitiesLoading = false) }
                _errors.update { it.copy(location = err.message) }
            }
        }
    }

    fun fetchBarangays(municipality: String) {
        val m = municipality.trim()
        if (m.isEmpty()) {
            _locationState.update { it.copy(barangays = emptyList(), barangaysLoading = false) }
            return
        }

        val cached = barangaysByMunicipality[m]
        if (cached != null) {
            _locationState.update { it.copy(barangays = cached, barangaysLoading = false) }
            return
        }

        barangaysJob?.cancel()
        barangaysJob = viewModelScope.launch {
            _locationState.update { it.copy(barangaysLoading = true) }

            runCatching {
                locationRepository.getBarangaysByCityMunicipality(m) // suspend
            }.onSuccess { list ->
                _locationState.update {
                    it.copy(
                        barangays = list,
                        barangaysLoading = false,

                        )
                }
                barangaysByMunicipality[m] = list
            }.onFailure { err ->
                _locationState.update { it.copy(barangaysLoading = false) }
                _errors.update { it.copy(location = err.message) }
            }
        }
    }

//    fun fetchCoordinates() {
//        coordinatesJob?.cancel()
//        coordinatesJob = viewModelScope.launch {
//            _coordinatesState.update { it.copy(coordinatesLoading = true, locationServicesDisabled = false) }
//            runCatching {
//                coordinatesRepository.getCoordinates()
//            }.onSuccess { coordinates ->
//                _coordinatesState.update {
//                    it.copy(coordinates = coordinates, coordinatesLoading = false)
//                }
//            }.onFailure { err ->
//                _coordinatesState.update { it.copy(coordinatesLoading = false) }
//                _errors.update { it.copy(coordinates = err.message) }
//            }
//        }
//    }

    data class Errors(
        val location: String? = null,
        val coordinates: String? = null,
    )

    data class LocationState(
        val municipalities: List<String> = emptyList(),
        val municipalitiesLoading: Boolean = false,

        val barangays: List<String> = emptyList(),
        val barangaysLoading: Boolean = false,
    )

    data class CoordinatesState(
        val coordinates: Coordinates = emptyCoordinates(),
        val coordinatesLoading: Boolean = false,
        val locationServicesDisabled: Boolean = false
    )
}
