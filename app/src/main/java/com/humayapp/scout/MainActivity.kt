package com.humayapp.scout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.SANDBOX_ENABLE
import com.humayapp.scout.core.data.settings.SettingsRepository
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.sync.FormSyncWorker
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepository
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import com.humayapp.scout.navigation.RootNavKey
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.status.SessionStatus
import jakarta.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(DelicateCoroutinesApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var formRepository: FormRepository

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var collectionRepository: CollectionRepository

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplash = true

        splashScreen.setKeepOnScreenCondition { keepSplash }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            )
        )

        setContent {

            val sessionStatus by authRepository.sessionStatus.collectAsState(initial = null)
            val isOnline by networkMonitor.isOnline.collectAsState(initial = false)
            var targetKey by remember { mutableStateOf<RootNavKey?>(null) }

            LaunchedEffect(Unit) {
                val requiresReauth = authRepository.getRequiresReauth()
                if (requiresReauth) {
                    authRepository.clearSessionOnly()
                    targetKey = RootNavKey.Auth
                    return@LaunchedEffect
                }

                val session = sessionStatus ?: return@LaunchedEffect
                val networkReady = withTimeoutOrNull(3000L) {
                    networkMonitor.isOnline.drop(1).first { it }
                    true
                } ?: isOnline

                targetKey = when {
                    SANDBOX_ENABLE -> RootNavKey.Sandbox
                    session is SessionStatus.Authenticated && networkReady -> RootNavKey.Main
                    else -> RootNavKey.Auth
                }
            }

            LaunchedEffect(targetKey) {
                when {
                    targetKey is RootNavKey.Auth -> {
                        val currentSession = sessionStatus
                        if (currentSession is SessionStatus.Authenticated) {
                            authRepository.signOut()
                        }
                    }
                     targetKey is RootNavKey.Main -> {
                         // TaskPullWorker.start(context = this@MainActivity)
                         FormSyncWorker.startUpSyncWork()
                     }
                }
            }

            keepSplash = targetKey == null

            targetKey?.let { key ->
                val rootNavigator = rememberStackNavigator<NavKey>(
                    id = "root",
                    initialKey = key
                )

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val state = rememberScoutAppState(
                    rootNavigator = rootNavigator,
                    snackbarHostState = snackbarHostState,
                    settingsRepository = settingsRepository,
                    formRepository = formRepository,
                    coroutineScope = scope,
                    networkMonitor = networkMonitor,
                    collectionRepository = collectionRepository
                )

                ScoutTheme {
                    CompositionLocalProvider(
                        LocalRootStackNavigator provides rootNavigator,
                        LocalScoutAppState provides state
                    ) {
                        ScoutApp(state = state)
                    }
                }
            }
        }
    }
}


