package com.humayapp.scout.feature.form.impl.data.registry.nutrient.review

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.AMOUNT_APPLIED_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.AMOUNT_UNIT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.BRAND_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.CROP_STAGE_ON_APPLICATION_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.FERTILIZER_TYPE_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.NITROGEN_CONTENT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.PHOSPHORUS_CONTENT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.POTASSIUM_CONTENT_KEY
import com.humayapp.scout.feature.form.impl.ui.components.FormReviewItem


@Composable
fun NutrientManagementReviewContent(state: FormState) {

    state.allFields.fastForEach { field ->
        val value = state.getFieldData(field.key).takeIf { it.isNotBlank() } ?: "N/A"
        FormReviewItem(field.label, value)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
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

        Text(
            "Fertilizer Application $index",
            style = ScoutTheme.material.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = ScoutTheme.material.colorScheme.onSurface,
        )

        FormReviewItem("  Fertilizer", state.getFieldData(fertilizerKey), isRow = true)
        FormReviewItem("  Brand", state.getFieldData(brandKey), isRow = true)
        FormReviewItem("  Nitrogen", state.getFieldData(nitrogenKey), isRow = true)
        FormReviewItem("  Phosphorus", state.getFieldData(phosphorusKey), isRow = true)
        FormReviewItem("  Potassium", state.getFieldData(potassiumKey), isRow = true)
        FormReviewItem("  Amount", "${state.getFieldData(amountKey)} ${state.getFieldData(unitKey)}", isRow = true)
        FormReviewItem("  Crop Stage", state.getFieldData(cropStageKey), isRow = true)

        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))

        index++
    }
}