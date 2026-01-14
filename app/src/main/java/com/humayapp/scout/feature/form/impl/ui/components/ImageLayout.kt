package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.humayapp.scout.core.ui.theme.ScoutTheme

//@Composable
//fun FormImagesLayout(
//    imageFields: List<WizardField>,
//    modifier: Modifier = Modifier,
//    imageContent: @Composable (key: WizardField, aspectRatio: Float, modifier: Modifier) -> Unit
//) {
//    Column(modifier = modifier) {
//        Text(
//            "Images",
//            style = ScoutTheme.material.typography.bodyLarge,
//            fontWeight = FontWeight.Medium,
//            color = ScoutTheme.material.colorScheme.onSurface,
//        )
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
//        ) {
//            imageFields.take(4).forEach { imageContent(it, 1f, Modifier.weight(1f)) }
//        }
//
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//        imageFields.getOrNull(4)?.let { imageContent(it, 16f / 9f, Modifier.fillMaxWidth()) }
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//    }
//}

//@Composable
//fun <T> FormImagesLayout(
//    imageFields: List<T>, // either wizard field or form image entity
//    modifier: Modifier = Modifier,
//    imageContent: @Composable (key: T, aspectRatio: Float, modifier: Modifier) -> Unit
//) {
//    Column(modifier = modifier) {
//        Text(
//            "Images",
//            style = ScoutTheme.material.typography.bodyLarge,
//            fontWeight = FontWeight.Medium,
//            color = ScoutTheme.material.colorScheme.onSurface,
//        )
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
//        ) {
//            imageFields.take(4).forEach { imageContent(it, 1f, Modifier.weight(1f)) }
//        }
//
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//
//        imageFields.getOrNull(4)?.let { imageContent(it, 16f / 9f, Modifier.fillMaxWidth()) }
//
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//    }
//}

//@Composable
//fun FormImagesLayout(
//    imageFields: List<WizardField>,
//    imageEntities: List<FormImageEntity>,
//    modifier: Modifier = Modifier,
//    imageContent: @Composable (field: WizardField, entity: FormImageEntity, aspectRatio: Float, modifier: Modifier) -> Unit
//) {
//    Column(modifier = modifier) {
//        Text(
//            "Images",
//            style = ScoutTheme.material.typography.bodyLarge,
//            fontWeight = FontWeight.Medium,
//            color = ScoutTheme.material.colorScheme.onSurface,
//        )
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
//        ) {
//            (0..3).forEach {
//                val field = imageFields.getOrNull(it)
//                val image = imageEntities.getOrNull(it)
//                if (field != null && image != null) {
//                    imageContent(field, image, 1f, Modifier.weight(1f))
//                }
//            }
//        }
//
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//
//        imageFields.getOrNull(4)?.let { field ->
//            val image = imageEntities.getOrNull(4)
//            Column(modifier = Modifier.fillMaxWidth()) {
//                if (image != null) {
//                    imageContent(field, image, 16f / 9f, Modifier.fillMaxWidth())
//                } else {
//                    FormReviewItem(label = field.label, value = "N/A")
//                }
//            }
//        }
//
//        Spacer(Modifier.height(ScoutTheme.spacing.medium))
//    }
//}


@Composable
fun <T> FormImagesLayout(
    items: List<T>,
    modifier: Modifier = Modifier,
    aspectRatios: List<Float> = listOf(1f, 1f, 1f, 1f, 1f),
    title: (@Composable () -> Unit)? = null,
    imageContent: @Composable (item: T, aspectRatio: Float, modifier: Modifier) -> Unit
) {
    Column(modifier = modifier) {
        title?.let {
            it()
            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
        ) {
            items.take(4).forEachIndexed { index, item ->
                imageContent(item, aspectRatios.getOrElse(index) { 1f }, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(ScoutTheme.spacing.medium))

        items.getOrNull(4)?.let { imageContent(it, aspectRatios.getOrElse(4) { 16f / 9f }, Modifier.fillMaxWidth()) }

        Spacer(Modifier.height(ScoutTheme.spacing.medium))
    }
}
