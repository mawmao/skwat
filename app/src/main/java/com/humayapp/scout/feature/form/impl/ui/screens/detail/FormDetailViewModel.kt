package com.humayapp.scout.feature.form.impl.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.database.model.CollectionTaskUiModel
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.feature.main.data.CollectionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.ExperimentalUuidApi

@HiltViewModel(assistedFactory = FormDetailsViewModel.Factory::class)
class FormDetailsViewModel @AssistedInject constructor(
    private val collectionRepository: CollectionRepository,
    @Assisted("collectionTaskId") private val collectionTaskId: Int,
    @Assisted("activityId") private val activityId: Int?,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FormDetailsUiState>(FormDetailsUiState.Loading)
    val uiState: StateFlow<FormDetailsUiState> = _uiState.asStateFlow()

    init {
        loadFormData()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun loadFormData() {
        collectionRepository.observeTaskWithImages(collectionTaskId)
            .onStart {
                _uiState.value = FormDetailsUiState.Loading
            }
            .onEach { (task, images) ->
                _uiState.value = FormDetailsUiState.Success(
                    task = task.copy(imageUrls = images),
                    formData = task.payload?.let { Json.parseToJsonElement(it) } ?: unreachable("form data should be available here"),
                    retakeAvailable = task.canRetake,
                    retakePending = false,
                    originalTask = null
                )
            }
            .catch {
                _uiState.value = FormDetailsUiState.Error(it.message ?: "Unknown")
            }
            .launchIn(viewModelScope)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("collectionTaskId") collectionTaskId: Int,
            @Assisted("activityId") activityId: Int?,
        ): FormDetailsViewModel
    }

    companion object {
        private const val LOG_TAG = "Scout: FormDetailViewModel"
    }
}

sealed class FormDetailsUiState {
    object Loading : FormDetailsUiState()
    data class Success(
        val task: CollectionTaskUiModel,
        val formData: JsonElement,
        val retakeAvailable: Boolean,
        val retakePending: Boolean,
        val originalTask: CollectionTask?
    ) : FormDetailsUiState()

    data class Error(val message: String) : FormDetailsUiState()
}


