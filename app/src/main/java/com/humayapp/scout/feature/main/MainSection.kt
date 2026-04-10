package com.humayapp.scout.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.humayapp.scout.core.ui.component.ScoutAlertDialog
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.util.ScoutErrorEvent
import com.humayapp.scout.core.ui.util.ScoutUiEvents
import com.humayapp.scout.feature.auth.data.ScoutAuthState
import com.humayapp.scout.feature.main.approved.api.navigation.ApprovedNavKey
import com.humayapp.scout.feature.main.approved.api.navigation.navigateToApproved
import com.humayapp.scout.feature.main.approved.impl.navigation.approvedEntryProvider
import com.humayapp.scout.feature.main.collected.api.navigation.CollectedNavKey
import com.humayapp.scout.feature.main.collected.api.navigation.navigateToCollected
import com.humayapp.scout.feature.main.collected.impl.navigation.collectedEntryProvider
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
import com.humayapp.scout.navigation.navigateToNotifications


@Composable
fun MainSection(vm: MainSectionViewModel = hiltViewModel()) {

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val uiError by vm.uiError.collectAsStateWithLifecycle()

    val rootNavigator = LocalRootStackNavigator.current
    val mainNavigator = rememberStackNavigator<NavKey>("main", initialKey = PendingNavKey)

    val currentUser by vm.currentUser.collectAsStateWithLifecycle()
    val isOnline by vm.isOnline.collectAsStateWithLifecycle()

    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.isRead }

    val authState by vm.authState.collectAsStateWithLifecycle()

    var showSessionExpiredDialog by remember { mutableStateOf(false) }

    val pendingCount = uiState.tasks.count { it.status == "pending" }
    val collectedCount = uiState.tasks.count { it.status == "completed" && (it.verificationStatus == "pending" || it.verificationStatus == null) }
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

    ScoutErrorEvent(errorMessage = uiError, onDismiss = { vm.onAction(MainSectionAction.ClearUiError) })
    ScoutUiEvents(vm.uiEvent) { event ->
        when (event) {
            MainSectionEvent.LogoutSuccess -> {
                vm.onAction(MainSectionAction.ToggleProfile(false))
                rootNavigator.navigateToAuth()
            }

            MainSectionEvent.SessionExpired -> {
                showSessionExpiredDialog = true
            }
        }
    }

    UserProfileDialog(
        isVisible = uiState.isProfileShown,
        user = currentUser,
        onDismissRequest = { vm.onAction(MainSectionAction.ToggleProfile(false)) },
        onSignOut = { vm.onAction(MainSectionAction.LogoutRequest) }
    )

    ScoutAlertDialog(
        isVisible = showSessionExpiredDialog,
        title = "Session Expired",
        message = "Your session has timed out for security. Please log in again to continue syncing your data.",
        onDismissRequest = { /* Prevent dismissal by clicking outside for critical auth errors */ },
        confirmButtonText = "Log In",
        onConfirm = {
            showSessionExpiredDialog = false
            vm.onAction(MainSectionAction.ToggleProfile(false))
            rootNavigator.navigateToAuth()
        }
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
                    onNotificationsClick = rootNavigator::navigateToNotifications,
                    unreadCount = unreadCount,
                    canRefresh = authState is ScoutAuthState.AuthenticatedOnline
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


