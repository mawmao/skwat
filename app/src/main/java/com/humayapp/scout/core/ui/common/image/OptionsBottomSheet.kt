package com.humayapp.scout.core.ui.common.image

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.humayapp.scout.core.system.util.createTempUri
import com.humayapp.scout.core.ui.component.ScoutBottomSheet
import com.humayapp.scout.core.ui.component.ScoutGhostButton
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme

import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageOptionsBottomSheet(
    key: String? = null,
    hasImage: Boolean = false,
    onRemove: (String) -> Unit,
    onEdit: (String) -> Unit,
    onPreview: (String) -> Unit,
    onDismiss: () -> Unit,
    onImageResult: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var shouldLaunchCamera by remember { mutableStateOf(false) } // added fix

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null && key != null) {
            onImageResult(tempUri!!)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && shouldLaunchCamera && tempUri != null && key != null) {
            takePicture.launch(tempUri!!)
        }
        shouldLaunchCamera = false
    }

    val pickGallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && key != null) {
            onImageResult(uri)
        }
    }



    if (key != null) {
        ScoutBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = onDismiss,
            verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.small)
        ) {

            val color = ScoutTheme.material.colorScheme.onSurface
            val modifier = Modifier.fillMaxWidth()

            if (hasImage) {
                val dangerColor = ScoutTheme.extras.colors.danger

                ScoutGhostButton(
                    modifier = modifier,
                    text = "Remove image",
                    contentColor = dangerColor,
                    prefixIcon = {
                        Icon(painter = painterResource(ScoutIcons.Trash), contentDescription = null, tint = dangerColor)
                    },
                    onClick = { onRemove(key) }

                )
                ScoutGhostButton(
                    modifier = modifier,
                    text = "Crop image",
                    contentColor = color,
                    prefixIcon = {
                        Icon(painter = painterResource(ScoutIcons.Crop), contentDescription = null, tint = color)
                    },
                    onClick = { onEdit(key) }

                )

                ScoutGhostButton(
                    modifier = modifier,
                    text = "Preview image",
                    contentColor = color,
                    prefixIcon = {
                        Icon(painter = painterResource(ScoutIcons.Image), contentDescription = null, tint = color)
                    },
                    onClick = { onPreview(key) }

                )
            } else {
                ScoutGhostButton(
                    modifier = modifier,
                    text = "Take a photo",
                    contentColor = color,
                    prefixIcon = {
                        Icon(
                            painter = painterResource(ScoutIcons.MobileCamera),
                            contentDescription = null,
                            tint = color
                        )
                    },
                    onClick = {
                        val uri = context.createTempUri()
                        tempUri = uri

                        // Check if camera permission is granted
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                            // Permission granted, launch camera
                            takePicture.launch(uri)
                        } else {
                            // Need to request permission first
                            shouldLaunchCamera = true
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )

                ScoutGhostButton(
                    modifier = modifier,
                    text = "Choose from gallery",
                    contentColor = color,
                    prefixIcon = {
                        Icon(painter = painterResource(ScoutIcons.Image), contentDescription = null, tint = color)
                    },
                    onClick = {
                        pickGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }
    }
}

