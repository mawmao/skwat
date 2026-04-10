package com.humayapp.scout.feature.form.impl.data.registry.monitoring

import android.util.Log
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.field
import com.humayapp.scout.feature.form.impl.model.validateIf
import kotlinx.serialization.Serializable

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

        override fun nextScreen(answers: Map<String, Any?>) = Images
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
                label = "Average Plant Height (by centimeters)",

                // check first if `crop-stage` != to "Not Yet Planted"
                validator = validateIf(
                    condition = { data ->
                        val cropStage = data[CROP_STAGE_KEY] as? String
                        cropStage != null && cropStage != "Not Yet Planted"
                    },
                    validator = Validators.floatRange(min = 1f, max = 200.0f, unit = "cm") { min, max, unit ->
                        "Average plant height should be between $min to $max $unit"
                    }
                )

            )
        )
    }


    @Serializable
    data object Images : MonitoringVisit() {
        override val title = "Field & Crop Images"
        override val description = "Document the field and rice crop from multiple angles"
        override val fields = listOf(
            field(key = IMG1_KEY, type = FieldType.IMAGE, label = "Front View", validator = Validators.image),
            field(key = IMG2_KEY, type = FieldType.IMAGE, label = "Right View", validator = Validators.image),
            field(key = IMG3_KEY, type = FieldType.IMAGE, label = "Left View", validator = Validators.image),
            field(key = IMG4_KEY, type = FieldType.IMAGE, label = "Back view", validator = Validators.image),
            field(key = IMG5_KEY, type = FieldType.IMAGE, label = "Close-up", validator = Validators.optionalImage),
        )

        override val nextRule: (FormState) -> Boolean = { state ->
            val allValid = state.validatePage(this)
            Log.d("Scout: MonitoringVisit.Images", "allValid = $allValid")
            if (!allValid) {
                state.setDialog(
                    FormState.Dialog(
                        title = "Incomplete Images",
                        message = "Front, right, left, and back view are required"
                    )
                )
            }
            allValid
        }
    }

    companion object {

        val entries = listOf(MonitoringDate, Conditions, Images)

        const val MONITORING_DATE_KEY = "date_monitored"
        const val CROP_STAGE_KEY = "crop_stage"
        const val SOIL_MOISTURE_STATUS_KEY = "soil_moisture_status"
        const val AVG_PLANT_HEIGHT_KEY = "avg_plant_height"

        const val IMG1_KEY = "img_1"
        const val IMG2_KEY = "img_2"
        const val IMG3_KEY = "img_3"
        const val IMG4_KEY = "img_4"
        const val IMG5_KEY = "img_5"
    }
}

