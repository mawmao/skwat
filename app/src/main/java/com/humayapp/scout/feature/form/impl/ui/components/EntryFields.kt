package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.model.WizardField

@Composable
fun <ImageType> FormFieldData(
    fields: List<WizardField>,
    getAnswer: (String) -> String,
    images: List<ImageType> = emptyList(),
    imageContent: @Composable (field: WizardField, image: ImageType?, aspectRatio: Float?, modifier: Modifier) -> Unit
) {
    val imageFields = fields.filter { it.key.startsWith("img_") }.sortedBy { it.key }
    val otherFields = fields.filter { !it.key.startsWith("img_") }

    if (imageFields.isNotEmpty()) {
        val imageItems = imageFields.zip(images) { field, img -> field to img }
        FormImagesLayout(
            items = imageItems,
            title = {
                Text(
                    "Images",
                    style = ScoutTheme.material.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ScoutTheme.material.colorScheme.onSurface,
                )
            }
        ) { (field, img), aspect, modifier ->
            imageContent(field, img, aspect, modifier)
        }
    }

    otherFields.fastForEach { field ->
        val value = getAnswer(field.key)
        FormReviewItem(label = field.label, value = value.ifBlank { "N/A" })
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
    }
}
