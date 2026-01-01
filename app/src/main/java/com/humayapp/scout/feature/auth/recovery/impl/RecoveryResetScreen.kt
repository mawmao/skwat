package com.humayapp.scout.feature.auth.recovery.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.auth.recovery.api.navigation.navigateToRecoverySuccess

// 2/3 of password recovery flow

@Composable
fun RecoveryResetScreen(modifier: Modifier = Modifier) {

    val authNavigator = LocalStackNavigator.current

    Screen {
        Text("Password Recovery 2 of 3 - Reset")
        ScoutOutlinedButton(text = "Back") { authNavigator.pop() }
        ScoutButton(text = "Next") { authNavigator.navigateToRecoverySuccess() }
    }
}