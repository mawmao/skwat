package com.humayapp.scout.feature.form.impl.data.registry.damage

import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.feature.form.impl.data.mapper.FormMapper
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.field
import io.github.jan.supabase.SupabaseClient

sealed class DamageAssessment : WizardEntry() {

    object CropStageAndSoilType : DamageAssessment() {
        override val title = "Crop Stage and Soil Type"
        override val description = "Record the crop growth stage and soil type at time of damage"
        override val fields = listOf(
            field(
                key = CROP_STAGE_KEY,
                type = FieldType.DROPDOWN,
                label = "Crop Stage",
                options = listOf("Seedling", "Vegetative", "Reproductive", "Maturity")
            ),
            field(
                key = SOIL_TYPE_KEY,
                type = FieldType.DROPDOWN,
                label = "Soil Type",
                options = listOf("Clayey", "Loamy", "Sandy", "Silty", "Peaty")
            )
        )

        override fun nextScreen(answers: Map<String, Any?>) = CauseOfDamage
    }

    object CauseOfDamage : DamageAssessment() {
        override val title = "Cause of Damage"
        override val description = "Identify the cause and observed pest or agent"
        override val fields = listOf(
            field(
                key = OBSERVED_PEST_KEY,
                type = FieldType.TEXT,
                label = "Observed Pest",
            ),
            field(
                key = CAUSE_OF_DAMAGE_KEY,
                type = FieldType.DROPDOWN,
                label = "Cause of Damage",
                options = listOf("Pest", "Disease", "Flood", "Drought", "Wind", "Other")
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = DamageImpact
    }

    object DamageImpact : DamageAssessment() {
        override val title = "Damage Impact"
        override val description = "Record severity level and affected area"
        override val fields = listOf(
            field(
                key = SEVERITY_KEY,
                type = FieldType.DROPDOWN,
                label = "Severity",
                options = listOf("Low", "Moderate", "High", "Severe")
            ),
            field(
                key = AFFECTED_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Affected Area (ha)",
                // TODO: validation should be less than total field area
            ),
        )
    }

    companion object {

        val startEntry = CropStageAndSoilType
        val entries = listOf(CropStageAndSoilType, CauseOfDamage, DamageImpact)

        val mapper: FormMapper = object : FormMapper() {
            override suspend fun upload(entry: FormEntryEntity, client: SupabaseClient) {
                defaultMapping(
                    table = SupabaseDBTables.DAMAGE_ASSESSMENTS,
                    entry = entry,
                    client = client,
                )
            }
        }

        const val CROP_STAGE_KEY = "crop_stage"
        const val SOIL_TYPE_KEY = "soil_type"
        const val OBSERVED_PEST_KEY = "observed_pest"
        const val CAUSE_OF_DAMAGE_KEY = "cause"
        const val SEVERITY_KEY = "severity"
        const val AFFECTED_AREA_KEY = "affected_area_ha"
    }
}
