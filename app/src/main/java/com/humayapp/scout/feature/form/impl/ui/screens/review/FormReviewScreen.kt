package com.humayapp.scout.feature.form.impl.ui.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.ScoutUiEvents
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.navigation.navigateToMain


@Composable
fun FormReviewScreen(vm: FormReviewViewModel) {

    val rootNavigator = LocalRootStackNavigator.current
    val state = LocalFormState.current

    ScoutUiEvents(vm.uiEvent) { event ->
        when (event) {
            FormReviewEvent.SubmitSuccess -> rootNavigator.navigateToMain()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScoutTheme.margin)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        state.formType.reviewContent(state)

        Spacer(Modifier.weight(1f))
        ScoutButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Finish",
            onClick = { vm.onAction(FormReviewAction.FormSubmit(state.answers)) }
        )
    }
}
