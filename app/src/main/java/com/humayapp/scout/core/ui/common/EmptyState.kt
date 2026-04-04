package com.humayapp.scout.core.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.ui.theme.ScoutTheme

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    message: String,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "(つ•̀ꞈ•̀)つ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.height(ScoutTheme.spacing.smallMedium))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}