package com.humayapp.scout.feature.history.impl.ui.screens

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.data.settings.SettingsRepository
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.SyncStatus
import com.humayapp.scout.core.network.util.asFieldData
import com.humayapp.scout.core.sync.enqueueSyncWork
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Instant

@HiltViewModel(assistedFactory = HistoryDetailViewModel.Factory::class)
class HistoryDetailViewModel @AssistedInject constructor(
    private val formRepository: FormRepository,
    private val settingsRepository: SettingsRepository,
    @Assisted("entry") private val entry: FormEntryEntity,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val uiState: StateFlow<HistoryDetailUiState> = combine(
        formRepository.getEntryByIdFlow(entry.id),
        formRepository.getImagesOfEntryByIdFlow(entry.id)
    ) { entry, images ->
        if (entry == null) {
            HistoryDetailUiState.Loading
        } else {
            HistoryDetailUiState.Ready(
                images = images,
                formType = FormType.fromActivityType(entry.activityType),
                fieldData = entry.payloadJson.asFieldData(),
                syncedAt = entry.syncedAt,
                syncStatus = entry.syncStatus
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryDetailUiState.Loading
    )

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _uiEvent = Channel<HistoryDetailUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun syncEntry() {
        viewModelScope.launch {
            if (_syncState.value is SyncState.Loading) return@launch
            _syncState.value = SyncState.Loading

            try {
                val autoSync = settingsRepository.getAutoSync().first()
                if (autoSync) {
                    context.enqueueSyncWork(entryId = entry.id)
                    _syncState.value = SyncState.Success
                    _uiEvent.send(HistoryDetailUiEvent.SyncStarted)
                } else {
                    context.enqueueSyncWork(entryId = entry.id)
                    _syncState.value = SyncState.Success
                    _uiEvent.send(HistoryDetailUiEvent.SyncStarted)
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message ?: "Sync failed")
                _uiEvent.send(HistoryDetailUiEvent.SyncFailed(e.message ?: "Sync failed"))
            } finally {
                // Reset state after a delay? Or leave as success/error until next sync.
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("entry") entry: FormEntryEntity): HistoryDetailViewModel
    }
}

sealed interface SyncState {
    object Idle : SyncState
    object Loading : SyncState
    object Success : SyncState
    data class Error(val message: String) : SyncState
}


sealed interface HistoryDetailUiState {
    data object Loading : HistoryDetailUiState

    @Immutable
    data class Ready(
        val images: List<FormImageEntity>,
        val formType: FormType,
        val fieldData: Map<String, Any?>,
        val syncedAt: Instant? = null,
        val syncStatus: SyncStatus = SyncStatus.PENDING
    ) : HistoryDetailUiState
}


sealed class HistoryDetailUiEvent {
    object SyncStarted : HistoryDetailUiEvent()
    data class SyncFailed(val message: String) : HistoryDetailUiEvent()
}
