package com.humayapp.scout.feature.form.impl.ui.screens.review

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.data.sync.SyncRepository
import com.humayapp.scout.core.database.model.SyncQueueEntity
import com.humayapp.scout.core.database.model.SyncType
import com.humayapp.scout.core.sync.SyncOrchestrator
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.main.data.CollectionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel(assistedFactory = FormReviewViewModel.Factory::class)
class FormReviewViewModel @AssistedInject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val collectionRepository: CollectionRepository,
    @Assisted("formType") private val formType: FormType,
    @Assisted("mfid") private val mfid: String,
    @Assisted("collection_task_id") private val collectionTaskId: Int,
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

        logAnswers(answers)

        viewModelScope.launch {
            try {
                // handle this cleanly when session expires
                val userId = authRepository.getCurrentUserId() ?: unreachable("user id in this context must never be null. session expiry not handled yet tho")

                val serializedString = formType.serializeAnswers(answers).toString()

                Log.d("Scout: FormReviewViewModel", "answers = $answers")
                Log.d("Scout: FormReviewViewModel", serializedString)

                val imageAnswers = answers
                    .filter { it.key.startsWith("img_") && it.value is String }
                    .mapValues { it.value as String }

                val success = collectionRepository.saveTaskWithImages(
                    activityType = formType.id,
                    context = context,
                    images = imageAnswers,
                    collectionTaskId = collectionTaskId,
                    userId = userId,
                    formData = serializedString
                )

                if (success) {
                    syncRepository.queueSync(
                        SyncQueueEntity(
                            type = SyncType.FORM_SUBMISSION,
                            refId = collectionTaskId.toString(),
                            payload = serializedString
                        )
                    )
                }

                _uiEvent.send(FormReviewEvent.SubmitSuccess)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Database insert failed", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun logAnswers(answers: Map<String, Any?>) {
        val maxKeyLength = answers.keys.maxOfOrNull { it.length } ?: 0
        Log.d(LOG_TAG, "---- Raw Form Answers ----")
        answers.forEach { (key, value) ->
            val paddedKey = key.padEnd(maxKeyLength)
            Log.d(LOG_TAG, "$paddedKey : $value")
        }
        Log.d(LOG_TAG, "----------------------")
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("formType") formType: FormType,
            @Assisted("mfid") mfid: String,
            @Assisted("collection_task_id") collectionTaskId: Int,
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
    data class SubmitSuccessAndNavigate(val activityId: Int) : FormReviewEvent()
    data class SubmitError(val message: String) : FormReviewEvent()
}

sealed class FormReviewAction {
    data class FormSubmit(val answers: Map<String, Any?>) : FormReviewAction()
}

sealed class FormSaveResult {
    data class Success(val taskId: Int) : FormSaveResult()
}
