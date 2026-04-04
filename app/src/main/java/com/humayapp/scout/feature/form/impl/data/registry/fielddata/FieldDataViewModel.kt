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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

    private val _refreshCoordinates = MutableSharedFlow<Unit>()
    fun refreshCoordinates() {
        viewModelScope.launch {
            _refreshCoordinates.emit(Unit)
        }
    }

    val coordinatesState: StateFlow<CoordinatesState> = combine(
        locationMonitor.isEnabled,
        _refreshCoordinates.asSharedFlow().onStart { emit(Unit) }
    ) { enabled, _ -> enabled }
        .flatMapLatest { enabled ->
            if (!enabled) {
                flowOf(CoordinatesState(locationServicesDisabled = true))
            } else {
                coordinatesRepository.coordinates
                    .map { coords ->
                        CoordinatesState(
                            coordinates = coords,
                            locationServicesDisabled = false,
                            error = null
                        )
                    }
                    .catch { e ->
                        emit(
                            CoordinatesState(
                                coordinatesLoading = false,
                                locationServicesDisabled = false,
                                error = e.message ?: "Failed to get location"
                            )
                        )
                    }
                    .onStart {
                        emit(CoordinatesState(coordinatesLoading = true))
                    }
            }
        }
        .stateIn(
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
        val locationServicesDisabled: Boolean = false,
        val error: String? = null
    )
}
