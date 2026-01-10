package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.model.WizardEntry


@Composable
fun DefaultWizardEntry(key: WizardEntry) {

    val state = LocalFormState.current

    WizardEntry(key) { page ->
        page.fields.fastForEach { field ->
            WizardField(
                field = field,
                value = { state.getAnswer(field.key) },
                onValueChange = { state.setAnswer(field.key, it) },
                modifier = Modifier.fillMaxWidth(),
                imeAction = field.imeAction
            )
            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }
    }
}

@Composable
fun WizardEntry(
    key: WizardEntry,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.(WizardEntry) -> Unit
) {

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScoutTheme.margin)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        verticalArrangement = Arrangement.Top,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(top = ScoutTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.extraSmall)
            ) {
                Text(text = key.title, style = ScoutTheme.material.typography.headlineMedium)
                Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
                Text(
                    text = key.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            actions?.let {
                it()
            }
        }
        Spacer(Modifier.height(ScoutTheme.spacing.medium))
        content(key)
    }
}
