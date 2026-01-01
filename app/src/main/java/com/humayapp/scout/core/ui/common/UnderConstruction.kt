package com.humayapp.scout.core.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.core.ui.theme.ScoutIcons

@Composable
fun UnderConstructionScreen(modifier: Modifier = Modifier) {
    Screen(modifier = modifier) {
        UnderConstructionWidget()
    }
}

@Composable
fun UnderConstructionWidget(
    scale: Float = 1f
) {
    val clampedScale = scale.coerceIn(0f, 1f)

    Icon(
        painter = painterResource(ScoutIcons.Construction),
        contentDescription = "Under Construction",
        modifier = Modifier.size(48.dp * clampedScale),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp * clampedScale))
    Text(
        text = "(つ•̀ꞈ•̀)つ",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 40.sp * clampedScale
    )
    Spacer(modifier = Modifier.height(8.dp * clampedScale))
    Text(
        text = "Under Construction",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp * clampedScale
    )
}
