package com.humayapp.scout.core.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
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
        // Read EXIF orientation
        val exifOrientation = context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        // Decode bitmap
        val bitmap = context.contentResolver.openInputStream(uri).use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return@withContext null

        // Apply EXIF rotation to match what's displayed
        val rotatedBitmap = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> bitmap.rotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> bitmap.rotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> bitmap.rotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> bitmap.flip(horizontal = true)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> bitmap.flip(vertical = true)
            ExifInterface.ORIENTATION_TRANSPOSE -> bitmap.rotate(90f).flip(horizontal = true)
            ExifInterface.ORIENTATION_TRANSVERSE -> bitmap.rotate(270f).flip(horizontal = true)
            else -> bitmap
        }

        // If we created a new bitmap, recycle the old one
        if (rotatedBitmap !== bitmap) {
            bitmap.recycle()
        }

        val intrinsicW = rotatedBitmap.width.toFloat()
        val intrinsicH = rotatedBitmap.height.toFloat()

        if (contentSize.width <= 0f || cropBoxSizePx <= 0f) {
            rotatedBitmap.recycle()
            return@withContext null
        }

        // Calculate scaled image dimensions
        val scaledImageWidth = contentSize.width * scale
        val scaledImageHeight = contentSize.height * scale

        // Calculate image position in container
        val imageLeft = (containerSize.width - scaledImageWidth) / 2f + contentOffset.x
        val imageTop = (containerSize.height - scaledImageHeight) / 2f + contentOffset.y

        // Calculate crop box position in container
        val cropLeft = (containerSize.width - cropBoxSizePx) / 2f
        val cropTop = (containerSize.height - cropBoxSizePx) / 2f

        // Calculate crop box position relative to the scaled image
        val leftInScaled = (cropLeft - imageLeft).coerceIn(0f, scaledImageWidth)
        val topInScaled = (cropTop - imageTop).coerceIn(0f, scaledImageHeight)
        val cropWidthInScaled = cropBoxSizePx.coerceAtMost(scaledImageWidth - leftInScaled)
        val cropHeightInScaled = cropBoxSizePx.coerceAtMost(scaledImageHeight - topInScaled)

        // Calculate ratio from scaled displayed size to intrinsic bitmap size
        val ratioX = intrinsicW / scaledImageWidth
        val ratioY = intrinsicH / scaledImageHeight

        // Map to bitmap coordinates
        val srcLeft = (leftInScaled * ratioX).toInt().coerceIn(0, rotatedBitmap.width - 1)
        val srcTop = (topInScaled * ratioY).toInt().coerceIn(0, rotatedBitmap.height - 1)

        val maxAvailableW = rotatedBitmap.width - srcLeft
        val maxAvailableH = rotatedBitmap.height - srcTop

        val srcW = (cropWidthInScaled * ratioX).toInt().coerceIn(1, maxAvailableW)
        val srcH = (cropHeightInScaled * ratioY).toInt().coerceIn(1, maxAvailableH)

        if (srcW <= 0 || srcH <= 0) {
            Log.e("Scout: Crop", "Invalid crop dimensions: w=$srcW, h=$srcH")
            rotatedBitmap.recycle()
            return@withContext null
        }

        Log.v("Scout: Crop", "EXIF orientation: $exifOrientation")
        Log.v("Scout: Crop", "Bitmap: ${rotatedBitmap.width}x${rotatedBitmap.height}, " +
                "Crop region: left=$srcLeft, top=$srcTop, w=$srcW, h=$srcH")

        val cropped = Bitmap.createBitmap(rotatedBitmap, srcLeft, srcTop, srcW, srcH)
        rotatedBitmap.recycle()

        val scaled = cropped.scale(outputSize.width, outputSize.height)
        cropped.recycle()

        val outFile = kotlin.io.path.createTempFile(
            directory = context.cacheDir.toPath(),
            prefix = "crop_",
            suffix = ".jpg"
        ).toFile()

        outFile.outputStream().use { outputStream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }
        scaled.recycle()

        return@withContext Uri.fromFile(outFile)
    } catch (t: Throwable) {
        t.printStackTrace()
        Log.e("Scout: Crop", "${t.localizedMessage}")
        return@withContext null
    }
}

// Helper extension functions
private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun Bitmap.flip(horizontal: Boolean = false, vertical: Boolean = false): Bitmap {
    val matrix = Matrix().apply {
        postScale(
            if (horizontal) -1f else 1f,
            if (vertical) -1f else 1f,
            width / 2f,
            height / 2f
        )
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
