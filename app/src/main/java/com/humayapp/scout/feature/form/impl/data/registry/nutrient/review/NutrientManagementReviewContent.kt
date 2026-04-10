package com.humayapp.scout.feature.form.impl.data.registry.nutrient.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import com.humayapp.scout.core.ui.component.ImageBox
import com.humayapp.scout.core.ui.component.ScoutLabel
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.AMOUNT_APPLIED_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.AMOUNT_UNIT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.APPLIED_AREA_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.BRAND_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.CROP_STAGE_ON_APPLICATION_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.FERTILIZER_TYPE_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.NITROGEN_CONTENT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.PHOSPHORUS_CONTENT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.POTASSIUM_CONTENT_KEY
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.ui.components.FormImagesLayout
import com.humayapp.scout.feature.form.impl.ui.components.FormReviewItem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun NutrientManagementReviewContent(state: FormState) {
    val monitoringKeys = listOf(
        "date_monitored",
        "crop_stage",
        "soil_moisture_status",
        "avg_plant_height"
    )

    val imageFields = state.allFields
        .filter { it.key.startsWith("img_") }
        .filter { state.getFieldData(it.key).isNotBlank() }
        .sortedBy { it.key }

    val monitoringFields = state.allFields.filter { it.key in monitoringKeys }
    val otherFields = state.allFields.filter {
        it.key !in monitoringKeys && !it.key.startsWith("img_")
    }

    otherFields.fastForEach { field ->
        val rawValue = state.getFieldData(field.key)
        if (rawValue.isNotBlank()) {
            FormReviewItem(label = field.label, value = rawValue)
            Spacer(Modifier.height(ScoutTheme.spacing.small))
        }
    }

    var index = 1
    while (true) {
        val fertilizerKey = "${FERTILIZER_TYPE_KEY}_$index"
        val brandKey = "${BRAND_KEY}_$index"

        if (!state.fieldData.containsKey(fertilizerKey)) break

        val nitrogenKey = "${NITROGEN_CONTENT_KEY}_$index"
        val phosphorusKey = "${PHOSPHORUS_CONTENT_KEY}_$index"
        val potassiumKey = "${POTASSIUM_CONTENT_KEY}_$index"
        val amountKey = "${AMOUNT_APPLIED_KEY}_$index"
        val unitKey = "${AMOUNT_UNIT_KEY}_$index"
        val cropStageKey = "${CROP_STAGE_ON_APPLICATION_KEY}_$index"

        Spacer(Modifier.height(ScoutTheme.spacing.small))
        Text(
            "Fertilizer Application $index",
            style = ScoutTheme.material.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = ScoutTheme.material.colorScheme.onSurface,
        )
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        FormReviewItem("  Fertilizer", state.getFieldData(fertilizerKey), isRow = true)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        FormReviewItem("  Brand", state.getFieldData(brandKey), isRow = true)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        FormReviewItem("  Nitrogen", state.getFieldData(nitrogenKey), isRow = true)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        FormReviewItem("  Phosphorus", state.getFieldData(phosphorusKey), isRow = true)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        FormReviewItem("  Potassium", state.getFieldData(potassiumKey), isRow = true)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        FormReviewItem("  Amount", "${state.getFieldData(amountKey)} ${state.getFieldData(unitKey)}", isRow = true)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
        FormReviewItem("  Crop Stage", state.getFieldData(cropStageKey), isRow = true)
        Spacer(Modifier.height(ScoutTheme.spacing.small))
        index++
    }

    if (monitoringFields.isNotEmpty()) {
        Column {
            Spacer(Modifier.height(ScoutTheme.spacing.small))
            Text(
                "Monitoring Visit",
                style = ScoutTheme.material.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ScoutTheme.material.colorScheme.onSurface,
            )
            Spacer(Modifier.height(ScoutTheme.spacing.small))
            monitoringFields.fastForEach { field ->
                val rawValue = state.getFieldData(field.key)
                if (rawValue.isNotBlank()) {
                    FormReviewItem(label = field.label, value = rawValue)
                    Spacer(Modifier.height(ScoutTheme.spacing.small))
                }
            }
            Spacer(Modifier.height(ScoutTheme.spacing.small))
        }
    }

    // Images section (if any)
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


