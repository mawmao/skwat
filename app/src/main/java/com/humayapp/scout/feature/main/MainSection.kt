package com.humayapp.scout.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.ui.component.NavigationItem
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.util.ScoutErrorEvent
import com.humayapp.scout.core.ui.util.ScoutUiEvents
import com.humayapp.scout.feature.history.api.navigation.HistoryNavKey
import com.humayapp.scout.feature.history.api.navigation.navigateToHistory
import com.humayapp.scout.feature.history.impl.navigation.historyEntryProvider
import com.humayapp.scout.feature.main.home.api.navigation.HomeNavKey
import com.humayapp.scout.feature.main.home.api.navigation.navigateToHome
import com.humayapp.scout.feature.main.home.impl.navigation.homeEntryProvider
import com.humayapp.scout.feature.main.ui.MainSectionNavigationBar
import com.humayapp.scout.feature.main.ui.MainSectionTopAppBar
import com.humayapp.scout.feature.main.ui.SettingsDialog
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


@Composable
fun MainSection(vm: MainSectionViewModel = hiltViewModel()) {

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val uiError by vm.uiError.collectAsStateWithLifecycle()

    val rootNavigator = LocalRootStackNavigator.current
    val mainNavigator = rememberStackNavigator<NavKey>("main", initialKey = HomeNavKey)

    ScoutErrorEvent(errorMessage = uiError, onDismiss = { vm.onAction(MainSectionAction.ClearUiError) })
    ScoutUiEvents(vm.uiEvent) { event ->
        when (event) {
            MainSectionEvent.LogoutSuccess -> {
                vm.onAction(MainSectionAction.ToggleSettings(false))
                rootNavigator.navigateToAuth()
            }
        }
    }

    SettingsDialog(
        isVisible = uiState.isSettingsShown,
        onDismissRequest = { vm.onAction(MainSectionAction.ToggleSettings(false)) },
        onSignOut = { vm.onAction(MainSectionAction.LogoutRequest) }
    )

    CompositionLocalProvider(LocalStackNavigator provides mainNavigator) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                MainSectionTopAppBar(
                    currentKey = mainNavigator.current,
                    onSettingsClick = { vm.onAction(MainSectionAction.ToggleSettings(true)) },
                    onSyncClick = {
                        // TODO: after sync is implemented
                    }
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
                    historyEntryProvider()
                },
            )
        }
    }
}

