package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScoutTopAppBar(
    modifier: Modifier = Modifier,
    onBackTap: (() -> Unit)? = null,
    onBackDoubleTap: (() -> Unit)? = null,
    onBackLongTap: (() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    paddingValues: PaddingValues = PaddingValues(vertical = 16.dp)
) {
    ScoutTopAppBarImpl(
        modifier = modifier,
        onBackTap = onBackTap,
        onBackDoubleTap = onBackDoubleTap,
        onBackLongTap = onBackLongTap,
        title = title,
        actions = actions,
        contentColor = contentColor,
        paddingValues = paddingValues
    )
}

@Composable
private fun ScoutTopAppBarImpl(
    modifier: Modifier = Modifier,
    onBackTap: (() -> Unit)? = null,
    onBackDoubleTap: (() -> Unit)? = null,
    onBackLongTap: (() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    paddingValues: PaddingValues = PaddingValues(vertical = 16.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(paddingValues = paddingValues),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackTap != null) {
            Box(
                Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    ScoutIconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = onBackTap,
                        onDoubleTap = onBackDoubleTap,
                        onLongPress = onBackLongTap,
                        contentDescription = "Navigate Back Icon Button",
                        tint = contentColor
                    )
                }
            }
        }

        if (title != null) {
            Box(
                Modifier.weight(1f),
                contentAlignment = if (onBackTap != null) Alignment.Center else Alignment.CenterStart
            ) {
                title()
            }
        }

        Row(
            modifier = Modifier.then(if (title != null) Modifier.weight(1f) else Modifier),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = { actions?.invoke(this) }
        )
    }
}
