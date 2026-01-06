package com.humayapp.scout.feature.form.impl.data.registry.fielddata

import androidx.compose.ui.text.input.ImeAction
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import kotlinx.serialization.Serializable

@Serializable
sealed class FieldData : WizardEntry() {

    @Serializable
    data object FarmerInformation : FieldData() {
        override val title = "Farmer Information"
        override val description = "Basic personal info for quick identification"
        override val fields = listOf(
            field(key = FIRST_NAME_KEY, type = FieldType.NAME, label = "First Name", imeAction = ImeAction.Next),
            field(key = LAST_NAME_KEY, type = FieldType.NAME, label = "Last Name", imeAction = ImeAction.Done),
            field(
                key = GENDER_KEY,
                type = FieldType.CARD_RADIO,
                label = "Gender",
                options = listOf("Male", "Female", "Other")
            ),
        )

        override fun nextScreen(answers: Map<String, Any>) = PersonalDetails
    }

    @Serializable
    data object PersonalDetails : FieldData() {
        override val title = "Personal Details"
        override val description = "Additional personal info"
        override val fields = listOf(
            field(key = DATE_OF_BIRTH_KEY, type = FieldType.DATE, label = "Date of Birth"),
            field(
                key = CELLPHONE_NO_KEY,
                type = FieldType.NUM_PHONE,
                label = "Cellphone No.",
                imeAction = ImeAction.Done
            ),
        )

        override fun nextScreen(answers: Map<String, Any>) = FieldTiming
    }

    @Serializable
    data object FieldTiming : FieldData() {
        override val title = "Field Timing"
        override val description = "Important dates for crop planning"
        override val fields = listOf(
            field(
                key = LAND_PREPARATION_DATE_KEY,
                type = FieldType.DATE,
                label = "Land Preparation Start Date"
            ),
            field(
                key = EST_CROP_ESTABLISHMENT_KEY,
                type = FieldType.DATE,
                label = "Estimated Crop Establishment Date"
            ),
            field(
                key = EST_METHOD_OF_ESTABLISHMENT_KEY,
                type = FieldType.CARD_RADIO,
                label = "Estimated Method of Establishment",
                options = listOf("Direct-seeded", "Transplanted")
            ),
        )

        override fun nextScreen(answers: Map<String, Any>) = FieldArea
    }

    @Serializable
    data object FieldArea : FieldData() {
        override val title = "Field Area"
        override val description = "Size of the field"
        override val fields = listOf(
            field(
                key = TOTAL_FIELD_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Total Field Area (ha)",
                imeAction = ImeAction.Done
            ),
        )

        override fun nextScreen(answers: Map<String, Any>) = FieldCondition
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
                options = listOf("Clayey", "Loamy", "Sandy", "Silty", "Peaty")
            ),
            field(
                key = CURRENT_FIELD_CONDITION_KEY,
                type = FieldType.DROPDOWN,
                label = "Current Field Condition",
                options = listOf("Fallow", "Just Harvested", "Planted", "Land Preparation")
            ),
        )

        override fun nextScreen(answers: Map<String, Any>) = FieldLocation
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
                options = listOf("Aklan", "Antique", "Capiz", "Iloilo", "Negros Occidental", "Guimaras")
            ),
            field(
                key = MUNICIPALITY_OR_CITY_KEY,
                type = FieldType.DROPDOWN_SEARCHABLE,
                label = "Municipality or City"
            ),
            field(
                key = BARANGAY_KEY,
                type = FieldType.DROPDOWN_SEARCHABLE,
                label = "Barangay"
            ),
        )

        override fun nextScreen(answers: Map<String, Any>) = GpsCoordinates
    }

    @Serializable
    data object GpsCoordinates : FieldData() {
        override val title = "GPS Coordinates"
        override val description = "Exact location of the field"
        override val fields = listOf(
            field(key = COORDINATES_KEY, type = FieldType.GPS, label = "Field Location (GPS)")
        )
    }

    companion object {

        val pageOverrides: WizardPageOverrides = mapOf(
            GpsCoordinates to { page -> GpsCoordinatesPage(page as GpsCoordinates) }
        )

        val startEntry = FarmerInformation
        val entries = listOf(
            FarmerInformation, PersonalDetails, FieldTiming, FieldArea,
            FieldCondition, FieldLocation, GpsCoordinates
        )

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


