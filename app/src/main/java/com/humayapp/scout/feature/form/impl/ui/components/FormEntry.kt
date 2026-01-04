package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.navigation.WizardNavKey

fun EntryProviderScope<WizardNavKey>.formEntry(
    key: WizardNavKey,
    metadata: Map<String, Any>,
    content: @Composable ColumnScope.(WizardNavKey) -> Unit
) {
    entry(key, metadata = metadata) { key ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScoutTheme.margin),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier.padding(top = ScoutTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.extraSmall))
            {
                Text(text = key.title, style = ScoutTheme.material.typography.headlineMedium)
                Text(
                    text = key.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(ScoutTheme.spacing.medium))
            content(key)
        }
    }
}
