package com.humayapp.scout.feature.form.impl.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.navigateToFormWizard
import com.humayapp.scout.feature.form.impl.LocalFormState

@Composable
fun FormConfirmScreen() {

    val formsNavigator = LocalStackNavigator.current
    val state = LocalFormState.current

    Screen {
        Text("Confirm Screen - ${state.formType.id} | ${state.mfid}")
        ScoutButton(text = "Confirm") { formsNavigator.navigateToFormWizard() }
    }
}
