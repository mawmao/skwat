package com.humayapp.scout.feature.form.impl.ui.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.component.ScoutLoadingButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.ScoutUiEvents
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.navigation.navigateToMain


@Composable
fun FormReviewScreen(vm: FormReviewViewModel) {
    val rootNavigator = LocalRootStackNavigator.current
    val state = LocalFormState.current
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    ScoutUiEvents(vm.uiEvent) { event ->
        when (event) {
            is FormReviewEvent.SubmitSuccessAndNavigate -> {
                // Navigate to main screen (or directly to details)
                rootNavigator.navigateToMain()
                // Optionally, you could navigate directly to the details screen:
                // rootNavigator.navigateToFormDetails(collectionTaskId, event.activityId)
            }
            FormReviewEvent.SubmitSuccess -> {
                rootNavigator.navigateToMain()
            }
        }
    }

    if (uiState.isLoading) {
        Text(text = "Submitting form...")
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
        ScoutLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Finish",
            isLoading = uiState.isLoading,
            onClick = { vm.onAction(FormReviewAction.FormSubmit(state.fieldData)) }
        )
    }
}
