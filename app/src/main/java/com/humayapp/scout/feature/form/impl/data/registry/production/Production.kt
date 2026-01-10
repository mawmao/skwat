package com.humayapp.scout.feature.form.impl.data.registry.production

import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.core.network.util.asJson
import com.humayapp.scout.feature.form.impl.data.mapper.FormMapper
import com.humayapp.scout.feature.form.impl.data.registry.production.overrides.HarvestYieldPage
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed class Production : WizardEntry() {


    object HarvestDetails : Production() {
        override val title = "Harvest Details"
        override val description = "Record harvest date and method used"
        override val fields = listOf(
            field(key = HARVEST_DATE_KEY, type = FieldType.DATE, label = "Harvest Date"),
            field(
                key = HARVEST_METHOD_KEY,
                type = FieldType.CARD_RADIO,
                label = "Harvesting Method",
                options = listOf("Manual", "Mechanical", "Other"),
                // TODO: validation should be 90-130 days after the actual establishment date
            )
        )

        override fun nextScreen(answers: Map<String, Any?>) = HarvestedAreaAndIrrigation
    }

    @kotlinx.serialization.Serializable
    object HarvestedAreaAndIrrigation : Production() {
        override val title = "Harvested Area and Irrigation"
        override val description = "Record harvested area and irrigation adequacy during season"
        override val fields = listOf(
            field(
                key = AREA_HARVESTED,
                type = FieldType.NUM_DECIMAL,
                label = "Area Harvested (ha)",
                // TODO: validation less than total field area
                // TODO: validation greater than 0.04 hectares
            ),
            field(
                key = IRRIGATION_SUPPLY,
                type = FieldType.DROPDOWN,
                label = "Irrigation Supply",
                options = listOf("Not Enough", "Not Sufficient", "Sufficient", "Excessive")
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = HarvestYield
    }

    @Serializable
    object HarvestYield : Production() {
        override val title = "Harvest Yield (Bags)"
        override val description = "Record number of bags harvested and average bag weight"
        override val fields = listOf(
            field(
                key = BAGS_HARVESTED_KEY,
                type = FieldType.NUM_WHOLE,
                label = "Bags Harvested",
                // todo: validation = min(0.0, "Bags harvested should not be less than 0")
            ),
            field(
                key = AVG_BAG_WEIGHT_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Average Bag Weight (kg)",
                // todo: validation: numLength(30.0..80.0, "Average bag weight should be between 30kg to 80kg")
            ),
        )

    }

    companion object {
        fun serialize(answers: Map<String, Any?>): JsonObject = answers.asJson()

        val pageOverrides: WizardPageOverrides = mapOf(
            HarvestYield to { page -> HarvestYieldPage(page as HarvestYield) }
        )

        val startEntry = HarvestDetails
        val entries = listOf(HarvestDetails, HarvestedAreaAndIrrigation, HarvestYield)

        val mapper: FormMapper = object : FormMapper() {
            override suspend fun upload(entry: FormEntryEntity, client: SupabaseClient) {
                defaultMapping(
                    table = SupabaseDBTables.HARVEST_RECORDS,
                    entry = entry,
                    client = client,
                )
            }
        }

        const val HARVEST_DATE_KEY = "harvest_date"
        const val HARVEST_METHOD_KEY = "harvesting_method"
        const val BAGS_HARVESTED_KEY = "bags_harvested"
        const val AVG_BAG_WEIGHT_KEY = "avg_bag_weight_kg"
        const val AREA_HARVESTED = "area_harvested_ha"
        const val IRRIGATION_SUPPLY = "irrigation_supply"
    }
}


