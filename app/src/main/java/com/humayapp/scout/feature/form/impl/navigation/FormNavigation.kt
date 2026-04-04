package com.humayapp.scout.feature.form.impl.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.network.util.getDouble
import com.humayapp.scout.core.network.util.getString
import com.humayapp.scout.core.ui.component.ScoutConfirmDialog
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.FormReviewNavKey
import com.humayapp.scout.feature.form.api.navigation.FormWizardNavKey
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.rememberFormState
import com.humayapp.scout.navigation.RootNavKey
import com.humayapp.scout.navigation.navigateToMain
import kotlinx.serialization.json.jsonObject
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
fun EntryProviderScope<NavKey>.formSection(metadata: Map<String, Any>) {

    entry<RootNavKey.Form>(metadata = metadata) { key ->

        val rootNavigator = LocalRootStackNavigator.current
        val formType = FormType.fromActivityType(key.collectionTask.activityType)
        val formState = rememberFormState(formType, key.collectionTask.mfid, key.collectionTask.id)
        val formNavigator = rememberStackNavigator<NavKey>("${formType.id} form", FormWizardNavKey)

        var showExitDialog by remember { mutableStateOf(false) }
        var showReviewBackDialog by remember { mutableStateOf(false) }
        var pendingExitAction by remember { mutableStateOf<(() -> Unit)?>(null) }

        LaunchedEffect(Unit) {
            val task = key.collectionTask
            formState.setFieldData("season_id", task.seasonId)

            when (formType) {
                FormType.FIELD_DATA -> {
                    formState.setFieldData(FieldData.PROVINCE_KEY, task.province)
                    formState.setFieldData(FieldData.MUNICIPALITY_OR_CITY_KEY, task.cityMunicipality)
                }

                FormType.CULTURAL_MANAGEMENT,
                FormType.NUTRIENT_MANAGEMENT,
                FormType.PRODUCTION,
                FormType.DAMAGE_ASSESSMENT -> {
                    val dependency = task.dependencyData?.jsonObject
                    if (dependency != null) {
                        when (formType) {
                            FormType.CULTURAL_MANAGEMENT -> {
                                dependency.getDouble(FieldData.TOTAL_FIELD_AREA_KEY)?.let {
                                    formState.setFieldData(FieldData.TOTAL_FIELD_AREA_KEY, it)
                                }
                                dependency.getString(FieldData.EST_CROP_ESTABLISHMENT_KEY)?.let {
                                    formState.setFieldData(FieldData.EST_CROP_ESTABLISHMENT_KEY, it)
                                }
                                dependency.getDouble(CulturalManagement.MONITORING_FIELD_AREA_KEY)?.let {
                                    formState.setFieldData(CulturalManagement.MONITORING_FIELD_AREA_KEY, it)
                                }
                            }

                            FormType.NUTRIENT_MANAGEMENT -> {
                                dependency.getDouble(CulturalManagement.MONITORING_FIELD_AREA_KEY)?.let {
                                    formState.setFieldData(CulturalManagement.MONITORING_FIELD_AREA_KEY, it)
                                }
                            }

                            FormType.PRODUCTION, FormType.DAMAGE_ASSESSMENT -> {
                                dependency.getDouble(FieldData.TOTAL_FIELD_AREA_KEY)?.let {
                                    formState.setFieldData(FieldData.TOTAL_FIELD_AREA_KEY, it)
                                }
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
