package com.humayapp.scout.feature.form.impl.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.navigateToFormConfirm
import com.humayapp.scout.feature.form.impl.LocalFormState

@Composable
fun FormScanScreen() {

    val formsNavigator = LocalStackNavigator.current
    val state = LocalFormState.current

    Screen {
        Text("Form Scan Screen - ${state.formType.id}")
        ScoutButton(text = "Simulate Scan") {
            state.setMfid("0601001")
            formsNavigator.navigateToFormConfirm()
        }
    }

}
