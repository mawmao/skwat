package com.humayapp.scout.feature.form.impl.ui.screens.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.system.ScoutPermissions
import com.humayapp.scout.core.ui.common.PermissionRationale
import com.humayapp.scout.core.ui.component.ScoutIconButton
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.ui.components.camera.CameraScanner
import com.humayapp.scout.navigation.navigateToForms
import com.humayapp.scout.navigation.navigateToMain


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FormScanScreen(
    vm: FormScanViewModel = hiltViewModel(),
    formType: FormType,
) {
    val rootNavigator = LocalRootStackNavigator.current

    val surfaceRequest by vm.surfaceRequest.collectAsStateWithLifecycle()
    val scanResult by vm.scannedBarcode.collectAsStateWithLifecycle()

    val permission = ScoutPermissions.Camera
    val locationPermissions = rememberMultiplePermissionsState(permissions = permission.permissions)

    LaunchedEffect(locationPermissions) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    if (!locationPermissions.allPermissionsGranted) {
        PermissionRationale(permission = permission)
        return
    }

    LaunchedEffect(scanResult) {
        scanResult?.let { barcode ->
            vm.resetScannedBarcode()
            rootNavigator.navigateToForms(formType, barcode)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        ScoutButton(text = "Skip") {
//            rootNavigator.navigateToForms(formType, "600401001")
//        }
        CameraScanner(
            surfaceRequest = surfaceRequest,
            bindToCamera = vm::bindToCamera,
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = { },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    navigationIcon = {
                        ScoutIconButton(
                            onClick = rootNavigator::navigateToMain,
                            icon = ScoutIcons.Back,
                            contentDescription = null,
                            tint = ScoutTheme.material.colorScheme.background
                        )
                    }
                )
            }
        )
    }
}
