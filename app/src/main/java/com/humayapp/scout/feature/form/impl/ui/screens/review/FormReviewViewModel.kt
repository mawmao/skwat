package com.humayapp.scout.feature.form.impl.ui.screens.review

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FormReviewViewModel.Factory::class)
class FormReviewViewModel @AssistedInject constructor(
    private val formRepository: FormRepository,
    private val authRepository: AuthRepository,
    @Assisted("formType") private val formType: FormType,
    @Assisted("mfid") private val mfid: String,
    @ApplicationContext private val context: Context
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

//    private fun toggleBackConfirm(show: Boolean) =
//        _state.update { it.copy(isBackConfirmShown = show) }

//    private fun toggleExitConfirm(show: Boolean) =
//        _state.update { it.copy(isExitConfirmShown = show) }

    private fun submitForm(answers: Map<String, Any?>) {
        viewModelScope.launch {
            val userId = getAuthenticatedUserId()

            val payloadJson = formType.serializeAnswers(answers).toString()
            val entry = FormEntryEntity(
                mfid = mfid,
                activityType = formType.id,
                collectedBy = userId ?: "",
                payloadJson = payloadJson,
            )

            try {
                val id = formRepository.saveFormEntry(entry)
                if (id > 0) {
                    Log.d(LOG_TAG, "form entry submission success")
                    context.enqueueSyncWork()
                    _uiEvent.send(FormReviewEvent.SubmitSuccess)
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Database insert failed")
            }
        }
    }


    private suspend fun getAuthenticatedUserId(): String? {
        return when (val status = authRepository.sessionStatus.first()) {
            is SessionStatus.Authenticated -> status.session.user?.id
            else -> throw IllegalStateException("User not authenticated")
        }
    }


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
//    val currentForm: com.mawmao.recon.forms.model.Form,
    val isBackConfirmShown: Boolean = false,
    val isExitConfirmShown: Boolean = false,
)

sealed class FormReviewEvent {
    object SubmitSuccess : FormReviewEvent()
}

sealed class FormReviewAction {
//    object ExitClick : ReviewAction()
//    object ExitDismiss : ReviewAction()
//    object BackClick : ReviewAction()
//    object BackDismiss : ReviewAction()
    data class FormSubmit(val answers: Map<String, Any?>) : FormReviewAction()
}
