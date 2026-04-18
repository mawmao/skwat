package com.humayapp.scout.feature.main.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.ui.common.ConnectivityIndicator
import com.humayapp.scout.core.ui.component.ScoutIconButton
import com.humayapp.scout.core.ui.component.ScoutLogo
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.main.approved.api.navigation.ApprovedNavKey
import com.humayapp.scout.feature.main.collected.api.navigation.CollectedNavKey
import com.humayapp.scout.feature.main.pending.api.navigation.PendingNavKey
import com.humayapp.scout.feature.main.rejected.api.navigation.RejectedNavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSectionTopAppBar(
    currentKey: NavKey,
    modifier: Modifier = Modifier,
    isOnline: Boolean,
    onProfileClick: () -> Unit,
    canRefresh: Boolean = false,
    isRefreshing: Boolean = false,
    onRefreshClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadCount: Int = 0,
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
                        PendingNavKey -> {
                            ScoutLogo(size = maxTitleHeight)
                            Text("Scout", style = ScoutTheme.material.typography.headlineMedium)
                        }

                        CollectedNavKey -> {
                            ScreenHeader(
                                title = "Collected Forms",
                                color = Color(0xFF2196F3)
                            )
                        }

                        ApprovedNavKey -> {
                            ScreenHeader(
                                title = "Approved Forms",
                                color = Color(0xFF4CAF50)
                            )
                        }

                        RejectedNavKey -> {
                            ScreenHeader(
                                title = "Rejected Forms",
                                color = Color(0xFFF44336)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(ScoutTheme.spacing.small))
                    ConnectivityIndicator(
                        isOffline = !isOnline,
                        hideLabel = true,
                        iconSize = 15.dp
                    )
                }
            }
        },
        actions = {
            Row(
                modifier = Modifier.height(maxTitleHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isRefreshing) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = LinearEasing)
                        )
                    )
                    ScoutIconButton(
                        onClick = { },
                        icon = ScoutIcons.Sync,
                        contentDescription = "Refreshing",
                        enabled = false,
                        modifier = Modifier.rotate(rotation)
                    )
                } else {
                    ScoutIconButton(
                        enabled = canRefresh,
                        onClick = onRefreshClick,
                        icon = ScoutIcons.Sync,
                        contentDescription = "Refresh Icon Button"
                    )
                }

                Box {
                    ScoutIconButton(
                        onClick = onNotificationsClick,
                        icon = ScoutIcons.Notification,
                        contentDescription = "Notifications"
                    )
                    if (unreadCount > 0) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                        ) {
                            Text(if (unreadCount > 99) "99+" else unreadCount.toString())
                        }
                    }
                }

                ScoutIconButton(
                    onClick = onProfileClick,
                    icon = ScoutIcons.AccountCircle,
                    contentDescription = "Profile"
                )
            }
        }
    )
}


@Composable
fun ScreenHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = ScoutTheme.margin, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(24.dp)
                .background(color, RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = ScoutTheme.material.typography.headlineMedium
        )
    }
}