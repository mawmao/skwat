package com.humayapp.scout.core.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class InputFieldColors(
    val focusedColor: Color,
    val unfocusedColor: Color,
    val errorColor: Color,
    val disabledColor: Color,
) {

    companion object {

        @Composable
        fun default() = InputFieldColors(
            focusedColor = ScoutTheme.material.colorScheme.onBackground,
            unfocusedColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
            errorColor = ScoutTheme.material.colorScheme.onError,
            disabledColor = ScoutTheme.extras.colors.mutedOnSurfaceVariant
        )
    }
}

object InputFieldTokens {

    fun <T> fastSpatial(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.9f, stiffness = 1400f)

    val labelTextStyle @Composable get() = ScoutTheme.material.typography.bodyMedium

    val focusedColor @Composable get() = ScoutTheme.material.colorScheme.onBackground
    val unfocusedColor @Composable get() = ScoutTheme.material.colorScheme.onSurfaceVariant
    val errorColor @Composable get() = ScoutTheme.material.colorScheme.onError
    val disabledColor @Composable get() = ScoutTheme.extras.colors.mutedOnSurfaceVariant

    val focusedBorderThickness: Dp = 2.dp
    val unfocusedBorderThickness: Dp = 1.dp

}