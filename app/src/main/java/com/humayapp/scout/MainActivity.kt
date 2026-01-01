package com.humayapp.scout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.navigation.RootNavKey
import dagger.hilt.android.AndroidEntryPoint

// should handle session

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            )
        )
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val rootNavigator = rememberStackNavigator(id = "root", initialKey = RootNavKey.Main);

            ScoutTheme {
                ScoutApp(
                    state = rememberScoutAppState(
                        rootNavigator = rootNavigator,
                        snackbarHostState = snackbarHostState
                    )
                );
            }
        }
    }
}

