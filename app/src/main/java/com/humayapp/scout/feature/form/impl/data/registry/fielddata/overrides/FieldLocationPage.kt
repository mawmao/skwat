package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
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
                        value = { formState.getFieldData(field.key) },
                        onValueChange = {
                            formState.setFieldData(field.key, it)
                            formState.clearFieldData(MUNICIPALITY_OR_CITY_KEY)
                            vm.fetchMunicipalities(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = field.imeAction
                    )
                }

                MUNICIPALITY_OR_CITY_KEY -> {
                    WizardField(
                        field = field,
                        visible = formState.hasFieldData(PROVINCE_KEY),
                        value = { formState.getFieldData(field.key) },
                        onValueChange = {
                            formState.setFieldData(field.key, it)
                            formState.clearFieldData(BARANGAY_KEY)
                            vm.fetchBarangays(it)
                        },
                        dynamicOptions = locationState.municipalities,
                        imeAction = field.imeAction,
                    )
                }

                BARANGAY_KEY -> {
                    WizardField(
                        field = field,
                        visible = formState.hasFieldData(PROVINCE_KEY) && formState.hasFieldData(MUNICIPALITY_OR_CITY_KEY),
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

