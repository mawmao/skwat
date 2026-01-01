package com.humayapp.scout.feature.form.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.navigateToFormConfirm
import com.humayapp.scout.navigation.navigateToMain

@Composable
fun FormScanScreen(state: FormState) {

    val rootNavigator = LocalRootStackNavigator.current
    val formsNavigator = LocalStackNavigator.current

    Screen {
        Text("Form Scan Screen - ${state.formType.id}")
        ScoutOutlinedButton(text = "Back") {
            // used root navigator since forms section is a root type
            rootNavigator.navigateToMain()
        }
        ScoutButton(text = "Next") { formsNavigator.navigateToFormConfirm() }
    }
}
