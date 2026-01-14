package com.humayapp.scout.core.ui.util

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import com.humayapp.scout.core.ui.theme.ScoutTheme


fun Modifier.scoutClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "scoutClickable"
        properties["onClick"] = onClick
        properties["enabled"] = enabled
    }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            ScoutTheme.extras.colors.hoveredWhite
        } else {
            Color.Transparent
        },
        label = "scoutClickable"
    )

    background(backgroundColor).clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}
