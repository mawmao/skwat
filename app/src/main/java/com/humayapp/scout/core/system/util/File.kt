package com.humayapp.scout.core.system.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

private const val TEMP_SUFFIX = ".tmp"

fun Context.createTempUri(): Uri {
    val prefix = "file_${System.currentTimeMillis()}"
    val authority = "${this.packageName}.provider"

    val tempFile = File.createTempFile(prefix, TEMP_SUFFIX, this.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(this, authority, tempFile)
}
