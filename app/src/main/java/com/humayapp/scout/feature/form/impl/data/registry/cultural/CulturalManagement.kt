package com.humayapp.scout.feature.form.impl.data.registry.cultural

import androidx.compose.runtime.Composable
import com.humayapp.scout.core.network.util.asJson
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.overrides.RiceVarietyInformationPage
import com.humayapp.scout.feature.form.impl.data.registry.cultural.review.CulturalManagementDetailsContent
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ImagesPage
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.overrides.ConditionPage
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import com.humayapp.scout.feature.form.impl.model.fieldThresholdRule
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed class CulturalManagement : WizardEntry() {

    data object FieldArea : CulturalManagement() {
        override val title = "Field Area & Ecosystem"
        override val description = "Describe the field’s physical area and key ecosystem characteristics"
        override val fields = listOf(
            field(
                key = MONITORING_FIELD_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Monitoring Field Area (by square meters)",
                validator = Validators.allOf(
                    Validators.floatRange(min = 400.0f, max = 10_000_000f, unit = "sqm") { min, max, unit ->
                        "Monitoring field area must be between $min to $max $unit"
                    },
                    Validators.notExceedTotalFieldArea(FieldData.TOTAL_FIELD_AREA_KEY)
                )
            ),
            field(
                key = ECOSYSTEM_KEY,
                type = FieldType.CARD_RADIO,
                label = "Ecosystem",
                options = listOf("Rainfed Lowland", "Irrigation"),
                validator = Validators.nonEmpty
            ),
        )

        override val nextRule = fieldThresholdRule(
            key = MONITORING_FIELD_AREA_KEY,
            threshold = 10_000_000f,
            message = { "Monitoring field area is $it sqm, which exceeds 10,000,000 ha. Press OK to proceed." }
        )

        override fun nextScreen(answers: Map<String, Any?>) = ActualCropEstablishment
    }

    data object ActualCropEstablishment : CulturalManagement() {
        override val title = "Actual Crop Establishment"
        override val description = "Provide the date and method used for establishing the crop."
        override val fields = listOf(
            field(
                key = ACTUAL_CROP_ESTABLISHMENT_DATE_KEY,
                type = FieldType.DATE,
                label = "Actual Crop Establishment Date",
                validator = Validators.isAfterBy(FieldData.EST_CROP_ESTABLISHMENT_KEY)
            ),
            field(
                key = ACTUAL_CROP_ESTABLISHMENT_METHOD_KEY,
                type = FieldType.CARD_RADIO,
                label = "Actual Crop Establishment Method",
                options = listOf("Direct-seeded", "Transplanted"),
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>): WizardEntry? {
            return when (val method = answers[ACTUAL_CROP_ESTABLISHMENT_METHOD_KEY]) {
                "Direct-seeded" -> DirectSeededDetails
                "Transplanted" -> TransplantedDetails
                else -> null
            }
        }
    }

    data object TransplantedDetails : CulturalManagement() {
        override val title = "Transplanting Details"
        override val description = "Record the sowing date and the age of seedlings at transplanting."
        override val fields = listOf(
            field(
                key = SOWING_DATE_KEY,
                type = FieldType.DATE,
                label = "Sowing Date",
                validator = Validators.isAfterBy(ACTUAL_CROP_ESTABLISHMENT_DATE_KEY)
            ),
            field(
                key = SEEDLING_AGE_AT_TRANSPLANTING_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Seedling Age at Transplanting (by days)",
                validator = Validators.intRange(min = 10, max = 60, unit = "days") { min, max, unit ->
                    "Seedling age must be between $min and $max $unit"
                }
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = TransplantedPlantSpacingBetween
    }

    data object TransplantedPlantSpacingBetween : CulturalManagement() {
        override val title = "Plant Spacing Between Rows"
        override val description = "Measure the distance between each plant row in the field."
        override val fields = listOf(
            field(
                key = D_BETWEEN_PLANT_ROW_1_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Between Plant Row #1 (by centimeters)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance between rows must be between $min and $max $unit"
                }
            ),
            field(
                key = D_BETWEEN_PLANT_ROW_2_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Between Plant Row #2 (by centimeters)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance between rows must be between $min and $max $unit"
                }
            ),
            field(
                key = D_BETWEEN_PLANT_ROW_3_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Between Plant Row #3 (by centimeters)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance between rows must be between $min and $max $unit"
                }
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = TransplantedPlantSpacingWithin
    }

    data object TransplantedPlantSpacingWithin : CulturalManagement() {
        override val title = "Plant Spacing Within Rows"
        override val description = "Measure the distance between plants within the same row."
        override val fields = listOf(
            field(
                key = D_WITHIN_PLANT_ROW_1_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Within Plant Row #1 (by centimeters)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance within rows must be between $min and $max $unit"
                }
            ),
            field(
                key = D_WITHIN_PLANT_ROW_2_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Within Plant Row #2 (by centimeters)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance within rows must be between $min and $max $unit"
                }
            ),
            field(
                key = D_WITHIN_PLANT_ROW_3_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Within Plant Row #3 (by centimeters)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance within rows must be between $min and $max $unit"
                }
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = RiceVarietyInformation
    }

    data object DirectSeededDetails : CulturalManagement() {
        override val title = "Direct Seeding Details"
        override val description = "Record the seeding rate and the direct seeding method used."
        override val fields = listOf(
            field(
                key = SEEDING_RATE_KG_HA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Seeding Rate (kg)",
                validator = Validators.floatRange(min = 15.0f, max = 200.0f, unit = "kg") { min, max, unit ->
                    "Seeding rate must be between $min and $max $unit"
                }
            ),
            field(
                key = DIRECT_SEEDING_METHOD_KEY,
                type = FieldType.CARD_RADIO,
                label = "Direct Seeding Method",
                options = listOf("Broadcasted"),
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = DirectSeededPlantCount
    }

    data object DirectSeededPlantCount : CulturalManagement() {
        override val title = "Number of Plants"
        override val description = "Enter the number of plants observed in each row."
        override val fields = listOf(
            field(
                key = NUM_PLANTS_1_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Number of Plants #1",
                validator = Validators.intRange(min = 5, max = 50) { min, max, _ ->
                    "Number of plants must be between $min and $max"
                }
            ),
            field(
                key = NUM_PLANTS_2_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Number of Plants #2",
                validator = Validators.intRange(min = 5, max = 50) { min, max, _ ->
                    "Number of plants must be between $min and $max"
                }
            ),
            field(
                key = NUM_PLANTS_3_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Number of Plants #3",
                validator = Validators.intRange(min = 5, max = 50) { min, max, _ ->
                    "Number of plants must be between $min and $max"
                }
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = RiceVarietyInformation
    }

    data object RiceVarietyInformation : CulturalManagement() {
        override val title = "Rice Variety Information"
        override val description =
            "Select the rice variety, its number, and the expected maturity duration."
        override val fields = listOf(
            field(
                key = RICE_VARIETY_KEY,
                type = FieldType.CARD_RADIO,
                label = "Rice Variety",
                options = listOf("NSIC Rc", "PSB Rc", "Other")
            ),
            field(
                key = RICE_VARIETY_NO_KEY,
                type = FieldType.NUM_WHOLE, // 2 or 3 digits depending on rice variety
                label = "Rice Variety No.",
                validator = Validators.lengthBasedOn(
                    otherKey = RICE_VARIETY_KEY,
                    mapping = mapOf(
                        "NSIC Rc" to 3,
                        "PSB Rc" to 2
                    ),
                    allowOther = true
                )
            ),
            field(
                key = RICE_VARIETY_MATURITY_DURATION_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Rice Variety Maturity Duration (by days)",

                // it is 90-150 in pdf, but reduced to 60 to be more flexible
                validator = Validators.intRange(min = 60, max = 150, unit = "days") { min, max, unit ->
                    "Rice variety maturity duration must be between $min and $max $unit"
                }
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = SeedClass
    }

    data object SeedClass : CulturalManagement() {
        override val title = "Seed Class"
        override val description = "Specify the class of seed used for planting."
        override val fields = listOf(
            field(
                key = SEED_CLASS_KEY,
                type = FieldType.DROPDOWN,
                label = "Seed Class",
                options = listOf("Foundation", "Hybrid", "Registered", "Certified", "Good"),
                validator = Validators.nonEmpty
            )
        )


        override fun nextScreen(answers: Map<String, Any?>) = MonitoringVisit.MonitoringDate
    }

    companion object {
        fun serialize(answers: Map<String, Any?>): JsonObject = answers.asJson(
            includeKey = { key -> !key.startsWith("img_") && key != "season_id" && key != FieldData.TOTAL_FIELD_AREA_KEY && key != FieldData.EST_CROP_ESTABLISHMENT_KEY },
        )

        val reviewContent: @Composable ((FormState) -> Unit) = { state -> CulturalManagementDetailsContent(state) }

        val pageOverrides: WizardPageOverrides = mapOf(
            RiceVarietyInformation to { page -> RiceVarietyInformationPage(page as RiceVarietyInformation) },
            MonitoringVisit.Conditions to { page -> ConditionPage(page as MonitoringVisit.Conditions) },
            MonitoringVisit.Images to { page -> ImagesPage(page as MonitoringVisit.Images) }
        )

        val startEntry = FieldArea
        val entries = listOf(
            FieldArea,
            ActualCropEstablishment,
            TransplantedDetails,
            TransplantedPlantSpacingBetween,
            TransplantedPlantSpacingWithin,
            DirectSeededDetails,
            DirectSeededPlantCount,
            RiceVarietyInformation,
            SeedClass
        ) + listOf(
            MonitoringVisit.MonitoringDate,
            MonitoringVisit.Conditions,
            MonitoringVisit.Images
        )

        fun culturalManagementJsonToAnswers(
            json: JsonElement,
            imageUrls: List<String>? = emptyList()
        ): Map<String, Any?> {
            val obj = json.jsonObject
            val answers = mutableMapOf<String, Any?>()

            obj["ecosystem"]?.let { answers[ECOSYSTEM_KEY] = it.jsonPrimitive.content }
            obj["monitoring_field_area_sqm"]?.let {
                answers[MONITORING_FIELD_AREA_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["actual_crop_establishment_date"]?.let { answers[ACTUAL_CROP_ESTABLISHMENT_DATE_KEY] = it.jsonPrimitive.content }
            obj["actual_crop_establishment_method"]?.let {
                answers[ACTUAL_CROP_ESTABLISHMENT_METHOD_KEY] = it.jsonPrimitive.content
            }
            obj["sowing_date"]?.let { answers[SOWING_DATE_KEY] = it.jsonPrimitive.content }
            obj["seedling_age_at_transplanting"]?.let {
                answers[SEEDLING_AGE_AT_TRANSPLANTING_KEY] = it.jsonPrimitive.content.toIntOrNull()
            }
            obj["distance_between_plant_row_1"]?.let {
                answers[D_BETWEEN_PLANT_ROW_1_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["distance_between_plant_row_2"]?.let {
                answers[D_BETWEEN_PLANT_ROW_2_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["distance_between_plant_row_3"]?.let {
                answers[D_BETWEEN_PLANT_ROW_3_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["distance_within_plant_row_1"]?.let {
                answers[D_WITHIN_PLANT_ROW_1_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["distance_within_plant_row_2"]?.let {
                answers[D_WITHIN_PLANT_ROW_2_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["distance_within_plant_row_3"]?.let {
                answers[D_WITHIN_PLANT_ROW_3_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["seeding_rate_kg_ha"]?.let { answers[SEEDING_RATE_KG_HA_KEY] = it.jsonPrimitive.content.toDoubleOrNull() }
            obj["direct_seeding_method"]?.let { answers[DIRECT_SEEDING_METHOD_KEY] = it.jsonPrimitive.content }
            obj["num_plants_1"]?.let { answers[NUM_PLANTS_1_KEY] = it.jsonPrimitive.content.toIntOrNull() }
            obj["num_plants_2"]?.let { answers[NUM_PLANTS_2_KEY] = it.jsonPrimitive.content.toIntOrNull() }
            obj["num_plants_3"]?.let { answers[NUM_PLANTS_3_KEY] = it.jsonPrimitive.content.toIntOrNull() }
            obj["rice_variety"]?.let { answers[RICE_VARIETY_KEY] = it.jsonPrimitive.content }
            obj["rice_variety_no"]?.let { answers[RICE_VARIETY_NO_KEY] = it.jsonPrimitive.content }
            obj["rice_variety_maturity_duration"]?.let {
                answers[RICE_VARIETY_MATURITY_DURATION_KEY] = it.jsonPrimitive.content.toIntOrNull()
            }
            obj["seed_class"]?.let { answers[SEED_CLASS_KEY] = it.jsonPrimitive.content }

            val monitoringVisitJson = obj["monitoring_visit"]?.jsonObject
            if (monitoringVisitJson != null) {
                monitoringVisitJson["date_monitored"]?.let { answers["date_monitored"] = it.jsonPrimitive.content }
                monitoringVisitJson["crop_stage"]?.let { answers["crop_stage"] = it.jsonPrimitive.content }
                monitoringVisitJson["soil_moisture_status"]?.let { answers["soil_moisture_status"] = it.jsonPrimitive.content }
                monitoringVisitJson["avg_plant_height"]?.let {
                    answers["avg_plant_height"] = it.jsonPrimitive.content.toDoubleOrNull()
                }
            }

            imageUrls?.forEachIndexed { index, url ->
                answers["img_${index + 1}"] = url
            }

            return answers
        }

        const val MONITORING_FIELD_AREA_KEY = "monitoring_field_area_sqm"
        const val ECOSYSTEM_KEY = "ecosystem"

        const val ACTUAL_CROP_ESTABLISHMENT_DATE_KEY = "actual_crop_establishment_date"
        const val ACTUAL_CROP_ESTABLISHMENT_METHOD_KEY = "actual_crop_establishment_method"

        // if transplanted
        const val SOWING_DATE_KEY = "sowing_date"
        const val SEEDLING_AGE_AT_TRANSPLANTING_KEY = "seedling_age_at_transplanting"
        const val D_BETWEEN_PLANT_ROW_1_KEY = "distance_between_plant_row_1"
        const val D_BETWEEN_PLANT_ROW_2_KEY = "distance_between_plant_row_2"
        const val D_BETWEEN_PLANT_ROW_3_KEY = "distance_between_plant_row_3"
        const val D_WITHIN_PLANT_ROW_1_KEY = "distance_within_plant_row_1"
        const val D_WITHIN_PLANT_ROW_2_KEY = "distance_within_plant_row_2"
        const val D_WITHIN_PLANT_ROW_3_KEY = "distance_within_plant_row_3"

        // if direct-seeded
        const val SEEDING_RATE_KG_HA_KEY = "seeding_rate_kg_ha"
        const val DIRECT_SEEDING_METHOD_KEY = "direct_seeding_method"
        const val NUM_PLANTS_1_KEY = "num_plants_1"
        const val NUM_PLANTS_2_KEY = "num_plants_2"
        const val NUM_PLANTS_3_KEY = "num_plants_3"

        const val RICE_VARIETY_KEY = "rice_variety"
        const val RICE_VARIETY_NO_KEY = "rice_variety_no"
        const val RICE_VARIETY_MATURITY_DURATION_KEY = "rice_variety_maturity_duration"
        const val SEED_CLASS_KEY = "seed_class"
    }
}

