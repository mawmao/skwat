package com.humayapp.scout.feature.form.impl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.navigation.navigateToMain

@Composable
fun FormReviewScreen() {

    val rootNavigator = LocalRootStackNavigator.current
    val state = LocalFormState.current

    val orderedFields = retain(state.entries) {
        state.entries.flatMap { it.fields }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScoutTheme.margin)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        orderedFields.fastForEach { field ->
            val value = state.answers[field.key] ?: return@fastForEach

            Text(text = "${field.label}: $value")
            Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        }

        Spacer(Modifier.height(ScoutTheme.spacing.medium))
        ScoutButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Finish",
            onClick = rootNavigator::navigateToMain
        )
    }
}

