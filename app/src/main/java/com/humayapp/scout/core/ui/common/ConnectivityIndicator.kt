package com.humayapp.scout.core.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.ui.theme.ScoutColors.HumayGreen
import com.humayapp.scout.core.ui.theme.ScoutColors.Red500
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.theme.ScoutTypography

@Composable
fun ConnectivityIndicator(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    hideLabel: Boolean = false,
    iconSize: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    val statusColor = if (isOffline) Red500 else HumayGreen
    val statusText = if (isOffline) "Offline" else "Online"

    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            painter = painterResource(if (isOffline) ScoutIcons.WifiOff else ScoutIcons.Wifi),
            contentDescription = statusText,
            tint = statusColor,
        )
        if (!hideLabel) {
            Text(
                text = statusText,
                color = ScoutTheme.extras.colors.mutedOnSurfaceVariant,
                style = ScoutTypography.labelSmall,
                fontSize = 11.sp,
                letterSpacing = (0.5).sp
            )
        }
    }
}
