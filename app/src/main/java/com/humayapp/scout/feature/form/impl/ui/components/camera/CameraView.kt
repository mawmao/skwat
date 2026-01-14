package com.humayapp.scout.feature.form.impl.ui.components.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraScanner(
    surfaceRequest: SurfaceRequest?,
    modifier: Modifier = Modifier,
    bindToCamera: (LifecycleOwner) -> Unit,
    topBar: @Composable () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        bindToCamera(lifecycleOwner)
    }

    Box(modifier = modifier.fillMaxSize()) {

        surfaceRequest?.let {
            CameraXViewfinder(
                surfaceRequest = it,
                modifier = Modifier.fillMaxSize()
            )
        }

        ScannerOverlay(
            modifier = Modifier
                .size(220.dp, 200.dp)
                .align(Alignment.Center),
            cornerRadius = 24.dp,
            strokeWidth = 5.dp,
            color = Color.White,
            cornerLineLength = 16.dp
        )
//        Column(modifier = Modifier.align(Alignment.TopCenter)) {
//            topBar()
//        }
    }
}

