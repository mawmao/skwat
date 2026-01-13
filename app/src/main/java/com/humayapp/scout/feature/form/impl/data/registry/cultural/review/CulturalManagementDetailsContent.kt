package com.humayapp.scout.feature.form.impl.data.registry.cultural.review

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement
import com.humayapp.scout.feature.form.impl.ui.components.FormReviewItem


@Composable
fun CulturalManagementDetailsContent(state: FormState) {
    val method = state.getFieldData(CulturalManagement.ACTUAL_CROP_ESTABLISHMENT_METHOD_KEY)

    val excludedKeys = when (method) {
        "Direct-seeded" -> listOf(
            CulturalManagement.TransplantedDetails,
            CulturalManagement.TransplantedPlantSpacingWithin,
            CulturalManagement.TransplantedPlantSpacingBetween,
        ).flatMap { it.fields }.map { it.key }.toSet()

        "Transplanted" -> listOf(
            CulturalManagement.DirectSeededDetails,
            CulturalManagement.DirectSeededPlantCount
        )
            .flatMap { it.fields }
            .map { it.key }.toSet()

        else -> emptySet()
    }


    // todo: handle images in the future
    state.allFields.filterNot { it.key in excludedKeys }.fastForEach { field ->
        val value = state.getFieldData(field.key)
            .takeIf { it.isNotBlank() } ?: return@fastForEach

        FormReviewItem(label = field.label, value = value)
        Spacer(Modifier.height(ScoutTheme.spacing.extraSmall))
    }
}