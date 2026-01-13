package com.humayapp.scout.feature.history.impl.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.network.util.asFieldData
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = HistoryDetailViewModel.Factory::class)
class HistoryDetailViewModel @AssistedInject constructor(
    private val formRepository: FormRepository,
    @Assisted("entry") private val entry: FormEntryEntity
) : ViewModel() {

    val uiState: StateFlow<HistoryDetailUiState> = formRepository.getImagesOfEntryByIdFlow(entry.id)
        .map { images ->
            HistoryDetailUiState.Ready(
                images = images,
                formType = FormType.fromActivityType(entry.activityType),
                fieldData = entry.payloadJson.asFieldData()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryDetailUiState.Loading
        )

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("entry") entry: FormEntryEntity
        ): HistoryDetailViewModel
    }
}


sealed interface HistoryDetailUiState {
    data object Loading : HistoryDetailUiState

    @Immutable
    data class Ready(
        val images: List<FormImageEntity>,
        val formType: FormType,
        val fieldData: Map<String, Any?>
    ) : HistoryDetailUiState
}

