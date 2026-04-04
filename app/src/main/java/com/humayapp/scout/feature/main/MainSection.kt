package com.humayapp.scout.feature.main

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.humayapp.scout.feature.main.approved.api.navigation.ApprovedNavKey
import com.humayapp.scout.feature.main.approved.api.navigation.navigateToApproved
import com.humayapp.scout.feature.main.approved.impl.navigation.approvedEntryProvider
import com.humayapp.scout.feature.main.collected.api.navigation.CollectedNavKey
import com.humayapp.scout.feature.main.collected.api.navigation.navigateToCollected
import com.humayapp.scout.feature.main.collected.impl.navigation.collectedEntryProvider
import com.humayapp.scout.feature.main.notification.impl.NotificationsScreen
import com.humayapp.scout.feature.main.notification.impl.NotificationsViewModel
import com.humayapp.scout.feature.main.pending.api.navigation.PendingNavKey
import com.humayapp.scout.feature.main.pending.api.navigation.navigateToPending
import com.humayapp.scout.feature.main.pending.impl.navigation.pendingEntryProvider
import com.humayapp.scout.feature.main.rejected.api.navigation.RejectedNavKey
import com.humayapp.scout.feature.main.rejected.api.navigation.navigateToRejected
import com.humayapp.scout.feature.main.rejected.impl.navigation.rejectedEntryProvider
import com.humayapp.scout.feature.main.ui.MainSectionNavigationBar
import com.humayapp.scout.feature.main.ui.MainSectionTopAppBar
import com.humayapp.scout.feature.main.ui.UserProfileDialog
import com.humayapp.scout.navigation.navigateToAuth
import com.humayapp.scout.navigation.navigateToDetail


@Composable
fun MainSection(
    vm: MainSectionViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel()
) {

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val uiError by vm.uiError.collectAsStateWithLifecycle()

    val rootNavigator = LocalRootStackNavigator.current
    val mainNavigator = rememberStackNavigator<NavKey>("main", initialKey = PendingNavKey)

    val currentUser by vm.currentUser.collectAsStateWithLifecycle()
    val isOnline by vm.isOnline.collectAsStateWithLifecycle()

    val notifications by notificationsViewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.isRead }

    val pendingCount = uiState.tasks.count { it.status == "pending" }
    val collectedCount = uiState.tasks.count { it.status == "completed" && it.verificationStatus == "pending" }
    val approvedCount = uiState.tasks.count { it.status == "completed" && it.verificationStatus == "approved" }
    val rejectedCount = uiState.tasks.count { it.status == "completed" && it.verificationStatus == "rejected" }

    val navItems = listOf(
        NavigationItem(
            key = PendingNavKey,
            label = "Pending",
            icon = ScoutIcons.ListAlt,
            navigationFunction = ::navigateToPending,
            badgeCount = pendingCount
        ),
        NavigationItem(
            key = CollectedNavKey,
            label = "Collected",
            icon = ScoutIcons.ListAltCheck,
            navigationFunction = ::navigateToCollected,
            badgeCount = collectedCount
        ),
        NavigationItem(
            key = ApprovedNavKey,
            label = "Approved",
            icon = ScoutIcons.Approved,
            navigationFunction = ::navigateToApproved,
            badgeCount = approvedCount
        ),
        NavigationItem(
            key = RejectedNavKey,
            label = "Rejected",
            icon = ScoutIcons.Rejected,
            navigationFunction = ::navigateToRejected,
            badgeCount = rejectedCount
        ),
    )

    LaunchedEffect(currentUser, isOnline) {
        Log.i("Scout", "[INFO] User: ${currentUser?.email ?: "null"}, Online: $isOnline")
    }

    ScoutErrorEvent(errorMessage = uiError, onDismiss = { vm.onAction(MainSectionAction.ClearUiError) })
    ScoutUiEvents(vm.uiEvent) { event ->
        when (event) {
            MainSectionEvent.LogoutSuccess -> {
                vm.onAction(MainSectionAction.ToggleProfile(false))
                rootNavigator.navigateToAuth()
            }
        }
    }

    UserProfileDialog(
        isVisible = uiState.isProfileShown,
        user = currentUser,
        onDismissRequest = { vm.onAction(MainSectionAction.ToggleProfile(false)) },
        onSignOut = { vm.onAction(MainSectionAction.LogoutRequest) }
    )

    CompositionLocalProvider(LocalStackNavigator provides mainNavigator) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                MainSectionTopAppBar(
                    currentKey = mainNavigator.current,
                    onProfileClick = { vm.onAction(MainSectionAction.ToggleProfile(true)) },
                    onRefreshClick = vm::refreshTasks,
                    isRefreshing = uiState.isLoading || uiState.isRefreshing,
                    onNotificationsClick = {
                        rootNavigator.navigateToDetail {
                            NotificationsScreen(viewModel = notificationsViewModel)
                        }
                    },
                    unreadCount = unreadCount
                )
            },
            bottomBar = {
                MainSectionNavigationBar(navigator = mainNavigator, items = navItems)
            }
        ) { innerPadding ->
            NavDisplay(
                modifier = Modifier.padding(innerPadding),
                backStack = mainNavigator.asBackStack(),
                onBack = mainNavigator::pop,
                entryProvider = entryProvider {
                    pendingEntryProvider(metadata = NavTransition.fade(), vm = vm)
                    collectedEntryProvider(metadata = NavTransition.fade(), vm = vm)
                    approvedEntryProvider(metadata = NavTransition.fade(), vm = vm)
                    rejectedEntryProvider(metadata = NavTransition.fade(), vm = vm)
                },
            )
        }
    }
}


