package com.humayapp.scout.core.ui.util

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

object ScoutSizeUtils {
    fun scaleToFit(contentSize: Size, containerSize: IntSize): Size {
        if (contentSize.width > 0f && contentSize.height > 0f && containerSize.width > 0 && containerSize.height > 0) {
            val fitScale = min(
                containerSize.width.toFloat() / contentSize.width,
                containerSize.height.toFloat() / contentSize.height
            )
            return Size(
                width = contentSize.width * fitScale,
                height = contentSize.height * fitScale
            )
        }
        return Size(
            width = containerSize.width.toFloat().coerceAtLeast(1f),
            height = containerSize.height.toFloat().coerceAtLeast(1f)
        )
    }
}
