package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.ui.theme.ScoutTheme

@Composable
fun WizardProgressBar(
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    totalCount: Int,
    currentCount: Int,
    backgroundColor: Color = ScoutTheme.material.colorScheme.secondary,
    progressColor: Color = ScoutTheme.material.colorScheme.primary,
) {
    val targetProgress = if (totalCount > 0) (currentCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = NavTransition.DEFAULT_NAV_DURATION, easing = NavTransition.screenTransitionEasing),
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        drawRect(color = backgroundColor, size = size)
        drawRect(color = progressColor, size = Size(width = size.width * animatedProgress, height = size.height))
    }
}
