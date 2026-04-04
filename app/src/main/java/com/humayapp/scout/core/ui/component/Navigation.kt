package com.humayapp.scout.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = modifier.padding(horizontal = 16.dp),
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
    val navigationFunction: (navigator: StackNavigator<NavKey>) -> Unit,
    val badgeCount: Int = 0
)

@Composable
fun RowScope.ScoutNavigationBarItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable () -> Unit,
    badgeCount: Int = 0,
) {
    NavigationBarItem(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        icon = {
            Box {
                icon()
                if (badgeCount > 0) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    ) {
                        Text(if (badgeCount > 99) "99+" else badgeCount.toString())
                    }
                }
            }
        },
        label = { Text(text = label, style = ScoutNavigationDefaults.itemLabelStyle()) },
        colors = ScoutNavigationDefaults.itemColors()
    )
}
