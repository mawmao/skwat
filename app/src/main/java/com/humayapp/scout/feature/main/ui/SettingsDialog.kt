package com.humayapp.scout.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.LocalScoutAppState
import com.humayapp.scout.core.ui.component.ScoutDialog
import com.humayapp.scout.core.ui.component.ScoutTextButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.theme.ScoutTypography
import kotlinx.coroutines.launch

// TODO
//    - add setting to remember on next login

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onSignOut: () -> Unit
) {

    val appState = LocalScoutAppState.current
    val autoSync by appState.settingsRepository.getAutoSync().collectAsStateWithLifecycle(true)
    val scope = rememberCoroutineScope()

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
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Auto‑sync data", style = ScoutTypography.bodyMedium)
                        Switch(
                            modifier = Modifier.scale(0.8f),
                            checked = autoSync,
                            colors = SwitchDefaults.colors(
                               checkedThumbColor = ScoutTheme.extras.colors.white
                            ),
                            onCheckedChange = {
                                scope.launch {
                                    appState.settingsRepository.setAutoSync(it)
                                }
                            }
                        )
                    }
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
