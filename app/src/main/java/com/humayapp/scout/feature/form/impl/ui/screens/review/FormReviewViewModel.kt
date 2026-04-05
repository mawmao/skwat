package com.humayapp.scout.feature.form.impl.ui.screens.review

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.data.settings.SettingsRepository
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.core.sync.enqueueSyncWork
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.User
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepository
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import com.humayapp.scout.feature.form.impl.model.CulturalManagementForm
import com.humayapp.scout.feature.form.impl.model.DamageAssessmentForm
import com.humayapp.scout.feature.form.impl.model.FieldActivityDetails
import com.humayapp.scout.feature.form.impl.model.FieldDataForm
import com.humayapp.scout.feature.form.impl.model.FormData
import com.humayapp.scout.feature.form.impl.model.NutrientManagementForm
import com.humayapp.scout.feature.form.impl.model.ProductionForm
import com.humayapp.scout.feature.form.impl.model.formDataJson
import com.humayapp.scout.feature.form.impl.ui.screens.review.FormDataCacheHelper.buildLocalFieldActivityDetails
import com.humayapp.scout.feature.form.impl.ui.screens.review.FormDataCacheHelper.parseFormData
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Clock

@HiltViewModel(assistedFactory = FormReviewViewModel.Factory::class)
class FormReviewViewModel @AssistedInject constructor(
    private val formRepository: FormRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val collectionTaskDao: CollectionTaskDao,
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

        val serialized = formType.serializeAnswers(answers)
        val serializedString = serialized.toString()
        Log.d(LOG_TAG, "---- Serialized Answers (without season_id) ----\n$serializedString\n-----------------------------------------------")

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
                        seasonId = answers["season_id"] as Int,
                        collectionTaskId = collectionTaskId,
                        payloadJson = "",
                    ),
                    context = context,
                    serializerFn = { answers -> formType.serializeAnswers(answers).toString() }
                )

                collectionTaskDao.markTaskCompleted(
                    taskId = collectionTaskId,
                    collectorId = userId,
                    collectedAt = Clock.System.now()
                )

                context.enqueueSyncWork(entryId = id)
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

    private suspend fun getAuthenticatedUserId(): String? = authRepository.getCurrentUserId()


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
}

sealed class FormReviewAction {
    data class FormSubmit(val answers: Map<String, Any?>) : FormReviewAction()
}




object FormDataCacheHelper {
    suspend fun buildLocalFieldActivityDetails(
        entry: FormEntryEntity,
        task: CollectionTask,
        authRepository: AuthRepository
    ): FieldActivityDetails {
        val formDataElement = Json.parseToJsonElement(entry.payloadJson)
        val currentUser = authRepository.getCurrentUser()
        val collectedBy = User(
            id = entry.collectedBy,
            email = currentUser?.email,
            firstName = currentUser?.firstName,
            lastName = currentUser?.lastName,
            role = currentUser?.role,
            dateOfBirth = currentUser?.dateOfBirth,
            isActive = currentUser?.isActive,
            createdAt = currentUser?.createdAt,
            updatedAt = currentUser?.updatedAt,
            lastSignInAt = currentUser?.lastSignInAt
        )
        return FieldActivityDetails(
            id = 0,
            mfid = entry.mfid,
            seasonYear = "",
            semester = "",
            fieldId = 0,
            seasonId = entry.seasonId,
            activityType = entry.activityType,
            collectedBy = collectedBy,
            verifiedBy = null,
            remarks = task.remarks,
            verificationStatus = task.verificationStatus ?: "pending",
            collectedAt = entry.collectedAt,
            verifiedAt = task.verifiedAt,
            syncedAt = entry.syncedAt,
            imageUrls = entry.imageUrls,
            farmerName = task.farmerName ?: "",
            barangay = task.barangay ?: "",
            municipality = task.cityMunicipality,
            province = task.province,
            isRetake = task.retakeOf != null,
            originalActivityId = null,
            formData = formDataElement,
        )
    }

    fun parseFormData(activityType: String, formDataElement: JsonElement): FormData {
        return when (activityType) {
            "field-data" -> formDataJson.decodeFromJsonElement<FieldDataForm>(formDataElement)
            "cultural-management" -> formDataJson.decodeFromJsonElement<CulturalManagementForm>(formDataElement)
            "nutrient-management" -> formDataJson.decodeFromJsonElement<NutrientManagementForm>(formDataElement)
            "production" -> formDataJson.decodeFromJsonElement<ProductionForm>(formDataElement)
            "damage-assessment" -> formDataJson.decodeFromJsonElement<DamageAssessmentForm>(formDataElement)
            else -> throw IllegalArgumentException("Unknown activity type: $activityType")
        }
    }
}