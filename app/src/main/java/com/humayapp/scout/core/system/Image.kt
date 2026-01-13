package com.humayapp.scout.core.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Clock


fun Context.getFormImageFolder(
    seasonId: Int,
    activityType: String,
    mfid: String,
    timestamp: String = Clock.System.now().toString()
): File {
    val safeTimestamp = timestamp.replace(":", "-") // file system safe
    val folder = File(filesDir, "forms/$seasonId/$activityType/$mfid/$safeTimestamp")
    if (!folder.exists()) folder.mkdirs()
    return folder
}

fun Context.saveImagesToFolder(
    answers: Map<String, Any?>,
    folder: File,
    maxDim: Int = 1440,
    quality: Int = 80,
): Map<String, Any?> {
    return answers.mapValues { (key, value) ->
        if (key.startsWith("img_") && value is String) {
            val uri = value.toUri()
            val outFile = File(folder, "$key.webp")

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            val (w, h) = options.outWidth to options.outHeight
            var sampleSize = 1
            if (w > 0 && h > 0) {
                val largest = maxOf(w, h)
                while (largest / sampleSize > maxDim) sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val savedPath = contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }?.let { bmp ->
                FileOutputStream(outFile).use { out ->
                    bmp.compress(Bitmap.CompressFormat.WEBP, quality, out)
                }
                bmp.recycle()
                outFile.absolutePath
            }

            savedPath ?: value
        } else value
    }
}
