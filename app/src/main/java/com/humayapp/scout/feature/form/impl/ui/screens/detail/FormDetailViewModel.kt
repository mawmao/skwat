package com.humayapp.scout.feature.form.impl.ui.screens.detail

import android.util.Log
import android.webkit.ConsoleMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.decode.ImageSource
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.core.network.util.SupabaseImageHelper
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.User
import com.humayapp.scout.feature.form.api.barangay
import com.humayapp.scout.feature.form.api.province
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
import io.ktor.client.request.forms.formData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.ExperimentalUuidApi

@HiltViewModel(assistedFactory = FormDetailsViewModel.Factory::class)
class FormDetailsViewModel @AssistedInject constructor(
    private val collectionRepository: CollectionRepository,
    private val supabase: SupabaseClient,
    @Assisted("collectionTaskId") private val collectionTaskId: Int,
    @Assisted("activityId") private val activityId: Int?,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FormDetailsUiState>(FormDetailsUiState.Loading)
    val uiState: StateFlow<FormDetailsUiState> = _uiState.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var originalTask: CollectionTask? = null

    init {
        loadFormData()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun loadFormData() {
        Log.d(LOG_TAG, "Loading form data of collection_task_id=$collectionTaskId + activityId=$activityId")

        viewModelScope.launch {
            _uiState.value = FormDetailsUiState.Loading
            _isRefreshing.value = true

            try {
                var task = collectionRepository.getCollectionTaskById(collectionTaskId)
                var formDataElement: JsonElement? = task?.formData?.let { FormDataJson.toJsonElement(it) }
                var rawDetails: FieldActivityDetails

                if (task == null || formDataElement == null) {
                    // Fetch from Supabase
                    Log.d(LOG_TAG, "Local data missing, fetching from Supabase")
                    val remoteTask = collectionRepository.getCollectionTaskFromSupabase(collectionTaskId)
                        ?: throw Exception("Form not found on server")

                    // Cache the remote task locally for future use
                    collectionRepository.upsertCollectionTask(remoteTask)

                    // Use the remote task as the current task
                    task = remoteTask
                    formDataElement = remoteTask.formData?.let { FormDataJson.toJsonElement(it) }
                        ?: throw Exception("Remote form data is null")

                    rawDetails = FieldActivityDetails(
                        mfid = task.mfid,
                        seasonId = task.seasonId,
                        activityType = task.activityType,
                        collectedAt = task.collectedAt ?: Clock.System.now(),
                        farmerName = task.farmerName ?: "",
                        barangay = task.barangay ?: "",
                        municipality = task.cityMunicipality,
                        province = task.province,
                        isRetake = task.retakeOf != null,
                        formData = formDataElement,
                        imageUrls = emptyList() // placeholder, will be filled below
                    )
                } else {
                    // Build from local data
                    rawDetails = FieldActivityDetails(
                        mfid = task.mfid,
                        seasonId = task.seasonId,
                        activityType = task.activityType,
                        collectedAt = task.collectedAt ?: Clock.System.now(),
                        farmerName = task.farmerName ?: "",
                        barangay = task.barangay ?: "",
                        municipality = task.cityMunicipality,
                        province = task.province,
                        isRetake = task.retakeOf != null,
                        formData = formDataElement,
                        imageUrls = emptyList()
                    )
                }

                // Handle images
                val images = collectionRepository.getImagesById(collectionTaskId)
                val localImages = mutableListOf<String>()
                val remotePaths = mutableListOf<String>()

                images.forEach { image ->
                    val file = File(image.localPath)
                    if (file.exists()) {
                        localImages.add(file.toURI().toString())
                    } else if (!image.remotePath.isNullOrBlank()) {
                        remotePaths.add(image.remotePath)
                    }
                }

                val signedUrls = if (remotePaths.isNotEmpty()) {
                    SupabaseImageHelper.generateSignedUrls(supabase, remotePaths)
                } else emptyList()

                val finalImages = localImages + signedUrls
                rawDetails = rawDetails.copy(imageUrls = finalImages)

                _uiState.value = FormDetailsUiState.Success(
                    task = task,                      // now guaranteed non-null
                    rawDetails = rawDetails,
                    formData = formDataElement!!,     // guaranteed non-null
                    retakeAvailable = task.canRetake,
                    retakePending = false,
                    originalTask = task
                )
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to load form data", e)
                _uiState.value = FormDetailsUiState.Error(e.message ?: "Unknown error")
            } finally {
                _isRefreshing.value = false
            }
        }
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
        val task: CollectionTask,
        val rawDetails: FieldActivityDetails,
        val formData: JsonElement,
        val retakeAvailable: Boolean,
        val retakePending: Boolean,
        val originalTask: CollectionTask?
    ) : FormDetailsUiState()

    data class Error(val message: String) : FormDetailsUiState()
}


fun jsonElementToDisplayList(element: JsonElement, prefix: String = ""): List<Pair<String, String>> {
    return when (element) {
        is JsonObject -> {
            element.entries.flatMap { (key, value) ->
                if (key == "monitoring_visit") return@flatMap emptyList()
                if (key == "fertilizer_application") return@flatMap emptyList()

                val label = if (prefix.isEmpty()) getReadableLabel(key) else "$prefix${getReadableLabel(key)}"
                when (value) {
                    is JsonNull -> listOf(label to "No Data")
                    is JsonPrimitive -> listOf(label to value.content)
                    is JsonArray -> {
                        if (key == "applications") {
                            value.flatMapIndexed { idx, app ->
                                val header = "$label ${idx + 1}"
                                val fields = jsonElementToDisplayList(app, "  ")
                                listOf(header to "") + fields
                            }
                        } else {
                            listOf(label to value.joinToString(", ") { it.toString() })
                        }
                    }

                    is JsonObject -> {
                        jsonElementToDisplayList(value, "$label - ")
                    }
                }
            }
        }

        else -> emptyList()
    }
}


object FormDataJson {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun toJsonElement(value: String?): JsonElement? =
        value?.let { json.parseToJsonElement(it) }

    fun fromJsonElement(element: JsonElement?): String? =
        element?.let { json.encodeToString(JsonElement.serializer(), it) }
}