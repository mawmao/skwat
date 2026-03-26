package com.humayapp.scout.feature.form.impl.ui.screens.review

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.data.settings.SettingsRepository
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.sync.enqueueSyncWork
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FormReviewViewModel.Factory::class)
class FormReviewViewModel @AssistedInject constructor(
    private val formRepository: FormRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    @Assisted("formType") private val formType: FormType,
    @Assisted("mfid") private val mfid: String,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormReviewScreenState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<FormReviewEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: FormReviewAction) {
        when (action) {
            is FormReviewAction.FormSubmit -> submitForm(action.answers)
        }
    }

    private fun submitForm(answers: Map<String, Any?>) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val userId = getAuthenticatedUserId() ?: unreachable("user id in this context can never be null")
                Log.d(LOG_TAG, "Trying to save form with images $answers by $userId")

                val id = formRepository.saveFormWithImages(
                    answers = answers,
                    initialEntry = FormEntryEntity(
                        mfid = mfid,
                        activityType = formType.id,
                        collectedBy = userId,
                        payloadJson = "",
                    ),
                    context = context,
                    serializerFn = { answers -> formType.serializeAnswers(answers).toString() }
                )

                val autoSyncEnabled = settingsRepository.getAutoSync().first()
                if (autoSyncEnabled) {
                    context.enqueueSyncWork(entryId = id)
                }

                _uiEvent.send(FormReviewEvent.SubmitSuccess)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Database insert failed", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun getAuthenticatedUserId(): String? = authRepository.getCurrentUserId()

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("formType") formType: FormType,
            @Assisted("mfid") mfid: String
        ): FormReviewViewModel
    }

    companion object {
        private const val LOG_TAG = "Scout: ReviewViewModel"
    }
}


data class FormReviewScreenState(
    val isBackConfirmShown: Boolean = false,
    val isExitConfirmShown: Boolean = false,
    val isLoading: Boolean = false
)

sealed class FormReviewEvent {
    object SubmitSuccess : FormReviewEvent()
}

sealed class FormReviewAction {
    data class FormSubmit(val answers: Map<String, Any?>) : FormReviewAction()
}
