package com.humayapp.scout.feature.form.impl.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.Screen
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.navigation.navigateToMain

@Composable
fun FormReviewScreen() {

    val rootNavigator = LocalRootStackNavigator.current
    val state = LocalFormState.current

    Screen {
        Text("Form Review Screen - ${state.formType.id}")
        ScoutButton(text = "Finish") {
            rootNavigator.navigateToMain()

            // could also show a snackbar after
        }
    }
}

