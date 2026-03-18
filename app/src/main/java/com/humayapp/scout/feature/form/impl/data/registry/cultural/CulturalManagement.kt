package com.humayapp.scout.feature.form.impl.data.registry.cultural

import androidx.compose.runtime.Composable
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.core.network.util.asJson
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.mapper.FormMapper
import com.humayapp.scout.feature.form.impl.data.registry.cultural.overrides.RiceVarietyInformationPage
import com.humayapp.scout.feature.form.impl.data.registry.cultural.review.CulturalManagementDetailsContent
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.json.JsonObject

sealed class CulturalManagement : WizardEntry() {

    data object FieldArea : CulturalManagement() {
        override val title = "Field Area & Ecosystem"
        override val description = "Describe the field’s physical area and key ecosystem characteristics"
        override val fields = listOf(
            field(
                key = MONITORING_FIELD_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Monitoring Field Area (by square meters)",

                // todo: check max should be the total field area in `FieldData
                // create a custom page to check only on validation of past forms feature are done
                validator = Validators.floatRange(min = 400.0f, unit = "sqm") { min, _, unit ->
                    "Monitoring field area must be least $min $unit"
                }
            ),
            field(
                key = ECOSYSTEM_KEY,
                type = FieldType.CARD_RADIO,
                label = "Ecosystem",
                options = listOf("Rainfed Lowland", "Irrigation"),
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = ActualCropEstablishment
    }

    data object ActualCropEstablishment : CulturalManagement() {
        override val title = "Actual Crop Establishment"
        override val description = "Provide the date and method used for establishing the crop."
        override val fields = listOf(
            /* field(
               key = ACTUAL_LAND_PREPARATION_METHOD,
                type = FieldType.CARD_RADIO,
                label = "Actual Land Preparation Method",
                options = listOf("Wet", "Dry"),
                validator = Validators.nonEmpty
            ),
             */
            field(
                key = ACTUAL_CROP_ESTABLISHMENT_DATE_KEY,
                type = FieldType.DATE,
                label = "Actual Crop Establishment Date",

                // todo: check max should be greater the estimated crop establishment in  in `FieldData
                // create a custom page to check only on validation of past forms feature are done
                validator = Validators.nonEmpty
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
                label = "Seedling Age at Transplanting (days)",
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
                label = "Distance Between Plant Row (1)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance between rows must be between $min and $max"
                }
            ),
            field(
                key = D_BETWEEN_PLANT_ROW_2_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Between Plant Row (2)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance between rows must be between $min and $max"
                }
            ),
            field(
                key = D_BETWEEN_PLANT_ROW_3_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Between Plant Row (3)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance between rows must be between $min and $max"
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
                label = "Distance Within Plant Row (1)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance within rows must be between $min and $max"
                }
            ),
            field(
                key = D_WITHIN_PLANT_ROW_2_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Within Plant Row (2)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance within rows must be between $min and $max"
                }
            ),
            field(
                key = D_WITHIN_PLANT_ROW_3_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Distance Within Plant Row (3)",
                // check unit
                validator = Validators.intRange(min = 10, max = 50, unit = "cm") { min, max, unit ->
                    "Distance within rows must be between $min and $max"
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
                label = "Seeding Rate (kg/ha)",
                validator = Validators.floatRange(min = 15.0f, max = 200.0f, unit = "kgs") { min, max, unit ->
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
                label = "Number of Plants (1)",
                validator = Validators.intRange(min = 5, max = 50) { min, max, _ ->
                    "Number of plants must be between $min and $max"
                }
            ),
            field(
                key = NUM_PLANTS_2_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Number of Plants (2)",
                validator = Validators.intRange(min = 5, max = 50) { min, max, _ ->
                    "Number of plants must be between $min and $max"
                }
            ),
            field(
                key = NUM_PLANTS_3_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Number of Plants (3)",
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
                label = "Rice Variety Maturity Duration",

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
    }

    companion object {
        fun serialize(answers: Map<String, Any?>): JsonObject = answers.asJson()

        val reviewContent: @Composable ((FormState) -> Unit) = { state -> CulturalManagementDetailsContent(state) }

        val pageOverrides: WizardPageOverrides = mapOf(
            RiceVarietyInformation to { page -> RiceVarietyInformationPage(page as RiceVarietyInformation) }
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
        )


        val mapper: FormMapper = object : FormMapper() {
            override suspend fun upload(entry: FormEntryEntity, client: SupabaseClient) {
                defaultMapping(
                    table = SupabaseDBTables.CROP_ESTABLISHMENTS,
                    entry = entry,
                    client = client,
                )
            }
        }

        const val MONITORING_FIELD_AREA_KEY = "monitoring_field_area_sqm"
        const val ECOSYSTEM_KEY = "ecosystem"

        // const val ACTUAL_LAND_PREPARATION_METHOD = "actual_land_preparation_method"
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

