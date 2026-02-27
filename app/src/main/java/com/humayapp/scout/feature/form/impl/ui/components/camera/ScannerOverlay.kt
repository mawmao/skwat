package com.humayapp.scout.feature.form.impl.ui.components.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = Color.White,
    cornerLineLength: Dp = 32.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            val strokePx = strokeWidth.toPx()
            val radius = cornerRadius.toPx()
            val lineLen = cornerLineLength.toPx()
            val inset = strokePx / 2f

            val stroke = Stroke(width = strokePx)

            drawScannerArcs(
                inset = inset,
                radius = radius,
                stroke = stroke,
                width = w,
                height = h,
                color = color
            )
            drawScannerEdges(
                inset = inset,
                radius = radius,
                strokeWidth = strokePx,
                lineLength = lineLen,
                width = w,
                height = h,
                color = color
            )
        }
    }
}


fun DrawScope.drawScannerArcs(
    inset: Float,
    radius: Float,
    stroke: Stroke,
    width: Float,
    height: Float,
    color: Color = Color.White,
) {
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 90f,
        useCenter = false,
        style = stroke,
        topLeft = Offset(inset, inset),
        size = Size(radius * 2, radius * 2)
    )
    // top-right
    drawArc(
        color = color,
        startAngle = 270f,
        sweepAngle = 90f,
        useCenter = false,
        style = stroke,
        topLeft = Offset(width - 2 * radius - inset, inset),
        size = Size(radius * 2, radius * 2)
    )
    // bottom-left
    drawArc(
        color = color,
        startAngle = 90f,
        sweepAngle = 90f,
        useCenter = false,
        style = stroke,
        topLeft = Offset(inset, height - 2 * radius - inset),
        size = Size(radius * 2, radius * 2)
    )
    // bottom-right
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 90f,
        useCenter = false,
        style = stroke,
        topLeft = Offset(width - 2 * radius - inset, height - 2 * radius - inset),
        size = Size(radius * 2, radius * 2)
    )
}

fun DrawScope.drawScannerEdges(
    inset: Float,
    radius: Float,
    strokeWidth: Float,
    lineLength: Float,
    width: Float,
    height: Float,
    color: Color = Color.White,
) {
    drawLine(
        color = color,
        start = Offset(inset + radius, inset),
        end = Offset(inset + radius + lineLength, inset),
        strokeWidth = strokeWidth
    )
    // top-left vertical
    drawLine(
        color = color,
        start = Offset(inset, inset + radius),
        end = Offset(inset, inset + radius + lineLength),
        strokeWidth = strokeWidth
    )

    // top-right horizontal
    drawLine(
        color = color,
        start = Offset(width - inset - radius, inset),
        end = Offset(width - inset - radius - lineLength, inset),
        strokeWidth = strokeWidth
    )
    // top-right vertical
    drawLine(
        color = color,
        start = Offset(width - inset, inset + radius),
        end = Offset(width - inset, inset + radius + lineLength),
        strokeWidth = strokeWidth
    )

    // bottom-left horizontal
    drawLine(
        color = color,
        start = Offset(inset + radius, height - inset),
        end = Offset(inset + radius + lineLength, height - inset),
        strokeWidth = strokeWidth
    )
    // bottom-left vertical
    drawLine(
        color = color,
        start = Offset(inset, height - inset - radius),
        end = Offset(inset, height - inset - radius - lineLength),
        strokeWidth = strokeWidth
    )

    // bottom-right horizontal
    drawLine(
        color = color,
        start = Offset(width - inset - radius, height - inset),
        end = Offset(width - inset - radius - lineLength, height - inset),
        strokeWidth = strokeWidth
    )
    // bottom-right vertical
    drawLine(
        color = color,
        start = Offset(width - inset, height - inset - radius),
        end = Offset(width - inset, height - inset - radius - lineLength),
        strokeWidth = strokeWidth
    )
}
