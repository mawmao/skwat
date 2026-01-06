package com.humayapp.scout.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// todo: review

@Composable
fun ScoutLabel(
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean? = null,
    space: Dp? = 6.dp,
) {
    val targetColor =
        when (active) {
            true -> MaterialTheme.colorScheme.onSurface
            false -> MaterialTheme.colorScheme.onSurfaceVariant
            null -> LocalContentColor.current
        }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 180)
    )

    Column {
        Text(
            text = label,
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
            color = animatedColor
        )

        space?.let { Spacer(Modifier.height(it)) }
    }
}
