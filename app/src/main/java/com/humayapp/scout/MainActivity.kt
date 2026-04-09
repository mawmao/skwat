package com.humayapp.scout

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.data.settings.SettingsRepository
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.auth.data.NewAuthRepository
import com.humayapp.scout.feature.auth.data.ScoutAuthState
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepository
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import com.humayapp.scout.navigation.RootNavKey
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var newAuthRepository: NewAuthRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var formRepository: FormRepository

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var collectionRepository: CollectionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("Scout: MainActivity", "[Core] Launching app.")

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

        lifecycleScope.launch {
            newAuthRepository.restoreSession()
        }

        setContent {

            val authState by newAuthRepository.authState.collectAsState(initial = ScoutAuthState.Initializing)
            var targetKey by remember { mutableStateOf<RootNavKey?>(null) }

            targetKey = when (authState) {
                is ScoutAuthState.Unauthenticated -> RootNavKey.Auth
                is ScoutAuthState.AuthenticatedOnline, is ScoutAuthState.AuthenticatedOffline -> RootNavKey.Main
                is ScoutAuthState.SessionExpired -> RootNavKey.Auth
                is ScoutAuthState.Initializing -> null
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


