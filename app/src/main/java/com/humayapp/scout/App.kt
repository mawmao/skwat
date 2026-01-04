package com.humayapp.scout

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.StackNavigator
import com.humayapp.scout.feature.auth.navigation.authSection
import com.humayapp.scout.feature.form.impl.navigation.formSection
import com.humayapp.scout.feature.main.navigation.mainSection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun rememberScoutAppState(
    rootNavigator: StackNavigator<NavKey>,
    snackbarHostState: SnackbarHostState,
): ScoutAppState {
    return remember(rootNavigator, snackbarHostState) {
        ScoutAppState(
            rootNavigator = rootNavigator,
            snackbarHostState = snackbarHostState
        )
    }
}

@Stable
class ScoutAppState(
    val rootNavigator: StackNavigator<NavKey>,
    val snackbarHostState: SnackbarHostState,

    // TODO: val networkMonitor: NetworkMonitor
) {
    // TODO: val isOffline = networkMonitor.isOffline;

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 1, replay = 0)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages
    fun showSnackbar(message: String) = _snackbarMessages.tryEmit(message)
}

@Composable
fun ScoutApp(state: ScoutAppState) {
    NavDisplay(
        backStack = state.rootNavigator.asBackStack(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            authSection(metadata = NavTransition.anchoredTop())
            mainSection(metadata = NavTransition.anchoredBottom())
            formSection(metadata = NavTransition.anchoredRight())
        }
    )
}