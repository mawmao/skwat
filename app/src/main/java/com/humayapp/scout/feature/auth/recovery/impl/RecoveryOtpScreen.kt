package com.humayapp.scout.feature.auth.recovery.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.auth.recovery.api.navigation.navigateToRecoveryReset

// 0/3 of password recovery flow

@Composable
fun RecoveryOtpScreen() {

    val authNavigator = LocalStackNavigator.current

    Screen {
        Text("Password Recovery 1 of 3 - Otp")
        ScoutOutlinedButton(text = "Back") { authNavigator.pop() }
        ScoutButton(text = "Next") { authNavigator.navigateToRecoveryReset() }
    }
}