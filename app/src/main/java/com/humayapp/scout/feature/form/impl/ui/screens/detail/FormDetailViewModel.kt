package com.humayapp.scout.feature.form.impl.ui.screens.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.User
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Duration.Companion.hours

@HiltViewModel(assistedFactory = FormDetailsViewModel.Factory::class)
class FormDetailsViewModel @AssistedInject constructor(
    private val formRepository: FormRepository,
    private val collectionRepository: CollectionRepository,
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    @Assisted("collectionTaskId") private val collectionTaskId: Int,
    @Assisted("activityId") private val activityId: Int?,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FormDetailsUiState>(FormDetailsUiState.Loading)
    val uiState: StateFlow<FormDetailsUiState> = _uiState.asStateFlow()

    private val _retakeEvent = MutableSharedFlow<Unit>()
    val retakeEvent = _retakeEvent.asSharedFlow()

    private var originalTask: CollectionTask? = null

    init {
        loadFormData()
    }

    private fun loadFormData() {
        viewModelScope.launch {
            _uiState.value = FormDetailsUiState.Loading
            try {
                val isOnline = authRepository.isOnline()
                val pair = if (activityId != null && isOnline) {
                    fetchRemoteFormData(activityId)
                } else {
                    fetchLocalFormData(collectionTaskId)
                }
                if (pair != null) {
                    val (rawDetails, typedFormData) = pair
                    val (retakeAvailable, retakePending) = getRetakeStatus(collectionTaskId)
                    val originalTask = getOriginalTask()
                    _uiState.value = FormDetailsUiState.Success(
                        rawDetails, typedFormData,
                        retakeAvailable, retakePending, originalTask
                    )
                } else {
                    _uiState.value = FormDetailsUiState.Error("Form data not found")
                }
            } catch (e: Exception) {
                _uiState.value = FormDetailsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun retakeForm() {
        viewModelScope.launch {
            _retakeEvent.emit(Unit)
        }
    }

    private suspend fun getRetakeStatus(collectionTaskId: Int): Pair<Boolean, Boolean> {
        val pendingRetake = collectionRepository.getRetakeTaskByOriginalId(collectionTaskId, "pending")
        val completedRetake = collectionRepository.getRetakeTaskByOriginalId(collectionTaskId, "completed", "pending")
        return Pair(pendingRetake != null, completedRetake != null)
    }

    private suspend fun fetchRemoteFormData(activityId: Int): Pair<FieldActivityDetails, FormData>? {
        Log.d(LOG_TAG, "Fetching remote form data for activityId=$activityId")
        return try {
            val raw = supabase.from("field_activity_details").select() {
                filter {
                    eq("id", activityId)
                }
            }.decodeSingle<FieldActivityDetails>()

            Log.d(LOG_TAG, "Raw image URLs: ${raw.imageUrls}")

            val signedUrls = raw.imageUrls.map { remotePath ->
                generateSignedUrl(remotePath).takeIf { it.isNotBlank() } ?: remotePath
            }
            val rawWithSignedUrls = raw.copy(imageUrls = signedUrls)

            val typedFormData = parseFormData(raw.activityType, raw.formData)

            // Fetch the original task for this collectionTaskId (used for retake navigation)
            originalTask = collectionRepository.getCollectionTaskById(collectionTaskId)

            rawWithSignedUrls to typedFormData
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error fetching remote form data", e)
            null
        }
    }

    private suspend fun generateSignedUrl(remotePath: String): String {
        return try {
            val url = supabase.storage.from("form-images")
                .createSignedUrl(remotePath, 1.hours)
            Log.d(LOG_TAG, "Generated signed URL for $remotePath: $url")
            url
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to generate signed URL for $remotePath", e)
            ""
        }
    }

    suspend fun getOriginalTask(): CollectionTask? =
        originalTask ?: collectionRepository.getCollectionTaskById(collectionTaskId)

    private suspend fun fetchLocalFormData(collectionTaskId: Int): Pair<FieldActivityDetails, FormData>? {
        val task = collectionRepository.getCollectionTaskById(collectionTaskId) ?: return null
        originalTask = task

        // 1. Try cache by activityId (for synced forms)
        if (task.activityId != null) {
            val cached = collectionRepository.getCachedFormDetails(task.activityId)
            if (cached != null) {
                val rawDetails = Json.decodeFromString<FieldActivityDetails>(cached.rawDetailsJson)
                val formData = Json.decodeFromString<FormData>(cached.formDataJson)
                return rawDetails to formData
            }
        }

        // 2. Try cache by collectionTaskId (for immediately after submission)
        val cachedByTask = collectionRepository.getCachedFormDetailsByTaskId(collectionTaskId)
        if (cachedByTask != null) {
            val rawDetails = Json.decodeFromString<FieldActivityDetails>(cachedByTask.rawDetailsJson)
            val formData = Json.decodeFromString<FormData>(cachedByTask.formDataJson)
            return rawDetails to formData
        }

        // 3. Fallback to form_entries (for pending sync forms not yet cached)
        val entry = formRepository.getEntryByCollectionTaskId(collectionTaskId)
        if (entry == null) {
            Log.d(LOG_TAG, "No cached or local data for collectionTaskId=$collectionTaskId")
            return null
        }

        val rawDetails = buildLocalFieldActivityDetails(entry, task)
        val typedFormData = parseFormData(rawDetails.activityType, rawDetails.formData)
        return rawDetails to typedFormData
    }

    private suspend fun buildLocalFieldActivityDetails(
        entry: FormEntryEntity,
        task: CollectionTask
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

    private fun parseFormData(activityType: String, formDataElement: JsonElement): FormData {
        return when (activityType) {
            "field-data" -> formDataJson.decodeFromJsonElement<FieldDataForm>(formDataElement)
            "cultural-management" -> formDataJson.decodeFromJsonElement<CulturalManagementForm>(formDataElement)
            "nutrient-management" -> formDataJson.decodeFromJsonElement<NutrientManagementForm>(formDataElement)
            "production" -> formDataJson.decodeFromJsonElement<ProductionForm>(formDataElement)
            "damage-assessment" -> formDataJson.decodeFromJsonElement<DamageAssessmentForm>(formDataElement)
            else -> throw IllegalArgumentException("Unknown activity type: $activityType")
        }
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
        val rawDetails: FieldActivityDetails,
        val formData: FormData,
        val retakeAvailable: Boolean,
        val retakePending: Boolean,
        val originalTask: CollectionTask?
    ) : FormDetailsUiState()

    data class Error(val message: String) : FormDetailsUiState()
}
