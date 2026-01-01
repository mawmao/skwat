package com.humayapp.scout.feature.form.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.form.api.navigation.navigateToFormReview
import com.humayapp.scout.navigation.navigateToMain

@Composable
fun FormWizardScreen(state: FormState) {
    val rootNavigator = LocalRootStackNavigator.current
    val formsNavigator = LocalStackNavigator.current

    Screen {
        Text("Form Wizard Screen")
        ScoutOutlinedButton(text = "Back") {
            // it does not make sense to go back to confirmation screen
            // this should ask the user if they want to cancel the form collection
            // then go back to the home screen if yes
            rootNavigator.navigateToMain()
        }
        ScoutButton(text = "Next") {
            formsNavigator.navigateToFormReview()
        }
    }
}

