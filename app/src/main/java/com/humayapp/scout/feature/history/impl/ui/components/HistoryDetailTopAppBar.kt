package com.humayapp.scout.feature.history.impl.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.humayapp.scout.core.ui.component.ScoutIconButton
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailTopAppBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "History Detail",
                style = ScoutTheme.material.typography.headlineSmall,
                color = ScoutTheme.extras.colors.mutedOnBackground
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            ScoutIconButton(
                onClick = onBack,
                icon = ScoutIcons.Back,
                contentDescription = null,
                tint = ScoutTheme.extras.colors.mutedOnBackground
            )
        }
    )
}