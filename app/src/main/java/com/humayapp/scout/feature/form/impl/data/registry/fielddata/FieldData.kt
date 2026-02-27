package com.humayapp.scout.feature.form.impl.data.registry.fielddata


import android.util.Log
import androidx.compose.ui.text.input.ImeAction
import com.humayapp.scout.core.network.util.asJson
import com.humayapp.scout.core.network.util.transformField
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.mapper.FieldDataMapper
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.FieldLocationPage
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.GpsCoordinatesPage
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ImagesPage
import com.humayapp.scout.feature.form.impl.data.repository.toGeometry
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.Period

@Serializable
sealed class FieldData : WizardEntry() {

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
                validator = Validators.floatRange(min = 0.04f, unit = "ha") { min, _, unit ->
                    "Field area must be at least $min $unit"
                }
            ),
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
                type = FieldType.DROPDOWN_SEARCHABLE,
                label = "Province",
                options = listOf("Aklan", "Antique", "Capiz", "Iloilo", "Negros Occidental", "Guimaras"),
                validator = Validators.nonEmpty,
            ),
            field(
                key = MUNICIPALITY_OR_CITY_KEY,
                type = FieldType.DROPDOWN_SEARCHABLE,
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

        override fun nextScreen(answers: Map<String, Any?>) = Images
    }

    // todo: validation
    @Serializable
    data object Images : FieldData() {
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
            Log.d("Scout: FieldData.Images", "allValid = $allValid")
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
        fun serialize(answers: Map<String, Any?>): JsonObject = answers.asJson(
            includeKey = { !it.startsWith("img_") },
            rules = listOf(
                transformField(GENDER_KEY) {
                    when (it) {
                        "Male" -> "male"
                        "Female" -> "female"
                        else -> it
                    }
                },
                transformField(COORDINATES_KEY) { it.toGeometry() }
            )
        )

        val pageOverrides: WizardPageOverrides = mapOf(
            FieldLocation to { page -> FieldLocationPage(page as FieldLocation) },
            GpsCoordinates to { page -> GpsCoordinatesPage(page as GpsCoordinates) },
            Images to { page -> ImagesPage(page as Images) }
        )

        val startEntry = FarmerInformation
        val entries = listOf(
            FarmerInformation, PersonalDetails, FieldTiming, FieldArea,
            FieldCondition, FieldLocation, GpsCoordinates, Images,
        )

        val mapper = FieldDataMapper

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
        const val IMG1_KEY = "img_1"
        const val IMG2_KEY = "img_2"
        const val IMG3_KEY = "img_3"
        const val IMG4_KEY = "img_4"
        const val IMG5_KEY = "img_5"
    }
}


