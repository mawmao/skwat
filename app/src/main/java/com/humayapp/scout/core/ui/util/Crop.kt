package com.humayapp.scout.core.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max


data class CropState(
    var scale: Float = 1f,
    var offset: Offset = Offset.Zero,
) {

    fun applyBoxClamp(
        pan: Offset,
        zoom: Float,
        contentSize: Size,
        viewportPx: Float
    ): CropState {
        val minScale = if (viewportPx > 0f && contentSize.width > 0f && contentSize.height > 0f)
            max(viewportPx / contentSize.width, viewportPx / contentSize.height)
        else 0.5f

        val newScale = (scale * zoom).coerceIn(minimumValue = minScale, maximumValue = 5f)

        val newOffset = (offset + pan).clampOffsets(
            contentWidth = contentSize.width * newScale,
            contentHeight = contentSize.height * newScale,
            viewportPx = viewportPx
        )

        return copy(scale = newScale, offset = newOffset)
    }
}

suspend fun cropImage(
    context: Context,
    uri: Uri,
    scale: Float,
    contentOffset: Offset,
    containerSize: IntSize,
    contentSize: Size,
    cropBoxSizePx: Float,
    outputSize: IntSize,
): Uri? = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openInputStream(uri).use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream) ?: return@withContext null
            val intrinsicW = bitmap.width.toFloat()
            val intrinsicH = bitmap.height.toFloat()
            if (contentSize.width <= 0f || cropBoxSizePx <= 0f) return@withContext null

            val scaledImageWidth = contentSize.width * scale
            val scaledImageHeight = contentSize.height * scale

            val imageLeft = (containerSize.width - scaledImageWidth) / 2f + contentOffset.x
            val imageTop = (containerSize.height - scaledImageHeight) / 2f + contentOffset.y

            val cropLeft = (containerSize.width - cropBoxSizePx) / 2f
            val cropTop = (containerSize.height - cropBoxSizePx) / 2f

            val leftInScaled = (cropLeft - imageLeft).coerceIn(0f, scaledImageWidth)
            val topInScaled = (cropTop - imageTop).coerceIn(0f, scaledImageHeight)
            val cropWidthInScaled = cropBoxSizePx.coerceAtMost(scaledImageWidth - leftInScaled)
            val cropHeightInScaled = cropBoxSizePx.coerceAtMost(scaledImageHeight - topInScaled)

            val ratioX = intrinsicW / (contentSize.width * scale)
            val ratioY = intrinsicH / (contentSize.height * scale)
            val ratio = (ratioX + ratioY) / 2f

            val srcLeft = (leftInScaled * ratio).toInt().coerceIn(0, bitmap.width)
            val srcTop = (topInScaled * ratio).toInt().coerceIn(0, bitmap.height)
            val srcW = (cropWidthInScaled * ratio).toInt().coerceAtLeast(1)
                .coerceAtMost(bitmap.width - srcLeft)
            val srcH = (cropHeightInScaled * ratio).toInt().coerceAtLeast(1)
                .coerceAtMost(bitmap.height - srcTop)

            val cropped = Bitmap.createBitmap(bitmap, srcLeft, srcTop, srcW, srcH)
            val scaled = cropped.scale(outputSize.width, outputSize.height)

            val outFile =
                kotlin.io.path.createTempFile(directory = context.cacheDir.toPath(), prefix = "crop_", suffix = ".jpg")
                    .toFile()
            outFile.outputStream().use { outputStream ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }
            return@withContext Uri.fromFile(outFile)
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        return@withContext null
    }
}
