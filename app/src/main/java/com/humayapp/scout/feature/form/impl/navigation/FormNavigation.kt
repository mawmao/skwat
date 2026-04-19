package com.humayapp.scout.feature.form.impl.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.database.model.CollectionTaskUiModel
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.network.util.getDouble
import com.humayapp.scout.core.network.util.getString
import com.humayapp.scout.core.network.util.getStringIfNotBlank
import com.humayapp.scout.core.ui.component.ScoutConfirmDialog
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.FormReviewNavKey
import com.humayapp.scout.feature.form.api.navigation.FormWizardNavKey
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.rememberFormState
import com.humayapp.scout.feature.main.data.CollectionRepository
import com.humayapp.scout.navigation.RootNavKey
import com.humayapp.scout.navigation.navigateToMain
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
fun EntryProviderScope<NavKey>.formSection(metadata: Map<String, Any>) {

    entry<RootNavKey.Form>(metadata = metadata) { key ->

        val rootNavigator = LocalRootStackNavigator.current
        val collectionTaskId = key.collectionTaskId

        val applicationContext = (metadata["context"] as? Context)?.applicationContext
            ?: throw IllegalStateException("Context not found in metadata")

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            CollectionRepositoryEntryPoint::class.java
        )
        val collectionRepository = entryPoint.collectionRepository()

        var collectionTask by remember { mutableStateOf<CollectionTaskUiModel?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(collectionTaskId) {
            isLoading = true
            error = null
            try {
                collectionTask = collectionRepository.getUiTaskById(collectionTaskId)
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@entry
        }

        if (error != null || collectionTask == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error loading form: ${error ?: "Task not found"}")
            }
            return@entry
        }

        val task = collectionTask!!
        val dependencyJson = task.dependencyData?.let { Json.parseToJsonElement(it) } as JsonObject
        val formType = FormType.fromActivityType(task.activityType)

        val hasFarmerDetails = formType == FormType.FIELD_DATA && !task.farmerName.isNullOrBlank()

        val (entries, startEntry) = when {
            formType != FormType.FIELD_DATA -> formType.entries to formType.startEntry
            hasFarmerDetails -> FieldData.entriesWithConfirm to FieldData.ConfirmFarmer
            else -> FieldData.entriesWithoutConfirm to FieldData.FarmerInformation
        }

        val formState = rememberFormState(
            formType = formType,
            mfid = task.mfid,
            collectionTaskId = task.id,
            entries = entries,
            startEntry = startEntry
        )

        val formNavigator = rememberStackNavigator<NavKey>("${formType.id} form", FormWizardNavKey)

        var showExitDialog by remember { mutableStateOf(false) }
        var showReviewBackDialog by remember { mutableStateOf(false) }
        var pendingExitAction by remember { mutableStateOf<(() -> Unit)?>(null) }

        LaunchedEffect(Unit) {
            formState.setFieldData("season_id", task.seasonId)

            when (formType) {
                FormType.FIELD_DATA -> {
                    formState.setFieldData(FieldData.PROVINCE_KEY, task.province)
                    formState.setFieldData(FieldData.MUNICIPALITY_OR_CITY_KEY, task.cityMunicipality)
                    formState.setFieldData(FieldData.BARANGAY_KEY, task.barangay)

                    if (hasFarmerDetails) {
                        dependencyJson.getString(FieldData.FIRST_NAME_KEY)?.let {
                            formState.setFieldData(FieldData.FIRST_NAME_KEY, it)
                        }
                        dependencyJson.getString(FieldData.LAST_NAME_KEY)?.let {
                            formState.setFieldData(FieldData.LAST_NAME_KEY, it)
                        }
                        dependencyJson.getString(FieldData.GENDER_KEY)?.let {
                            formState.setFieldData(FieldData.GENDER_KEY, it)
                        }
                        dependencyJson.getString(FieldData.DATE_OF_BIRTH_KEY)?.let {
                            formState.setFieldData(FieldData.DATE_OF_BIRTH_KEY, it)
                        }
                        dependencyJson.getString(FieldData.CELLPHONE_NO_KEY)?.let {
                            formState.setFieldData(FieldData.CELLPHONE_NO_KEY, it)
                        }
                    }
                }

                FormType.CULTURAL_MANAGEMENT,
                FormType.NUTRIENT_MANAGEMENT,
                FormType.PRODUCTION,
                FormType.DAMAGE_ASSESSMENT -> {
                    when (formType) {
                        FormType.CULTURAL_MANAGEMENT -> {
                            dependencyJson.getDouble(FieldData.TOTAL_FIELD_AREA_KEY)?.let {
                                formState.setFieldData(FieldData.TOTAL_FIELD_AREA_KEY, it)
                            }
                            dependencyJson.getString(FieldData.EST_CROP_ESTABLISHMENT_KEY)?.let {
                                formState.setFieldData(FieldData.EST_CROP_ESTABLISHMENT_KEY, it)
                            }
                            dependencyJson.getDouble(CulturalManagement.MONITORING_FIELD_AREA_KEY)?.let {
                                formState.setFieldData(CulturalManagement.MONITORING_FIELD_AREA_KEY, it)
                            }
                        }

                        FormType.NUTRIENT_MANAGEMENT -> {
                            dependencyJson.getDouble(CulturalManagement.MONITORING_FIELD_AREA_KEY)?.let {
                                formState.setFieldData(CulturalManagement.MONITORING_FIELD_AREA_KEY, it)
                            }
                        }

                        FormType.PRODUCTION, FormType.DAMAGE_ASSESSMENT -> {
                            dependencyJson.getDouble(FieldData.TOTAL_FIELD_AREA_KEY)?.let {
                                formState.setFieldData(FieldData.TOTAL_FIELD_AREA_KEY, it)
                            }
                        }
                    }
                }
            }
        }

        CompositionLocalProvider(
            LocalStackNavigator provides formNavigator,
            LocalFormState provides formState
        ) {
            FormNavDisplay(
                modifier = Modifier.fillMaxSize(),
                onBack = {
                    when (formNavigator.current) {
                        is FormWizardNavKey -> {
                            if (formState.canScrollBack) {
                                formState.scrollWizardBack()
                            } else {
                                pendingExitAction = { rootNavigator.navigateToMain() }
                                showExitDialog = true
                            }
                        }

                        is FormReviewNavKey -> {
                            showReviewBackDialog = true
                        }
                    }
                }
            )
        }

        ScoutConfirmDialog(
            isVisible = showExitDialog,
            message = "Are you sure you want to cancel this form? Your progress will be lost.",
            onConfirm = {
                showExitDialog = false
                pendingExitAction?.invoke()
                pendingExitAction = null
            },
            onDismissRequest = {
                showExitDialog = false
                pendingExitAction = null
            }
        )

        ScoutConfirmDialog(
            isVisible = showReviewBackDialog,
            message = "Edit again?",
            onConfirm = {
                showReviewBackDialog = false
                formNavigator.pop()
            },
            onDismissRequest = {
                showReviewBackDialog = false
            }
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CollectionRepositoryEntryPoint {
    fun collectionRepository(): CollectionRepository
}

fun getDependencyString(json: JsonElement?, key: String): String? =
    json?.jsonObject?.get(key)?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
