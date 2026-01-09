package com.humayapp.scout.feature.form.impl.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.sync.enqueueSyncWork
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.ScoutUiEvents
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import com.humayapp.scout.navigation.navigateToMain
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


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

            Log.d(LOG_TAG, "raw answers = $answers")

            val jsonLogger = Json { prettyPrint }

//            val payloadJson = if (formType.transformer != null) {
//                Log.d(
//                    "Scout: ReviewViewModel",
//                    "Payload: ${jsonLogger.encodeToString(formType.transformer.transform(answers))}"
//                )
//                Json.encodeToString(formType.transformer.transform(answers))
//            } else {
//                Json.encodeToString(answers.toJsonObject())
//            }
            Log.d(LOG_TAG, "Payload: ${jsonLogger.encodeToString(answers.toJsonObject())}")

            val payloadJson = Json.encodeToString(answers.toJsonObject())

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

fun Map<String, Any?>.toJsonObject(): JsonObject {
    return buildJsonObject {
        for ((key, value) in this@toJsonObject) {
            put(key, value as String)
        }
    }
}

@Composable
fun FormReviewScreen(
    vm: FormReviewViewModel
) {
    val rootNavigator = LocalRootStackNavigator.current
    val state = LocalFormState.current

    ScoutUiEvents(vm.uiEvent) { event ->
        when (event) {
            FormReviewEvent.SubmitSuccess -> rootNavigator.navigateToMain()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScoutTheme.margin)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        // this is the default
        state.entries.fastForEach { entry ->
            entry.fields.fastForEach { field ->
                val value = state.getAnswer(field.key)
                Text(text = "${field.label}: $value")
                Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
            }
        }

        Spacer(Modifier.height(16.dp))

        // todo: extract this so each form type handles its own review ui
        // this is for nutrient management
        if (state.formType == FormType.NUTRIENT_MANAGEMENT) {
            var index = 1
            while (true) {
                val fertilizerKey = "${NutrientManagement.FERTILIZER_TYPE_KEY}_$index"
                val brandKey = "${NutrientManagement.BRAND_KEY}_$index"

                if (!state.answers.containsKey(fertilizerKey)) break

                val nitrogenKey = "${NutrientManagement.NITROGEN_CONTENT_KEY}_$index"
                val phosphorusKey = "${NutrientManagement.PHOSPHORUS_CONTENT_KEY}_$index"
                val potassiumKey = "${NutrientManagement.POTASSIUM_CONTENT_KEY}_$index"
                val amountKey = "${NutrientManagement.AMOUNT_APPLIED_KEY}_$index"
                val unitKey = "${NutrientManagement.AMOUNT_UNIT_KEY}_$index"
                val cropStageKey = "${NutrientManagement.CROP_STAGE_ON_APPLICATION_KEY}_$index"

                Text("Fertilizer Application #$index")
                Text("  Fertilizer: ${state.getAnswer(fertilizerKey)}")
                Text("  Brand: ${state.getAnswer(brandKey)}")
                Text("  Nitrogen: ${state.getAnswer(nitrogenKey)}")
                Text("  Phosphorus: ${state.getAnswer(phosphorusKey)}")
                Text("  Potassium: ${state.getAnswer(potassiumKey)}")
                Text("  Amount: ${state.getAnswer(amountKey)} ${state.getAnswer(unitKey)}")
                Text("  Crop Stage: ${state.getAnswer(cropStageKey)}")
                Spacer(Modifier.height(ScoutTheme.spacing.medium))

                index++
            }
        }

        Spacer(Modifier.weight(1f))
        ScoutButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Finish",
            onClick = { vm.onAction(FormReviewAction.FormSubmit(state.answers)) }
        )
    }
}

