package com.humayapp.scout.feature.main.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.ui.component.NavigationItem
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.feature.main.history.api.navigation.HistoryNavKey
import com.humayapp.scout.feature.main.history.api.navigation.navigateToHistory
import com.humayapp.scout.feature.main.history.impl.navigation.historyEntryProvider
import com.humayapp.scout.feature.main.home.api.navigation.HomeNavKey
import com.humayapp.scout.feature.main.home.api.navigation.navigateToHome
import com.humayapp.scout.feature.main.home.impl.navigation.homeEntryProvider
import com.humayapp.scout.feature.main.ui.MainSectionNavigationBar
import com.humayapp.scout.feature.main.ui.MainSectionTopAppBar
import com.humayapp.scout.feature.main.ui.SettingsDialog
import com.humayapp.scout.navigation.RootNavKey
import com.humayapp.scout.navigation.navigateToAuth

// the top level routes in the main section
val NavigationItems = listOf(
    NavigationItem(
        key = HomeNavKey,
        label = "Home",
        icon = ScoutIcons.Home,
        navigationFunction = ::navigateToHome
    ),
    NavigationItem(
        key = HistoryNavKey,
        label = "History",
        icon = ScoutIcons.History,
        navigationFunction = ::navigateToHistory
    ),
)

fun EntryProviderScope<NavKey>.mainSection(metadata: Map<String, Any>) {
    entry<RootNavKey.Main>(metadata = metadata) {

        val isSettingsShown = retain { mutableStateOf(false) }

        val rootNavigator = LocalRootStackNavigator.current
        val mainNavigator = rememberStackNavigator("main", initialKey = HomeNavKey)

        CompositionLocalProvider(LocalStackNavigator provides mainNavigator) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    MainSectionTopAppBar(
                        currentKey = mainNavigator.current,
                        onSettingsClick = { isSettingsShown.value = true },
                        onSyncClick = { TODO("sync not yet implemented") }
                    )
                },
                bottomBar = {
                    MainSectionNavigationBar(navigator = mainNavigator, items = NavigationItems)
                }
            ) { innerPadding ->
                NavDisplay(
                    modifier = Modifier.padding(innerPadding),
                    backStack = mainNavigator.asBackStack(),
                    onBack = mainNavigator::pop,
                    entryProvider = entryProvider {
                        homeEntryProvider(metadata = NavTransition.fade())
                        historyEntryProvider(metadata = NavTransition.fade())
                    },
                )
            }

            SettingsDialog(
                isVisible = isSettingsShown.value,
                onDismissRequest = { isSettingsShown.value = false },
                onSignOut = {
                    // find ways to make this cleaner and more graceful
                    isSettingsShown.value = false
                    rootNavigator.navigateToAuth()
                }
            )
        }
    }
}