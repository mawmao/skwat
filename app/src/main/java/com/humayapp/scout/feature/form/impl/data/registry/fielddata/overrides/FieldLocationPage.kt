package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.ScoutErrorEvent
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.BARANGAY_KEY
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.MUNICIPALITY_OR_CITY_KEY
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.PROVINCE_KEY
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldDataViewModel
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField

@Composable
fun FieldLocationPage(
    page: FieldData.FieldLocation,
    vm: FieldDataViewModel = hiltViewModel()
) {
    val formState = LocalFormState.current
    val locationState by vm.locationState.collectAsStateWithLifecycle()
    val errors by vm.errors.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.fetchBarangays(formState.getFieldData(MUNICIPALITY_OR_CITY_KEY))
    }

    ScoutErrorEvent(
        errorMessage = errors.location,
        onDismiss = { vm.clearError() }
    )

    WizardEntry(page) { entry ->
        entry.fields.fastForEach { field ->
            when (field.key) {
                PROVINCE_KEY -> {
                    WizardField(
                        field = field,
                        value = { formState.getFieldData(PROVINCE_KEY) },
                        onValueChange = { formState.setFieldData(field.key, it) },
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = field.imeAction,
                        isReadOnly = true
                    )
                }

                MUNICIPALITY_OR_CITY_KEY -> {
                    WizardField(
                        field = field,
                        visible = formState.hasFieldData(MUNICIPALITY_OR_CITY_KEY),
                        value = { formState.getFieldData(MUNICIPALITY_OR_CITY_KEY) },
                        onValueChange = { },
                        dynamicOptions = locationState.municipalities,
                        imeAction = field.imeAction,
                        isReadOnly = true
                    )
                }

                BARANGAY_KEY -> {
                    WizardField(
                        field = field,
                        visible = formState.hasFieldData(PROVINCE_KEY) && formState.hasFieldData(
                            MUNICIPALITY_OR_CITY_KEY
                        ),
                        value = { formState.getFieldData(field.key) },
                        onValueChange = { formState.setFieldData(field.key, it) },
                        dynamicOptions = locationState.barangays,
                        imeAction = field.imeAction
                    )
                }
            }

            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }
    }
}

