package com.humayapp.scout.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object ScoutColors {

    val White = Color.White

    val Gray100 = Color(0xFFF2F2F7) // background
    val Gray300 = Color(0xFFD9D9D9) // secondary

    val Gray500 = Color(0xFFA3A3A4) // onSurfaceVariant
    val MutedGray500 = Color(0xD9A3A3A4) // 0.85 alpha

    val Gray700 = Color(0xFF555556) // onSurface
    val Gray800 = Color(0xD91C1C1E) // mutedOnBackground
    val Gray900 = Color(0xFF1C1C1E) // onBackground

    val Red100 = Color(0xFFFEECEC) // error
    val Red500 = Color(0xFFC22323) // OnError

    val HumayGreen = Color(0XFF00A63E)
    val ScoutGreen = Color.hsl(hue = 120f, saturation = 0.45f, lightness = 0.55f)
    val ScoutLogoGreenDark = Color(0xFF006633)
    val ScoutLogoGreenLight = Color(0xFF009A4D)
}

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
    )
}

val LocalScoutExtras = staticCompositionLocalOf { ScoutExtras() }

