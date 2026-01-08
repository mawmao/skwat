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
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.SANDBOX_ENABLE
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.util.onAvailability
import com.humayapp.scout.navigation.RootNavKey
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.status.SessionStatus
import jakarta.inject.Inject

// should handle session

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

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

            LaunchedEffect(sessionStatus) {
                if (sessionStatus != null) keepSplash = false
            }

            sessionStatus.onAvailability {

                val snackbarHostState = remember { SnackbarHostState() }
                val rootNavigator = rememberStackNavigator<NavKey>(
                    id = "root",

                    // development only
                    initialKey = if (SANDBOX_ENABLE) RootNavKey.Sandbox else {
                        when (sessionStatus) {
                            is SessionStatus.Authenticated -> RootNavKey.Main
                            else -> RootNavKey.Auth
                        }
                    }
                );

                ScoutTheme {
                    CompositionLocalProvider(LocalRootStackNavigator provides rootNavigator) {
                        ScoutApp(
                            state = rememberScoutAppState(
                                rootNavigator = rootNavigator,
                                snackbarHostState = snackbarHostState
                            )
                        )
                    }
                }
            }
        }
    }
}


