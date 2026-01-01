package com.humayapp.scout.feature.form.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.form.api.navigation.navigateToFormWizard

@Composable
fun FormConfirmScreen(state: FormState) {
    val formsNavigator = LocalStackNavigator.current

    Screen {
        Text("Form Confirm Screen")
        ScoutOutlinedButton(text = "Back") {
            formsNavigator.pop()
        }
        ScoutButton(text = "Next") { formsNavigator.navigateToFormWizard() }
    }
}
