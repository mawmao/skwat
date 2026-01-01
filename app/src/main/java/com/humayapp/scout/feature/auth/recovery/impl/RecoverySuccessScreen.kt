package com.humayapp.scout.feature.auth.recovery.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.auth.login.api.navigation.navigateToLogin

// 3/3 of password recovery flow

@Composable
fun RecoverySuccessScreen(modifier: Modifier = Modifier) {

    val authNavigator = LocalStackNavigator.current

    Screen {
        Text("Password Recovery 3 of 3 - Success")
        ScoutButton(text = "Finish") { authNavigator.navigateToLogin(popAll = true) }
    }
}