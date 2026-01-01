package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@Composable
fun ScoutFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    fabIcon: Painter,
    fabSize: Dp = ScoutFABDefaults.size,
    ringGap: Dp = ScoutFABDefaults.ringGap,
    ringThickness: Dp = ScoutFABDefaults.ringThickness,
    colors: ScoutFABColors = ScoutFABDefaults.colors(),
) {
    Box(
        modifier = modifier
            .size(fabSize + 2F * (ringGap + ringThickness))
            .drawBehind {
                val outerRadius = size.minDimension / 2
                val innerRadius = outerRadius - ringThickness.toPx()
                drawCircle(color = colors.ring, radius = outerRadius)
                drawCircle(color = colors.ringGap, radius = innerRadius)
            },
        contentAlignment = Alignment.Center
    ) {
        LargeFloatingActionButton(
            modifier = Modifier.size(fabSize),
            onClick = onClick,
            shape = CircleShape,
            containerColor = colors.fab,
            elevation = ScoutFABDefaults.elevation()
        ) {
            Icon(
                painter = fabIcon,
                contentDescription = null,
                tint = colors.icon,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Stable
data class ScoutFABColors(
    val fab: Color,
    val icon: Color,
    val ringGap: Color,
    val ring: Color,
)

object ScoutFABDefaults {

    val size: Dp = 48.dp
    val ringGap: Dp = 2.dp
    val ringThickness: Dp = 2.dp

    @Composable
    fun colors(
        fab: Color = MaterialTheme.colorScheme.primary,
        icon: Color = Color.White,
        ringGap: Color = Color.White,
        ring: Color = fab,
    ): ScoutFABColors = ScoutFABColors(fab = fab, icon = icon, ringGap = ringGap, ring = ring)

    @Composable
    fun elevation(): FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        hoveredElevation = 0.dp,
    )
}