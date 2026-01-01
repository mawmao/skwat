package com.humayapp.scout.feature.form.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.navigation.navigateToMain

@Composable
fun FormReviewScreen(state: FormState) {
    val rootNavigator = LocalRootStackNavigator.current
    val formsNavigator = LocalStackNavigator.current

    Screen {
        Text("Form Review Screen")
        ScoutOutlinedButton(text = "Back") {
            // ask the user if they want to edit their form first
            formsNavigator.pop()
        }
        ScoutButton(text = "Finish") {
            rootNavigator.navigateToMain()

            // could also show a snackbar after
        }
    }
}

