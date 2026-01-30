package com.humayapp.scout.feature.form.impl.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.navigateToFormWizard
import com.humayapp.scout.feature.form.impl.LocalFormState

@Composable
fun FormConfirmScreen() {
    val formsNavigator = LocalStackNavigator.current
    val state = LocalFormState.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScoutTheme.margin)
    ) {
        Box(
            modifier = Modifier.weight(1F).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Confirm Screen - ${state.formType.id} | ${state.mfid}")
        }

        ScoutButton(
            text = "Confirm",
            modifier = Modifier.fillMaxWidth()
        ) {
            formsNavigator.navigateToFormWizard()
        }
    }
}
