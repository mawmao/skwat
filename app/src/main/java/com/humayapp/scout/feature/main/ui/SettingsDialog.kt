package com.humayapp.scout.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.ui.common.UnderConstructionWidget
import com.humayapp.scout.core.ui.component.ScoutDialog
import com.humayapp.scout.core.ui.component.ScoutTextButton
import com.humayapp.scout.core.ui.theme.ScoutTheme

// TODO
//    - add setting to remember on next login

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onSignOut: () -> Unit
) {
    ScoutDialog(
        isVisible = isVisible,
        onDismiss = onDismissRequest,
        content = {
            Column(
                modifier = Modifier.padding(ScoutTheme.spacing.large)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
                ) {
                    Text(
                        text = "Settings",
                        style = ScoutTheme.material.typography.headlineSmall,
                        color = ScoutTheme.extras.colors.mutedOnBackground
                    )
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.6f))
                }
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .padding(ScoutTheme.spacing.medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UnderConstructionWidget(scale = 0.8f)
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium),
                    horizontalAlignment = Alignment.End
                ) {
                    ScoutTextButton(
                        text = "Sign Out",
                        color = ScoutTheme.extras.colors.danger,
                        letterSpacing = (0.5).sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        onClick = onSignOut,
                    )
                }
            }
        }
    )
}
