package com.humayapp.scout.core.ui.util

import androidx.compose.ui.geometry.Offset

fun Offset.clampOffsets(
    contentWidth: Float,
    contentHeight: Float,
    viewportPx: Float // 1x1
): Offset {

    val excessWidth = (contentWidth - viewportPx) / 2f
    val excessHeight = (contentHeight - viewportPx) / 2f

    val x = if (contentWidth >= viewportPx) this.x.coerceIn(-excessWidth, excessWidth) else 0f
    val y = if (contentHeight >= viewportPx) this.y.coerceIn(-excessHeight, excessHeight) else 0f

    return Offset(x, y)
}
