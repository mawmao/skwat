package com.humayapp.scout.feature.form.impl.data.registry.monitoring

import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.network.SupabaseDBTables
import com.humayapp.scout.core.network.util.asJson
import com.humayapp.scout.feature.form.impl.data.mapper.FormMapper
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.overrides.ConditionPage
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import com.humayapp.scout.feature.form.impl.model.validateIf
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.json.JsonObject

sealed class MonitoringVisit : WizardEntry() {

    data object MonitoringDate : MonitoringVisit() {
        override val title = "Monitoring Date"
        override val description = "Record the date of the field monitoring visit"
        override val fields = listOf(
            field(
                key = MONITORING_DATE_KEY,
                type = FieldType.DATE,
                label = "Monitoring Date",

                // tbd
                validator = Validators.mustBeToday()
            )
        )

        override fun nextScreen(answers: Map<String, Any?>) = Conditions
    }

    data object Conditions : MonitoringVisit() {
        override val title = "Conditions"
        override val description = "Record crop status, soil moisture, and plant height"
        override val fields = listOf(
            field(
                key = CROP_STAGE_KEY,
                type = FieldType.DROPDOWN,
                label = "Crop Stage",
                options = listOf("Not Yet Planted", "Emerging", "Vegetative", "Flowering", "Harvest Ready"),
                validator = Validators.nonEmpty
            ),
            field(
                key = SOIL_MOISTURE_STATUS_KEY,
                type = FieldType.DROPDOWN,
                label = "Soil Moisture Status",
                options = listOf("Dry", "Moist", "Wet", "Flooded"),
                validator = Validators.nonEmpty
            ),
            field(
                key = AVG_PLANT_HEIGHT_KEY,
                type = FieldType.NUM_DECIMAL_OR_NA,
                label = "Average Plant Height",

                // check first if `crop-stage` != to "Not Yet Planted"
                validator = validateIf(
                    condition = { data ->
                        val cropStage = data[CROP_STAGE_KEY] as? String
                        cropStage != null && cropStage != "Not Yet Planted"
                    },
                    validator = Validators.floatRange(min = 0.0f, unit = "cm") { min, _, unit ->
                        "Average plant height should be at least $min $unit"
                    }
                )

            )
        )
    }

    companion object {

        fun serialize(answers: Map<String, Any?>): JsonObject = answers.asJson()

        val pageOverrides: WizardPageOverrides = mapOf(
            Conditions to { page -> ConditionPage(page as Conditions) }
        )

        val startEntry = MonitoringDate
        val entries = listOf(MonitoringDate, Conditions)

        val mapper: FormMapper = object : FormMapper() {
            override suspend fun upload(entry: FormEntryEntity, client: SupabaseClient) {
                defaultMapping(
                    table = SupabaseDBTables.MONITORING_VISITS,
                    entry = entry,
                    client = client,
                )
            }
        }

        const val MONITORING_DATE_KEY = "date_monitored"
        const val CROP_STAGE_KEY = "crop_stage"
        const val SOIL_MOISTURE_STATUS_KEY = "soil_moisture_status"
        const val AVG_PLANT_HEIGHT_KEY = "avg_plant_height"
    }
}

