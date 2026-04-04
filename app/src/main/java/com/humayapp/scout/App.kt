package com.humayapp.scout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.humayapp.scout.core.data.settings.SettingsRepository
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.StackNavigator
import com.humayapp.scout.core.sandbox
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.core.ui.theme.ScoutTypography
import com.humayapp.scout.feature.auth.navigation.authSection
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepository
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import com.humayapp.scout.feature.form.impl.navigation.formSection
import com.humayapp.scout.feature.form.impl.ui.screens.scan.FormScanScreen
import com.humayapp.scout.feature.main.navigation.mainSection
import com.humayapp.scout.navigation.OverlayType
import com.humayapp.scout.navigation.RootNavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

val LocalScoutAppState = staticCompositionLocalOf<ScoutAppState> {
    error("No ScoutAppState provided")
}

@Composable
fun rememberScoutAppState(
    rootNavigator: StackNavigator<NavKey>,
    snackbarHostState: SnackbarHostState,
    settingsRepository: SettingsRepository,
    formRepository: FormRepository,
    coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    collectionRepository: CollectionRepository
): ScoutAppState {
    return remember(rootNavigator, snackbarHostState, settingsRepository, formRepository, coroutineScope, networkMonitor, collectionRepository) {
        ScoutAppState(
            rootNavigator = rootNavigator,
            snackbarHostState = snackbarHostState,
            settingsRepository = settingsRepository,
            formRepository = formRepository,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            collectionRepository = collectionRepository
        )
    }
}

@Stable
class ScoutAppState(
    val formRepository: FormRepository,
    val rootNavigator: StackNavigator<NavKey>,
    val snackbarHostState: SnackbarHostState,
    val settingsRepository: SettingsRepository,
    val coroutineScope: CoroutineScope,
    val networkMonitor: NetworkMonitor,
    val collectionRepository: CollectionRepository
) {

    private val _snackbarMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        replay = 1
    )

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val snackbarMessages: SharedFlow<String> = _snackbarMessages
    fun showSnackbar(message: String) = _snackbarMessages.tryEmit(message)
}


@Composable
fun ScoutApp(state: ScoutAppState) {

    LaunchedEffect(Unit) {
        merge(state.formRepository.syncEvents, state.snackbarMessages).collect { message ->
            state.snackbarHostState.showSnackbar(message = message )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = state.rootNavigator.asBackStack(),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                authSection(metadata = NavTransition.anchoredTop())
                mainSection(metadata = NavTransition.anchoredBottom())
                formSection(metadata = NavTransition.anchoredRight())

                entry<RootNavKey.Detail>(metadata = NavTransition.anchoredRight()) { it.content() }
                entry<RootNavKey.Overlay>(metadata = NavTransition.anchoredRight()) {
                    when (val overlay = it.overlayType) {
                        is OverlayType.Scan -> {
                            val formType = remember(overlay.formTypeName) {
                                FormType.valueOf(overlay.formTypeName)
                            }
                            FormScanScreen(formType = formType)
                        }
                    }
                }

                sandbox()
            }
        )
        SnackbarHost(
            hostState = state.snackbarHostState,
            snackbar = { data -> ReconSnackbar(data) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .systemBarsPadding(),
        )
    }
}

@Composable
private fun ReconSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        shape = RoundedCornerShape(32.dp),
        modifier = modifier
            .widthIn(max = 220.dp)
            .heightIn(max = 40.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .wrapContentWidth(Alignment.CenterHorizontally),
        action = {
            data.visuals.actionLabel?.let { label ->
                TextButton(onClick = { data.performAction() }) {
                    Text(label, color = Color.Yellow)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8F),
        contentColor = MaterialTheme.colorScheme.background,
    ) {
        Text(
            text = data.visuals.message,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = ScoutTypography.bodySmall,
        )
    }
}
