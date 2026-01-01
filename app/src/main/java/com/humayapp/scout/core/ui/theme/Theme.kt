package com.humayapp.scout.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import com.humayapp.scout.core.ui.theme.ScoutColors.Gray100
import com.humayapp.scout.core.ui.theme.ScoutColors.Gray300
import com.humayapp.scout.core.ui.theme.ScoutColors.Gray500
import com.humayapp.scout.core.ui.theme.ScoutColors.Gray700
import com.humayapp.scout.core.ui.theme.ScoutColors.Gray900
import com.humayapp.scout.core.ui.theme.ScoutColors.Red100
import com.humayapp.scout.core.ui.theme.ScoutColors.Red500
import com.humayapp.scout.core.ui.theme.ScoutColors.ScoutGreen
import com.humayapp.scout.core.ui.theme.ScoutColors.White

private val LightColorScheme = lightColorScheme(
    background = Gray100,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray700,
    onSurfaceVariant = Gray500,
    primary = ScoutGreen,
    onPrimary = Gray500,
    secondary = Gray300,
    error = Red100,
    onError = Red500
)


object ScoutTheme {
    val material: MaterialTheme
        @Composable @ReadOnlyComposable get() = MaterialTheme

    val extras: ScoutExtras
        @Composable @ReadOnlyComposable get() = LocalScoutExtras.current

    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = Spacing()

    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = Shapes()

    val margin: Dp
        @Composable @ReadOnlyComposable get() = spacing.medium
}

@Composable
fun ScoutTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalScoutExtras provides ScoutExtras()
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = ScoutTypography,
            content = content
        )
    }
}