package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.humayapp.scout.core.system.ScoutPermissions
import com.humayapp.scout.core.ui.common.PermissionRationale
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.ScoutErrorEvent
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.COORDINATES_KEY
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldDataViewModel
import com.humayapp.scout.feature.form.impl.data.repository.isNotZero
import com.humayapp.scout.feature.form.impl.data.repository.toDisplay
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField

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
        if (!locationPermissions.allPermissionsGranted && !formState.hasFieldData(COORDINATES_KEY)) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    if (!locationPermissions.allPermissionsGranted) {
        PermissionRationale(permission = permission)
        return
    }

    if (coordinatesState.locationServicesDisabled) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(ScoutIcons.LocationOn),
                contentDescription = permission.title,
            )
            Spacer(Modifier.height(ScoutTheme.spacing.large))
            Text(text = "Enable Location / GPS", style = ScoutTheme.material.typography.headlineSmall)
            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
            Text(
                text = permission.description,
                style = ScoutTheme.material.typography.bodyMedium,
                color = ScoutTheme.extras.colors.mutedOnBackground
            )
        }
        return
    }

    // Handle error from GPS acquisition
    if (coordinatesState.error != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(ScoutIcons.Error),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Unable to get location",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = coordinatesState.error ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            ScoutButton(
                text = "Retry",
                onClick = vm::refreshCoordinates
            )
            Spacer(Modifier.height(12.dp))
        }
        return
    }

    ScoutErrorEvent(
        errorMessage = errors.coordinates,
        onDismiss = { vm.clearError() }
    )

    LaunchedEffect(coordinatesState.coordinates) {
        if (coordinatesState.coordinates.isNotZero()) {
            formState.setFieldData(COORDINATES_KEY, coordinatesState.coordinates.toDisplay())
        }
    }

    WizardEntry(page) { entry ->
        if (coordinatesState.coordinatesLoading && !formState.hasFieldData(COORDINATES_KEY)) {
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
