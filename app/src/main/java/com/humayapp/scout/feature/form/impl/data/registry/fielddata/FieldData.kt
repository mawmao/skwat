package com.humayapp.scout.feature.form.impl.data.registry.fielddata


import androidx.compose.ui.text.input.ImeAction
import com.humayapp.scout.core.network.util.asJson
import com.humayapp.scout.core.network.util.transformField
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ConfirmFarmerPage
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.FieldLocationPage
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.GpsCoordinatesPage
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ImagesPage
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.overrides.ConditionPage
import com.humayapp.scout.feature.form.impl.data.repository.toGeometry
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import com.humayapp.scout.feature.form.impl.model.fieldThresholdRule
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Period

@Serializable
sealed class FieldData : WizardEntry() {

    @Serializable
    data object ConfirmFarmer : FieldData() {
        override val title = "Confirm Farmer"
        override val description = "Verify farmer information"
        override val fields = listOf(
            field(
                key = "confirm_farmer",
                type = FieldType.CARD_RADIO,
                label = "Is this the correct farmer?",
                options = listOf("Yes", "No"),
                validator = Validators.nonEmpty
            )
        )

        override fun nextScreen(answers: Map<String, Any?>): WizardEntry {
            return if (answers["confirm_farmer"] == "Yes") {
                FieldTiming
            } else {
                FarmerInformation
            }
        }
    }

    @Serializable
    data object FarmerInformation : FieldData() {
        override val title = "Farmer Information"
        override val description = "Basic personal info for quick identification"
        override val fields = listOf(
            field(
                key = FIRST_NAME_KEY,
                type = FieldType.NAME,
                label = "First Name",
                imeAction = ImeAction.Next,
                validator = Validators.name
            ),
            field(
                key = LAST_NAME_KEY,
                type = FieldType.NAME,
                label = "Last Name",
                imeAction = ImeAction.Done,
                validator = Validators.name
            ),
            field(
                key = GENDER_KEY,
                type = FieldType.CARD_RADIO,
                label = "Gender",
                options = listOf("Male", "Female", "Other"),
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = PersonalDetails
    }

    @Serializable
    data object PersonalDetails : FieldData() {
        override val title = "Personal Details"
        override val description = "Additional personal info"
        override val fields = listOf(
            field(
                key = DATE_OF_BIRTH_KEY,
                type = FieldType.DATE,
                label = "Date of Birth",
                validator = Validators.dateOfBirth(16, 80)
            ),
            field(
                key = CELLPHONE_NO_KEY,
                type = FieldType.NUM_PHONE,
                label = "Cellphone No.",
                imeAction = ImeAction.Done,
                validator = Validators.phone
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = FieldTiming
    }

    @Serializable
    data object FieldTiming : FieldData() {
        override val title = "Field Timing"
        override val description = "Important dates for crop planning"
        override val fields = listOf(
            field(
                key = LAND_PREPARATION_DATE_KEY,
                type = FieldType.DATE,
                label = "Land Preparation Start Date",
                validator = Validators.withinRange(Period.ofDays(30))
            ),
            field(
                key = EST_CROP_ESTABLISHMENT_KEY,
                type = FieldType.DATE,
                label = "Estimated Crop Establishment Date",
                validator = Validators.isAfterBy(otherKey = LAND_PREPARATION_DATE_KEY, after = Period.ofMonths(3))
            ),
            field(
                key = EST_METHOD_OF_ESTABLISHMENT_KEY,
                type = FieldType.CARD_RADIO,
                label = "Estimated Method of Establishment",
                options = listOf("Direct-seeded", "Transplanted"),
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = FieldArea
    }

    @Serializable
    data object FieldArea : FieldData() {
        override val title = "Field Area"
        override val description = "Size of the field"
        override val fields = listOf(
            field(
                key = TOTAL_FIELD_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Total Field Area (by hectares)",
                imeAction = ImeAction.Done,
                validator = Validators.floatRange(min = 0.04f, max = 999.0f, unit = "ha") { min, max, unit ->
                    "Field area must be between $min to $max $unit"
                }
            ),
        )

        override val nextRule = fieldThresholdRule(
            key = TOTAL_FIELD_AREA_KEY,
            threshold = 20f,
            message = { "Field area is $it ha, which exceeds 20 ha. Press OK to proceed." }
        )

        override fun nextScreen(answers: Map<String, Any?>) = FieldCondition
    }

    @Serializable
    data object FieldCondition : FieldData() {
        override val title = "Field Condition"
        override val description = "Current state of the field"
        override val fields = listOf(
            field(
                key = SOIL_TYPE_KEY,
                type = FieldType.DROPDOWN,
                label = "Soil Type",
                options = listOf("Clayey", "Loamy", "Sandy", "Silty", "Peaty"),
                validator = Validators.nonEmpty
            ),
            field(
                key = CURRENT_FIELD_CONDITION_KEY,
                type = FieldType.DROPDOWN,
                label = "Current Field Condition",
                options = listOf("Fallow", "Just Harvested", "Planted", "Land Preparation"),
                validator = Validators.nonEmpty
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = FieldLocation
    }

    @Serializable
    data object FieldLocation : FieldData() {
        override val title = "Field Location"
        override val description = "Administrative info"
        override val fields = listOf(
            field(
                key = PROVINCE_KEY,
                type = FieldType.GPS,
                label = "Province",
                options = listOf("Aklan", "Antique", "Capiz", "Iloilo", "Negros Occidental", "Guimaras"),
                validator = Validators.nonEmpty,
            ),
            field(
                key = MUNICIPALITY_OR_CITY_KEY,
                type = FieldType.GPS,
                label = "Municipality or City",
                validator = Validators.nonEmpty,
            ),
            field(
                key = BARANGAY_KEY,
                type = FieldType.DROPDOWN_SEARCHABLE,
                label = "Barangay",
                validator = Validators.nonEmpty,
            ),
        )

        override fun nextScreen(answers: Map<String, Any?>) = GpsCoordinates
    }

    @Serializable
    data object GpsCoordinates : FieldData() {
        override val title = "GPS Coordinates"
        override val description = "Exact location of the field"
        override val fields = listOf(
            field(
                key = COORDINATES_KEY,
                type = FieldType.GPS,
                label = "Field Location (GPS)",
                validator = Validators.nonEmpty,
            )
        )

        override fun nextScreen(answers: Map<String, Any?>) = MonitoringVisit.MonitoringDate
    }

    companion object {
        fun serialize(answers: Map<String, Any?>): JsonObject = answers.asJson(
            includeKey = { key -> !key.startsWith("img_") && key != "season_id" && key != "confirm_farmer" },
            rules = listOf(
                transformField(GENDER_KEY) {
                    when (it) {
                        "Male" -> "male"
                        "Female" -> "female"
                        "Other" -> "other"
                        else -> it
                    }
                },
                transformField(COORDINATES_KEY) { it.toGeometry() }
            )
        )

        val pageOverrides: WizardPageOverrides = mapOf(
            ConfirmFarmer to { page -> ConfirmFarmerPage(page as ConfirmFarmer) },
            FieldLocation to { page -> FieldLocationPage(page as FieldLocation) },
            GpsCoordinates to { page -> GpsCoordinatesPage(page as GpsCoordinates) },
            MonitoringVisit.Conditions to { page -> ConditionPage(page as MonitoringVisit.Conditions) },
            MonitoringVisit.Images to { page -> ImagesPage(page as MonitoringVisit.Images) }
        )

        val startEntry = FarmerInformation
        val entries = listOf(
            ConfirmFarmer,
            FarmerInformation, PersonalDetails, FieldTiming, FieldArea,
            FieldCondition, FieldLocation, GpsCoordinates
        ) + listOf(
            MonitoringVisit.MonitoringDate,
            MonitoringVisit.Conditions,
            MonitoringVisit.Images
        )

        fun fieldDataJsonToAnswers(
            json: JsonElement,
            imageUrls: List<String>? = emptyList(),
        ): Map<String, Any?> {
            val obj = json.jsonObject
            val answers = mutableMapOf<String, Any?>()

            obj["first_name"]?.let { answers[FIRST_NAME_KEY] = it.jsonPrimitive.content }
            obj["last_name"]?.let { answers[LAST_NAME_KEY] = it.jsonPrimitive.content }
            obj["gender"]?.let { answers[GENDER_KEY] = it.jsonPrimitive.content }
            obj["date_of_birth"]?.let { answers[DATE_OF_BIRTH_KEY] = it.jsonPrimitive.content }
            obj["cellphone_no"]?.let { answers[CELLPHONE_NO_KEY] = it.jsonPrimitive.content }
            obj["land_preparation_start_date"]?.let { answers[LAND_PREPARATION_DATE_KEY] = it.jsonPrimitive.content }
            obj["est_crop_establishment_date"]?.let { answers[EST_CROP_ESTABLISHMENT_KEY] = it.jsonPrimitive.content }
            obj["est_crop_establishment_method"]?.let {
                answers[EST_METHOD_OF_ESTABLISHMENT_KEY] = it.jsonPrimitive.content
            }
            obj["total_field_area_ha"]?.let {
                answers[TOTAL_FIELD_AREA_KEY] = it.jsonPrimitive.content.toDoubleOrNull()
            }
            obj["soil_type"]?.let { answers[SOIL_TYPE_KEY] = it.jsonPrimitive.content }
            obj["current_field_condition"]?.let { answers[CURRENT_FIELD_CONDITION_KEY] = it.jsonPrimitive.content }
            obj["province"]?.let { answers[PROVINCE_KEY] = it.jsonPrimitive.content }
            obj["municipality_or_city"]?.let { answers[MUNICIPALITY_OR_CITY_KEY] = it.jsonPrimitive.content }
            obj["barangay"]?.let { answers[BARANGAY_KEY] = it.jsonPrimitive.content }
            obj["location"]?.let { location -> answers[COORDINATES_KEY] = location.toString() }

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

        const val FIRST_NAME_KEY = "first_name"
        const val LAST_NAME_KEY = "last_name"
        const val GENDER_KEY = "gender"
        const val DATE_OF_BIRTH_KEY = "date_of_birth"
        const val CELLPHONE_NO_KEY = "cellphone_no"
        const val LAND_PREPARATION_DATE_KEY = "land_preparation_start_date"
        const val EST_CROP_ESTABLISHMENT_KEY = "est_crop_establishment_date"
        const val EST_METHOD_OF_ESTABLISHMENT_KEY = "est_crop_establishment_method"
        const val TOTAL_FIELD_AREA_KEY = "total_field_area_ha"
        const val SOIL_TYPE_KEY = "soil_type"
        const val CURRENT_FIELD_CONDITION_KEY = "current_field_condition"
        const val PROVINCE_KEY = "province"
        const val MUNICIPALITY_OR_CITY_KEY = "municipality_or_city"
        const val BARANGAY_KEY = "barangay"
        const val COORDINATES_KEY = "location"
    }
}


