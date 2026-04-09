package com.humayapp.scout.core.ui.common.image

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.compose.rememberAsyncImagePainter
import com.humayapp.scout.core.ui.util.CropState
import com.humayapp.scout.core.ui.util.ScoutSizeUtils
import com.humayapp.scout.core.ui.util.cropImage
import kotlinx.coroutines.launch

private const val LOG_TAG = "Scout: ImageCropDialog"

@Composable
fun ImageCropDialog(
    uri: String,
    onDismiss: () -> Unit,
    onCropComplete: (Uri) -> Unit
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val painter = rememberAsyncImagePainter(model = uri.toUri())
    val painterState by painter.state.collectAsStateWithLifecycle()

    val imageIntrinsicSize = if (painterState is AsyncImagePainter.State.Success) {
        val successState = painterState as AsyncImagePainter.State.Success
        Size(width = successState.result.image.width.toFloat(), height = successState.result.image.height.toFloat())
    } else Size.Zero

    var displayedImageSize by remember { mutableStateOf(Size.Zero) }
    var cropBoxSize by remember { mutableFloatStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val cropState = remember { mutableStateOf(CropState()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CropToolbar(
                onCancel = {
                    Log.v(LOG_TAG, "Cancel clicked")
                    onDismiss()
                },
                onDone = {
                    Log.v(LOG_TAG, "Done clicked")
                    Log.v(
                        LOG_TAG,
                        "Crop params scale=${cropState.value.scale} offset=${cropState.value.offset} " +
                                "container=$containerSize content=$displayedImageSize cropBox=$cropBoxSize"
                    )
                    scope.launch {
                        Log.v(LOG_TAG, "Crop coroutine start")
                        val cropped = cropImage(
                            context = context,
                            uri = uri.toUri(),
                            scale = cropState.value.scale,
                            contentOffset = cropState.value.offset,
                            containerSize = containerSize,
                            contentSize = displayedImageSize,
                            cropBoxSizePx = cropBoxSize,
                            outputSize = IntSize(600, 600)
                        )
                        Log.v(LOG_TAG, "cropImage result=$cropped")

                        if (cropped != null) {
                            onCropComplete(cropped)
                        } else {
                            Log.e(LOG_TAG, "cropImage returned null")
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .onGloballyPositioned { coordinates ->
                        Log.v(LOG_TAG, "Container size=$containerSize")
                        containerSize = coordinates.size
                    },
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(imageIntrinsicSize, containerSize) {
                    displayedImageSize = ScoutSizeUtils.scaleToFit(imageIntrinsicSize, containerSize)
                }

                SubcomposeAsyncImage(
                    model = uri,
                    contentDescription = "Image to crop",
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            Modifier.pointerInput(imageIntrinsicSize, cropBoxSize) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    cropState.value = cropState.value.applyBoxClamp(
                                        pan = pan,
                                        zoom = zoom,
                                        contentSize = displayedImageSize,
                                        viewportPx = cropBoxSize
                                    )
                                }
                            }
                        )
                        .graphicsLayer(
                            scaleX = cropState.value.scale,
                            scaleY = cropState.value.scale,
                            translationX = cropState.value.offset.x,
                            translationY = cropState.value.offset.y
                        ),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                ) {
                    SubcomposeAsyncImageContent()
                }

                CropBoxOverlay { size -> cropBoxSize = size.minDimension }
            }

            Text(
                text = "Pinch to zoom - Drag to reposition",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    BackHandler { onDismiss() }
}

@Composable
private fun CropToolbar(onCancel: () -> Unit, onDone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancel) { Text("Cancel", color = Color.White) }
        Text("Crop Image", color = Color.White, fontWeight = FontWeight.Bold)
        TextButton(onClick = onDone) { Text("Done", color = MaterialTheme.colorScheme.primary) }
    }
}


@Composable
private fun CropBoxOverlay(onCropSizeCalculated: (Size) -> Unit) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cropSize = size.minDimension * 0.8f
        val left = (size.width - cropSize) / 2f
        val top = (size.height - cropSize) / 2f

        drawRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset.Zero, size = Size(size.width, top))
        drawRect(
            Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, top + cropSize),
            size = Size(size.width, size.height - top - cropSize)
        )
        drawRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset(0f, top), size = Size(left, cropSize))
        drawRect(
            Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(left + cropSize, top),
            size = Size(size.width - left - cropSize, cropSize)
        )

        drawRoundRect(
            color = Color.White,
            cornerRadius = CornerRadius(16f, 16f),
            topLeft = Offset(left, top),
            size = Size(cropSize, cropSize),
            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(32f, 16f)))
        )

        onCropSizeCalculated(Size(cropSize, cropSize))
    }
}

