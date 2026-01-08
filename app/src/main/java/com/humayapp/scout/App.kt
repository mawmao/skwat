package com.humayapp.scout

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.compose.AsyncImage
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.StackNavigator
import com.humayapp.scout.core.sandbox
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.core.ui.theme.InputFieldTokens
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.auth.navigation.authSection
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.navigation.formSection
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.main.navigation.mainSection
import com.humayapp.scout.navigation.RootNavKey
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

            // development only
            sandbox()
        }
    )
}
