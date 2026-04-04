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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import com.humayapp.scout.core.ui.component.ImageBox
import com.humayapp.scout.core.ui.component.ScoutLabel
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.FormState

@Composable
fun FormDetailsContent(state: FormState) {

    val imageFields = state.allFields
        .filter { it.key.startsWith("img_") }
        .filter { state.getFieldData(it.key).isNotBlank() }
        .sortedBy { it.key }

    val monitoringKeys = listOf(
        "date_monitored",
        "crop_stage",
        "soil_moisture_status",
        "avg_plant_height"
    )

    val monitoringFields = state.allFields.filter { it.key in monitoringKeys }
    val otherFields = state.allFields.filter {
        it.key !in monitoringKeys && !it.key.startsWith("img_")
    }

    otherFields.fastForEach { field ->
        val rawValue = state.getFieldData(field.key)
        FormReviewItem(label = field.label, value = rawValue.ifBlank { "N/A" })
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
    }

    if (monitoringFields.isNotEmpty()) {
        Column {
            Text(
                "Monitoring Visit",
                style = ScoutTheme.material.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ScoutTheme.material.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            monitoringFields.fastForEach { field ->
                val rawValue = state.getFieldData(field.key)
                FormReviewItem(label = field.label, value = rawValue.ifBlank { "N/A" })
                Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
            }
            Spacer(Modifier.height(16.dp))
        }
    }


    if (imageFields.isNotEmpty()) {
        FormImagesLayout(
            items = imageFields,
            title = {
                Text(
                    "Images",
                    style = ScoutTheme.material.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ScoutTheme.material.colorScheme.onSurface,
                )
            }
        ) { field, aspectRatio, modifier ->
            val path = state.getFieldData(field.key)
            Column(modifier = modifier) {
                ScoutLabel(label = field.label, enableHorizontalPadding = false)
                ImageBox(
                    uri = path.takeIf { field.key.isNotBlank() }?.toUri(),
                    modifier = Modifier,
                    aspectRatio = aspectRatio
                )
            }
        }
    }

}


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

