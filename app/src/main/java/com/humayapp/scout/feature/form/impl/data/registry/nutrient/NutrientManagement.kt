package com.humayapp.scout.feature.form.impl.data.registry.nutrient

import com.humayapp.scout.feature.form.impl.data.registry.nutrient.mapper.NutrientManagementMapper
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.overrides.FertilizerApplicationPage
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field

sealed class NutrientManagement : WizardEntry() {

    data object FertilizedArea : NutrientManagement() {
        override val title = "Fertilized Area"
        override val description = "Track fertilized area and related information"
        override val fields = listOf(
            field(
                key = APPLIED_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Fertilized Area (sqm)",
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = FertilizerApplication
    }

    data object FertilizerApplication : NutrientManagement() {
        override val title = "Fertilizer Application"
        override val description = "Records fertilizer use"
        override val fields = emptyList<WizardField>()
    }

    companion object {

        data class FertilizerApplicationItem(
            var fertilizerType: String = "",
            var brand: String = "",
            var nitrogen: String = "",
            var phosphorus: String = "",
            var potassium: String = "",
            var amount: String = "",
            var amountUnit: String = "",
            var cropStage: String = ""
        )

        val pageOverrides: WizardPageOverrides = mapOf(
            FertilizerApplication to { page -> FertilizerApplicationPage(page as FertilizerApplication) }
        )

        val startEntry = FertilizedArea
        val entries = listOf(
            FertilizedArea, FertilizerApplication
        )

        val mapper = NutrientManagementMapper

        const val APPLIED_AREA_KEY = "applied_area_sqm"


        fun fertilizerApplicationFields(index: Int): List<WizardField> = listOf(
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
                validator = Validators.nonEmpty
            ),
            field(
                key = "${NITROGEN_CONTENT_KEY}_$index",
                type = FieldType.NUM_PERCENT,
                label = "N %",
                validator = Validators.nonEmpty
            ),
            field(
                key = "${PHOSPHORUS_CONTENT_KEY}_$index",
                type = FieldType.NUM_PERCENT,
                label = "P %",
                validator = Validators.nonEmpty
            ),
            field(
                key = "${POTASSIUM_CONTENT_KEY}_$index",
                type = FieldType.NUM_PERCENT,
                label = "K %",
                validator = Validators.nonEmpty
            ),
            field(
                key = "${AMOUNT_APPLIED_KEY}_$index",
                type = FieldType.NUM_DECIMAL,
                label = "Amount Applied",
                validator = Validators.nonEmpty
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

        // fertilizer application fields (one or more)
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



