package com.humayapp.scout.feature.form.impl.data.registry.production

import com.humayapp.scout.core.network.util.asJson
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ImagesPage
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.overrides.ConditionPage
import com.humayapp.scout.feature.form.impl.data.registry.production.overrides.HarvestYieldPage
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import kotlinx.serialization.json.JsonObject

sealed class Production : WizardEntry() {

    object HarvestDetails : Production() {
        override val title = "Harvest Details"
        override val description = "Record harvest date and method used"
        override val fields = listOf(
            field(
                key = HARVEST_DATE_KEY,
                type = FieldType.DATE,
                label = "Harvest Date",

                // to be determined
                // TO DO: check max should be 90 - 130 days after the actual establishment date in `CulturalManagement`
                // create a custom page to check only on validation of past forms feature are done
                validator = Validators.nonEmpty
            ),
            field(
                key = HARVEST_METHOD_KEY,
                type = FieldType.CARD_RADIO,
                label = "Harvesting Method",
                options = listOf("Manual", "Mechanical", "Other"),

                validator = Validators.nonEmpty
            )
        )

        override fun nextScreen(answers: Map<String, Any?>) = HarvestedAreaAndIrrigation
    }

    object HarvestedAreaAndIrrigation : Production() {
        override val title = "Harvested Area and Irrigation"
        override val description = "Record harvested area and irrigation adequacy during season"
        override val fields = listOf(
            field(
                key = AREA_HARVESTED,
                type = FieldType.NUM_DECIMAL,
                label = "Area Harvested (by hectares)",

                validator = Validators.allOf(
                    Validators.floatRange(min = 400.0f, max = 10_000_000f, unit = "ha") { min, max, unit ->
                        "Area harvested must be between $min to $max $unit"
                    },
                    Validators.notExceedTotalFieldArea(FieldData.TOTAL_FIELD_AREA_KEY)
                )
            ),
            field(
                key = IRRIGATION_SUPPLY,
                type = FieldType.DROPDOWN,
                label = "Irrigation Supply",
                options = listOf("Not Enough", "Not Sufficient", "Sufficient", "Excessive"),
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = HarvestYield
    }

    object HarvestYield : Production() {
        override val title = "Harvest Yield"
        override val description = "Record number of bags harvested and average bag weight"
        override val fields = listOf(
            field(
                key = BAGS_HARVESTED_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Bags Harvested",

                validator = Validators.intRange(min = 0) { min, _, _ ->
                    "Bags harvested must be at least $min"
                }
            ),
            field(
                key = AVG_BAG_WEIGHT_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Average Bag Weight (by kilogram)",
                validator = Validators.floatRange(min = 1.0f, max = 30.0f, unit = "kg") { min, max, unit ->
                    "Bags harvested must be between $min$unit and $max$unit"
                }
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = MonitoringVisit.MonitoringDate
    }

    companion object {
        fun serialize(answers: Map<String, Any?>): JsonObject = answers.asJson(
            includeKey = { key -> !key.startsWith("img_") && key != "season_id" && key != FieldData.TOTAL_FIELD_AREA_KEY },
        )

        val pageOverrides: WizardPageOverrides = mapOf(
            HarvestYield to { page -> HarvestYieldPage(page as HarvestYield) },
            MonitoringVisit.Conditions to { page -> ConditionPage(page as MonitoringVisit.Conditions) },
            MonitoringVisit.Images to { page -> ImagesPage(page as MonitoringVisit.Images) }
        )

        val startEntry = HarvestDetails
        val entries = listOf(HarvestDetails, HarvestedAreaAndIrrigation, HarvestYield) + listOf(
            MonitoringVisit.MonitoringDate,
            MonitoringVisit.Conditions,
            MonitoringVisit.Images
        )

        const val HARVEST_DATE_KEY = "harvest_date"
        const val HARVEST_METHOD_KEY = "harvesting_method"
        const val BAGS_HARVESTED_KEY = "bags_harvested"
        const val AVG_BAG_WEIGHT_KEY = "avg_bag_weight_kg"
        const val AREA_HARVESTED = "area_harvested_ha"
        const val IRRIGATION_SUPPLY = "irrigation_supply"
    }
}


