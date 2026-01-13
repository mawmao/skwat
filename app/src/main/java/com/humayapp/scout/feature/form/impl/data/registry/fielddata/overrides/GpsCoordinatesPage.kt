package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.humayapp.scout.core.system.ScoutPermissions
import com.humayapp.scout.core.ui.common.PermissionRationale
import com.humayapp.scout.core.ui.util.ScoutErrorEvent
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.COORDINATES_KEY
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldDataViewModel
import com.humayapp.scout.feature.form.impl.data.repository.isNotZero
import com.humayapp.scout.feature.form.impl.data.repository.toDisplay
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField

//
// subject for cleaning
//

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GpsCoordinatesPage(
    page: FieldData.GpsCoordinates,
    vm: FieldDataViewModel = hiltViewModel()
) {
    val formState = LocalFormState.current
    if (formState.currentScreen != page) return

    val coordinatesState by vm.coordinatesState.collectAsStateWithLifecycle()
    val errors by vm.errors.collectAsStateWithLifecycle()
    val permission = ScoutPermissions.Location

    val locationPermissions = rememberMultiplePermissionsState(permissions = permission.permissions)

    LaunchedEffect(locationPermissions) {
        if (!locationPermissions.allPermissionsGranted && !formState.hasFieldData(
                COORDINATES_KEY
            )
        ) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    if (!locationPermissions.allPermissionsGranted) {
        PermissionRationale(permission = permission)
        return
    }

    ScoutErrorEvent(
        errorMessage = errors.coordinates,
        onDismiss = { vm.clearError() }
    )

    LaunchedEffect(Unit) {
        if (!formState.hasFieldData(COORDINATES_KEY)) {
            vm.fetchCoordinates()
        }
    }

    LaunchedEffect(coordinatesState.coordinates) {
        if (coordinatesState.coordinates.isNotZero()) {
            formState.setFieldData(COORDINATES_KEY, coordinatesState.coordinates.toDisplay())
        }
    }

    WizardEntry(page) { entry ->
        if (coordinatesState.coordinatesLoading && !formState.hasFieldData(COORDINATES_KEY)) {
            // temporary
            // this should be changed because this looks like sh
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            entry.fields.fastForEach { field ->
                WizardField(
                    field = field,
                    value = { formState.getFieldData(field.key) },
                    onValueChange = { formState.setFieldData(field.key, it) },
                    modifier = Modifier.fillMaxWidth(),
                    imeAction = field.imeAction
                )
            }
        }
    }
}
