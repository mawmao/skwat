package com.humayapp.scout.feature.form.impl.data.registry.nutrient

import androidx.compose.runtime.Composable
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.mapper.NutrientManagementMapper
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.overrides.application.FertilizerApplicationPage
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.review.NutrientManagementReviewContent
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

sealed class NutrientManagement : WizardEntry() {

    data object FertilizedArea : NutrientManagement() {
        override val title = "Fertilized Area"
        override val description = "Track fertilized area and related information"
        override val fields = listOf(
            field(
                key = APPLIED_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Fertilized Area (sqm)",

                // todo: check max should not exceed monitoring field area in `CulturalManagement`
                // create a custom page to check only on validation of past forms feature are done
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = FertilizerApplication
    }

    data object FertilizerApplication : NutrientManagement() {
        override val title = "Fertilizer Application"
        override val description = "Records fertilizer use"
        override val fields = emptyList<WizardField>()

        override val nextRule: (FormState) -> Boolean = { state ->
            // only checked one since you can't even add a single key without the other keys
            val hasApplication = state.fieldData.keys.any { it.startsWith(FERTILIZER_TYPE_KEY) }
            if (!hasApplication) {
                state.setDialog(
                    FormState.Dialog(
                        title = "Missing Application",
                        message = "You need to add at least one fertilizer application before proceeding."
                    )
                )
            }

            hasApplication
        }


        fun indexedFields(index: Int): List<WizardField> = listOf(
            field(
                key = "${FERTILIZER_TYPE_KEY}_$index",
                label = "Fertilizer Type",
                type = FieldType.DROPDOWN,
                options = listOf("Organic", "Inorganic", "Compost", "Other"),
                validator = Validators.nonEmpty
            ),
            field(
                key = "${BRAND_KEY}_$index",
                type = FieldType.NAME,
                label = "Brand Name",
                validator = Validators.name
            ),
            field(
                key = "${NITROGEN_CONTENT_KEY}_$index",
                type = FieldType.NUM_PERCENT,
                label = "N %",
                validator = Validators.floatRange(min = 0.0f, max = 100.0f, unit = "%") { min, max, unit ->
                    "Nitrogen content must be between $min$unit and $max$unit"
                }
            ),
            field(
                key = "${PHOSPHORUS_CONTENT_KEY}_$index",
                type = FieldType.NUM_PERCENT,
                label = "P %",
                validator = Validators.floatRange(min = 0.0f, max = 100.0f, unit = "%") { min, max, unit ->
                    "Phosphorus content must be between $min$unit and $max$unit"
                }
            ),
            field(
                key = "${POTASSIUM_CONTENT_KEY}_$index",
                type = FieldType.NUM_PERCENT,
                label = "K %",
                validator = Validators.floatRange(min = 0.0f, max = 100.0f, unit = "%") { min, max, unit ->
                    "Potassium content must be between $min$unit and $max$unit"
                }
            ),
            field(
                key = "${AMOUNT_APPLIED_KEY}_$index",
                type = FieldType.NUM_DECIMAL,
                label = "Amount Applied",
                validator = Validators.intRange(min = 0) { min, _, _ ->
                    "Amount applied must be at least $min"
                }
            ),
            field(
                key = "${AMOUNT_UNIT_KEY}_$index",
                type = FieldType.DROPDOWN,
                label = "Unit",
                options = listOf("kg", "g"),
                validator = Validators.nonEmpty
            ),
            field(
                key = "${CROP_STAGE_ON_APPLICATION_KEY}_$index",
                type = FieldType.DROPDOWN,
                label = "Crop Stage on Application",
                options = listOf("Pre-planting", "Vegetative", "Flowering", "Maturity"),
                validator = Validators.nonEmpty
            ),
        )
    }

    companion object {

        val pageOverrides: WizardPageOverrides = mapOf(
            FertilizerApplication to { page -> FertilizerApplicationPage(page as FertilizerApplication) }
        )

        val startEntry = FertilizedArea
        val entries = listOf(FertilizedArea, FertilizerApplication)
        val mapper = NutrientManagementMapper

        val reviewContent: @Composable ((FormState) -> Unit) = { state -> NutrientManagementReviewContent(state) }

        fun serialize(answers: Map<String, Any?>): JsonObject = serializeImpl(answers)

        const val APPLIED_AREA_KEY = "applied_area_sqm"
        const val FERTILIZER_TYPE_KEY = "fertilizer_type"
        const val BRAND_KEY = "brand"
        const val NITROGEN_CONTENT_KEY = "nitrogen_content_pct"
        const val PHOSPHORUS_CONTENT_KEY = "phosphorus_content_pct"
        const val POTASSIUM_CONTENT_KEY = "potassium_content_pct"
        const val AMOUNT_APPLIED_KEY = "amount_applied"
        const val AMOUNT_UNIT_KEY = "amount_unit"
        const val CROP_STAGE_ON_APPLICATION_KEY = "crop_stage_on_application"
    }
}


private fun serializeImpl(answers: Map<String, Any?>): JsonObject {
    val appliedArea = answers["applied_area_sqm"]

    val indices = answers.keys
        .mapNotNull { "_(\\d+)$".toRegex().find(it)?.groupValues?.get(1)?.toInt() }
        .distinct()
        .sorted()

    val fertilizerApplications = indices.map { index ->
        val fields = answers
            .filterKeys { it.endsWith("_$index") }
            .mapKeys { it.key.removeSuffix("_$index") }
            .mapValues { (_, value) ->
                when (value) {
                    is Number -> JsonPrimitive(value)
                    is Boolean -> JsonPrimitive(value)
                    is String -> JsonPrimitive(value)
                    null -> JsonNull
                    else -> JsonPrimitive(value.toString())
                }
            }
        JsonObject(fields)
    }

    return buildJsonObject {
        put("applied_area_sqm", appliedArea?.let { JsonPrimitive(it.toString()) } ?: JsonNull)
        put("fertilizer_application", JsonArray(fertilizerApplications))
    }
}


