package com.humayapp.scout.feature.form.impl.data.registry.nutrient

import androidx.compose.runtime.Composable
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ImagesPage
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.overrides.ConditionPage
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.APPLIED_AREA_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.overrides.application.FertilizerApplicationPage
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.review.NutrientManagementReviewContent
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import com.humayapp.scout.feature.form.impl.model.fieldThresholdRule
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed class NutrientManagement : WizardEntry() {

    data object FertilizedArea : NutrientManagement() {
        override val title = "Fertilized Area"
        override val description = "Track fertilized area and related information"
        override val fields = listOf(
            field(
                key = APPLICATION_DATE_KEY,
                type = FieldType.DATE,
                label = "Application Date",
                validator = Validators.nonEmpty
            ),
            field(
                key = APPLIED_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Fertilized Area (by square meters)",
                validator = Validators.floatRangeWithinMonitoringArea(
                    min = 1f,
                    monitoringAreaKey = CulturalManagement.MONITORING_FIELD_AREA_KEY,
                    unit = "sqm"
                )
            ),

            )

        override val nextRule = fieldThresholdRule(
            key = APPLIED_AREA_KEY,
            threshold = 10_000_000f,
            message = { "Fertilized area is $it sqm, which exceeds 10,000,000 ha. Press OK to proceed." }
        )

        override fun nextScreen(answers: Map<String, Any?>) = FertilizerApplication
    }

    data object FertilizerApplication : NutrientManagement() {
        override val title = "Fertilizer Application"
        override val description = "Records fertilizer use"
        override val fields = emptyList<WizardField>()

        override fun nextScreen(answers: Map<String, Any?>) = MonitoringVisit.MonitoringDate

        override val nextRule: (FormState) -> Boolean = { state ->
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
                validator = Validators.intRange(min = 1) { min, _, _ ->
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
            FertilizerApplication to { page -> FertilizerApplicationPage(page as FertilizerApplication) },
            MonitoringVisit.Conditions to { page -> ConditionPage(page as MonitoringVisit.Conditions) },
            MonitoringVisit.Images to { page -> ImagesPage(page as MonitoringVisit.Images) }
        )

        val startEntry = FertilizedArea
        val entries = listOf(
            FertilizedArea,
            FertilizerApplication
        ) + listOf(
            MonitoringVisit.MonitoringDate,
            MonitoringVisit.Conditions,
            MonitoringVisit.Images
        )

        val reviewContent: @Composable ((FormState) -> Unit) = { state -> NutrientManagementReviewContent(state) }

        fun serialize(answers: Map<String, Any?>): JsonObject = serializeImpl(answers)


        fun nutrientManagementJsonToAnswers(json: JsonElement, imageUrls: List<String>? = emptyList()): Map<String, Any?> {
            val obj = json.jsonObject
            val answers = mutableMapOf<String, Any?>()

            // Top‑level fields (flat)
            obj["applied_area_sqm"]?.let { answers[APPLIED_AREA_KEY] = it.jsonPrimitive.content }
            obj["date_monitored"]?.let { answers["date_monitored"] = it.jsonPrimitive.content }
            obj["crop_stage"]?.let { answers["crop_stage"] = it.jsonPrimitive.content }
            obj["soil_moisture_status"]?.let { answers["soil_moisture_status"] = it.jsonPrimitive.content }
            obj["avg_plant_height"]?.let { answers["avg_plant_height"] = it.jsonPrimitive.content.toDoubleOrNull() }

            // Applications array
            val applicationsArray = obj["applications"]?.jsonArray ?: return answers
            applicationsArray.forEachIndexed { idx, appElement ->
                val app = appElement.jsonObject
                val index = idx + 1
                answers["${FERTILIZER_TYPE_KEY}_$index"] = app["fertilizer_type"]?.jsonPrimitive?.content
                answers["${BRAND_KEY}_$index"] = app["brand"]?.jsonPrimitive?.content
                answers["${NITROGEN_CONTENT_KEY}_$index"] = app["nitrogen_content_pct"]?.jsonPrimitive?.content?.toFloatOrNull()
                answers["${PHOSPHORUS_CONTENT_KEY}_$index"] =
                    app["phosphorus_content_pct"]?.jsonPrimitive?.content?.toFloatOrNull()
                answers["${POTASSIUM_CONTENT_KEY}_$index"] = app["potassium_content_pct"]?.jsonPrimitive?.content?.toFloatOrNull()
                answers["${AMOUNT_APPLIED_KEY}_$index"] = app["amount_applied"]?.jsonPrimitive?.content?.toFloatOrNull()
                answers["${AMOUNT_UNIT_KEY}_$index"] = app["amount_unit"]?.jsonPrimitive?.content
                answers["${CROP_STAGE_ON_APPLICATION_KEY}_$index"] = app["crop_stage_on_application"]?.jsonPrimitive?.content
            }

            imageUrls?.forEachIndexed { index, url ->
                answers["img_${index + 1}"] = url
            }

            return answers
        }

        const val APPLICATION_DATE_KEY = "application_date"
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
    // Existing logic for applied area and applications
    val appliedArea = answers[APPLIED_AREA_KEY]
    val indices = answers.keys
        .mapNotNull { "_(\\d+)$".toRegex().find(it)?.groupValues?.get(1)?.toInt() }
        .distinct()
        .sorted()

    val fertilizerApplications = indices.mapNotNull { index ->
        val fields = answers
            .filterKeys { it.endsWith("_$index") && !it.startsWith("img_") && it != "season_id" }
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
        if (fields.isEmpty()) null else JsonObject(fields)
    }

    // --- Monitoring visit fields (will be added as top-level) ---
    val monitoringVisitKeys = setOf(
        "date_monitored",
        "crop_stage",
        "soil_moisture_status",
        "avg_plant_height"
    )

    return buildJsonObject {
        // Add applied area
        put(APPLIED_AREA_KEY, appliedArea?.let { JsonPrimitive(it.toString()) } ?: JsonNull)

        // Add applications array
        put("applications", JsonArray(fertilizerApplications))

        // Add monitoring visit fields directly as top-level keys
        answers.forEach { (key, value) ->
            if (key in monitoringVisitKeys) {
                val jsonValue = when (value) {
                    is Number -> JsonPrimitive(value)
                    is Boolean -> JsonPrimitive(value)
                    is String -> JsonPrimitive(value)
                    null -> JsonNull
                    else -> JsonPrimitive(value.toString())
                }
                put(key, jsonValue)
            }
        }
    }
}
