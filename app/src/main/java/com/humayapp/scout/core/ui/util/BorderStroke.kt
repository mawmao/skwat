package com.humayapp.scout.core.ui.util

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.Dp
import com.humayapp.scout.core.ui.theme.InputFieldColors
import com.humayapp.scout.core.ui.theme.InputFieldTokens.focusedBorderThickness
import com.humayapp.scout.core.ui.theme.InputFieldTokens.unfocusedBorderThickness


@Composable
fun animateInputFieldBorderStrokeAsState(
    enabled: Boolean,
    selected: Boolean,
    isError: Boolean,
    colors: InputFieldColors,
): State<BorderStroke> {

    val targetColor = when {
        !enabled -> colors.disabledColor
        isError -> colors.errorColor
        selected -> colors.focusedColor
        else -> colors.unfocusedColor
    }

    val color = if (enabled) animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring(dampingRatio = 1f, stiffness = 3800f)
    ) else rememberUpdatedState(targetColor)

    val thickness = if (enabled) animateDpAsState(
        targetValue = if (selected || isError) focusedBorderThickness else unfocusedBorderThickness,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 1400f)
    ) else rememberUpdatedState(unfocusedBorderThickness)

    return rememberUpdatedState(BorderStroke(width = thickness.value, color = color.value))
}


@Composable
fun animateBorderStrokeAsState(
    enabled: Boolean,
    isError: Boolean,
    focused: Boolean,
    colors: TextFieldColors,
): State<BorderStroke> {

    val targetColor = when {
        !enabled -> colors.disabledIndicatorColor
        isError -> colors.errorIndicatorColor
        focused -> colors.focusedIndicatorColor
        else -> colors.unfocusedIndicatorColor
    }

    val indicatorColor = if (enabled) animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring(dampingRatio = 1.0f, stiffness = 3800.0f)
    ) else rememberUpdatedState(targetColor)

    val thickness = if (enabled) animateDpAsState(
        targetValue = if (focused || isError) focusedBorderThickness else unfocusedBorderThickness,
        animationSpec = spring<Dp>(dampingRatio = 0.9f, stiffness = 1400.0f)
    ) else rememberUpdatedState(unfocusedBorderThickness)

    return rememberUpdatedState(BorderStroke(thickness.value, indicatorColor.value))
}

