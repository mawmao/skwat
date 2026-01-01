package com.humayapp.scout.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator

object ScoutNavigationDefaults {

    val transparentColor = Color.Transparent

    @Composable
    fun itemLabelStyle() = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp)
    @Composable
    fun itemColors(): NavigationBarItemColors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15F),
        selectedIconColor = MaterialTheme.colorScheme.onBackground,
        selectedTextColor = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun ScoutDefaultNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier.padding(horizontal = 20.dp),
        tonalElevation = 0.dp,
        contentColor = ScoutNavigationDefaults.transparentColor,
        containerColor = ScoutNavigationDefaults.transparentColor,
        content = content
    )
}

@Immutable
data class NavigationItem(
    val key: NavKey,
    val label: String,
    @DrawableRes val icon: Int,
    val navigationFunction: (navigator: StackNavigator) -> Unit,
)

@Composable
fun RowScope.ScoutNavigationBarItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable () -> Unit,
) {
    NavigationBarItem(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = { Text(text = label, style = ScoutNavigationDefaults.itemLabelStyle()) },
        colors = ScoutNavigationDefaults.itemColors()
    )
}





/**
 * Originally implemented to create a NavigationBar with a FAB with '+' icon in the middle of the bar
 *
 * Currently unused.
 *
 * Usage:
 * ```
 *     ScoutFabNavigationBar(
 *         onFabPress = { TODO() },
 *         isItemSelected = { key -> key == mainNavigator.current },
 *         onItemPress = { key -> mainNavigator.push(key) },
 *         fabIcon = painterResource(R.drawable.add_24px),
 *         items = listOf(
 *             NavigationItem(key = HomeNavKey, label = "Home"),
 *             NavigationItem(key = HistoryNavKey, label = "History"),
 *         )
 *     )
 *  ```
 */
@Composable
fun ScoutFabNavigationBar(
    modifier: Modifier = Modifier,
    barHeight: Dp = 72.dp,
    fabSize: Dp = 56.dp,
    fabIcon: Painter,
    onFabPress: () -> Unit,
    isItemSelected: (NavKey) -> Boolean,
    onItemPress: (NavKey) -> Unit,
    items: List<NavigationItem>
) {
    require(items.size % 2 == 0) { "Navigation items must be an even number" }

    val half = items.size / 2

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + (fabSize / 2)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.take(half).fastForEach { item ->
                ScoutNavigationBarItem(
                    selected = isItemSelected(item.key),
                    onClick = { onItemPress(item.key) },
                    icon = {},
                    label = item.label
                )
            }
            Spacer(Modifier.size(fabSize))
            items.takeLast(half).fastForEach { item ->
                ScoutNavigationBarItem(
                    selected = isItemSelected(item.key),
                    onClick = { onItemPress(item.key) },
                    icon = {},
                    label = item.label
                )
            }
        }

        ScoutFAB(
            modifier = Modifier.align(Alignment.TopCenter),
            fabSize = fabSize,
            onClick = onFabPress,
            fabIcon = fabIcon,
        )
    }
}
