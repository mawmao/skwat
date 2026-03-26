package com.humayapp.scout.feature.form.impl.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.system.ScoutPermissions
import com.humayapp.scout.core.ui.common.PermissionRationale
import com.humayapp.scout.core.ui.component.ScoutBottomSheet
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.ui.components.camera.CameraScanner
import com.humayapp.scout.navigation.navigateToForms


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FormScanScreen(
    vm: FormScanViewModel = hiltViewModel(),
    formType: FormType,
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    val rootNavigator = LocalRootStackNavigator.current

    val surfaceRequest by vm.surfaceRequest.collectAsStateWithLifecycle()
    val scanResult by vm.scannedBarcode.collectAsStateWithLifecycle()
    val isTorchOn by vm.isTorchOn.collectAsStateWithLifecycle()
    val isCameraReady by vm.isCameraReady.collectAsStateWithLifecycle()

    val permission = ScoutPermissions.Camera
    val locationPermissions = rememberMultiplePermissionsState(permissions = permission.permissions)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scannedBarcodeForSheet by remember { mutableStateOf<String?>(null) }
    var scannedMunicity by remember { mutableStateOf("") }
    var scannedProvince by remember { mutableStateOf("") }

    LaunchedEffect(lifecycleOwner) {
        vm.bindToCamera(lifecycleOwner)
    }

    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            vm.onPause()
        } else {
            vm.onResume(lifecycleOwner)
        }
    }

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
            scannedBarcodeForSheet = barcode
            val result = vm.parseMfid(barcode) ?: unreachable("no reason for mfid to not know location")
            val (province, city) = result
            scannedProvince = province
            scannedMunicity = city
            vm.resetScannedBarcode()
            sheetState.show()
        }
    }

    if (scannedBarcodeForSheet != null) {
        ScoutBottomSheet(
            onDismissRequest = { scannedBarcodeForSheet = null },
            sheetState = sheetState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            scannedBarcodeForSheet?.let { barcode ->
                Text("Confirm")
                Spacer(Modifier.height(8.dp))
                Text(text = "${scannedMunicity}, $scannedProvince")
                Spacer(Modifier.height(8.dp))
                Text(barcode, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(ScoutTheme.spacing.large))

                ScoutButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Proceed",
                    onClick = {
                        scannedBarcodeForSheet = null
                        rootNavigator.navigateToForms(formType, barcode, scannedProvince, scannedMunicity)
                    }
                )
            }
            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            modifier = Modifier
                .height(360.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(ScoutIcons.QrCodeScanner),
                contentDescription = null,
                tint = Color.White
            )
            Spacer(Modifier.height(ScoutTheme.spacing.medium))
            Text(text = "Scan MFID", style = ScoutTheme.material.typography.headlineSmall, color = Color.White)
            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
            Text(
                text = "Use the camera to scan the MFID",
                style = ScoutTheme.material.typography.bodyMedium,
                color = Color.White
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CameraScanner(
                modifier = Modifier
                    .size(width = 220.dp, height = 220.dp)
                    .clip(RoundedCornerShape(24.dp)),
                surfaceRequest = surfaceRequest,
                isTorchOn = isTorchOn,
                isCameraReady = isCameraReady,
                onTorchPress = {
                    vm.enableTorch(!isTorchOn)
                }
            )
        }
    }
}
