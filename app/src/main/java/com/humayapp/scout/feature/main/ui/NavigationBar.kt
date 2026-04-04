package com.humayapp.scout.feature.main.ui


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator
import com.humayapp.scout.core.ui.component.NavigationItem
import com.humayapp.scout.core.ui.component.ScoutDefaultNavigationBar
import com.humayapp.scout.core.ui.component.ScoutNavigationBarItem

@Composable
fun MainSectionNavigationBar(
    navigator: StackNavigator<NavKey>,
    items: List<NavigationItem>
) {
    ScoutDefaultNavigationBar(
        content = {
            items.fastForEach { item ->
                ScoutNavigationBarItem(
                    selected = item.key == navigator.current,
                    onClick = { item.navigationFunction(navigator) },
                    icon = {
                        Icon(
                            modifier = Modifier.padding(vertical = 2.dp),
                            painter = painterResource(item.icon),
                            contentDescription = item.label,
                        )
                    },
                    label = item.label,
                    badgeCount = item.badgeCount
                )
            }
        }
    )
}
