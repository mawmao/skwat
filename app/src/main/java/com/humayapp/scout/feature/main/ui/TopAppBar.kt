package com.humayapp.scout.feature.main.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.ui.component.ScoutIconButton
import com.humayapp.scout.core.ui.component.ScoutLogo
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.history.api.navigation.HistoryNavKey
import com.humayapp.scout.feature.main.home.api.navigation.HomeNavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSectionTopAppBar(
    currentKey: NavKey,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    onSyncClick: () -> Unit,
) {
    val maxTitleHeight = 48.dp

    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {
            AnimatedContent(
                targetState = currentKey,
                label = "TopBarTitle",
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150)) using
                            SizeTransform(clip = false)
                }
            ) { key ->
                Row(
                    modifier = Modifier.height(maxTitleHeight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    when (key) {
                        HomeNavKey -> {
                            ScoutLogo(size = maxTitleHeight)
                            Text("Scout", style = ScoutTheme.material.typography.headlineMedium)
                        }

                        HistoryNavKey -> {
                            Text("History", style = ScoutTheme.material.typography.headlineMedium)
                        }
                    }
                }
            }
        },
        actions = {
            AnimatedContent(
                targetState = currentKey,
                label = "TopBarIcon",
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150)) using
                            SizeTransform(clip = false)
                }
            ) { key ->
                Row(
                    modifier = Modifier.height(maxTitleHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (key) {
                        HomeNavKey -> {
                            ScoutIconButton(
                                onClick = onSyncClick,
                                icon = ScoutIcons.Sync,
                                contentDescription = "Sync Icon Button"
                            )
                        }

                        HistoryNavKey -> {
                            ScoutIconButton(
                                onClick = {},
                                icon = ScoutIcons.Sort,
                                contentDescription = "Sort Icon Button"
                            )
                        }
                    }
                    ScoutIconButton(
                        onClick = onSettingsClick,
                        icon = ScoutIcons.Settings,
                        contentDescription = "Settings Icon Button"
                    )
                }
            }
        }
    )
}

