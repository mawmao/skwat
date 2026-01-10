package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.FormState


@Composable
fun FormReviewItem(
    label: String,
    value: String,
    isRow: Boolean = false,
) {
    if (isRow) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label:",
                style = ScoutTheme.material.typography.bodyMedium,
                color = ScoutTheme.material.colorScheme.onSurfaceVariant,
            )
            Text(text = value)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$label:",
                style = ScoutTheme.material.typography.bodyMedium,
                color = ScoutTheme.material.colorScheme.onSurfaceVariant,
            )
            Text(text = value)
        }
    }
}

@Composable
fun FormReviewDefaultContent(
    state: FormState
) {
    state.entries.fastForEach { entry ->
        entry.fields.fastForEach { field ->
            val value = state.getAnswer(field.key).takeIf { it.isNotBlank() } ?: "N/A"
            FormReviewItem(field.label, value)
            Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        }
    }
}
