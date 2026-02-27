package com.humayapp.scout.feature.form.impl.ui.components.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme

@Composable
fun CameraScanner(
    surfaceRequest: SurfaceRequest?,
    modifier: Modifier = Modifier,
    isTorchOn: Boolean,
    isCameraReady: Boolean,
    onTorchPress: () -> Unit,
) {


    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(contentAlignment = Alignment.Center) {
            Box(modifier = modifier.background(Color.Black)) {
                surfaceRequest?.let {
                    key(it) {
                        CameraXViewfinder(
                            surfaceRequest = it,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            if (!isCameraReady) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }

            this@Column.AnimatedVisibility(
                visible = isCameraReady,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ScannerOverlay(
                    modifier = Modifier.size(250.dp),
                    cornerRadius = 24.dp,
                    strokeWidth = 2.dp,
                    color = Color.White,
                    cornerLineLength = 16.dp
                )
            }
        }

        Spacer(Modifier.height(64.dp))

        AnimatedVisibility(
            visible = isCameraReady,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            TorchButton(isTorchOn = isTorchOn, onClick = onTorchPress)
        }
    }
}


@Composable
fun TorchButton(
    modifier: Modifier = Modifier,
    isTorchOn: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(if (isTorchOn) Color.Black else Color.White)
            .clickable(onClick = onClick)
            .padding(ScoutTheme.spacing.smallMedium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(ScoutIcons.Flashlight),
            contentDescription = null,
            tint = if (isTorchOn) Color.White else Color.Black
        )
    }
}
