package com.humayapp.scout.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalScoutSpacing = staticCompositionLocalOf { ScoutSpacing() }
val LocalScoutShapes = staticCompositionLocalOf { ScoutShapes() }
val LocalScoutExtras = staticCompositionLocalOf { ScoutExtras() }

@Immutable
data class ScoutExtras(
    val colors: Colors = Colors()
) {
    @Immutable
    data class Colors(
        val mutedOnBackground: Color = ScoutColors.Gray800,
        val mutedOnSurfaceVariant: Color = ScoutColors.MutedGray500,
        val logoDark: Color = ScoutColors.ScoutLogoGreenDark,
        val logoLight: Color = ScoutColors.ScoutLogoGreenLight,
        val danger: Color = ScoutColors.Red500,
        val white: Color = ScoutColors.White,
        val hoveredWhite: Color = ScoutColors.Gray150,
        val warning: Color = ScoutColors.Amber
    )
}


@Immutable
data class ScoutShapes(
    val cornerSmall: RoundedCornerShape = RoundedCornerShape(4.dp),
    val cornerMedium: RoundedCornerShape = RoundedCornerShape(8.dp),
    val cornerMediumLarge: RoundedCornerShape = RoundedCornerShape(12.dp),
    val cornerLarge: RoundedCornerShape = RoundedCornerShape(16.dp)
)

@Immutable
data class ScoutSpacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val smallMedium: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val mediumLarge: Dp = 20.dp,
    val large: Dp = 24.dp,
    val largeExtraLarge: Dp = 28.dp,
    val extraLarge: Dp = 32.dp,
    val margin: Dp = medium
)


