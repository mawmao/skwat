package com.humayapp.scout.feature.form.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.navigation.WizardNavKey
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.field
import kotlinx.serialization.Serializable

@Serializable
sealed class FieldData : WizardNavKey() {

    @Serializable
    data object FarmerInformation : FieldData() {
        override val title = "Farmer Information"
        override val description = "Basic personal info for quick identification"
        override val nextKey = PersonalDetails
        override val fields = listOf(
            field(key = FIRST_NAME_KEY, type = FieldType.NAME, label = "First Name"),
            field(key = LAST_NAME_KEY, type = FieldType.NAME, label = "Last Name"),
            field(key = GENDER_KEY, type = FieldType.CARD_RADIO, label = "Gender"),
        )
    }

    @Serializable
    data object PersonalDetails : FieldData() {
        override val title = "Personal Details"
        override val description = "Additional personal info"
        override val nextKey = FieldTiming
        override val fields = listOf(
            field(key = DATE_OF_BIRTH_KEY, type = FieldType.DATE, label = "Date of Birth"),
            field(key = CELLPHONE_NO_KEY, type = FieldType.NUM_PHONE, label = "Cellphone No."),
        )
    }

    @Serializable
    data object FieldTiming : FieldData() {
        override val title = "Field Timing"
        override val description = "Important dates for crop planning"
        override val nextKey = null
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
    }

    @Serializable
    data object FieldArea : FieldData() {
        override val title = "Field Area"
        override val description = "Size of the field"
        override val nextKey = FieldCondition
        override val fields = listOf(
            field(
                key = TOTAL_FIELD_AREA_KEY,
                type = FieldType.NUM_DECIMAL,
                label = "Total Field Area (ha)",
            ),
        )
    }

    @Serializable
    data object FieldCondition : FieldData() {
        override val title = "Field Condition"
        override val description = "Current state of the field"
        override val nextKey = FieldLocation
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
    }

    @Serializable
    data object FieldLocation : FieldData() {
        override val group = WizardGroupId("field")
        override val title = "Field Location"
        override val description = "Administrative info"
        override val nextKey = GpsCoordinates
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
    }

    @Serializable
    data object GpsCoordinates : FieldData() {
        override val group = WizardGroupId("field")
        override val title = "GPS Coordinates"
        override val description = "Exact location of the field"
        override val fields = listOf(
            field(key = COORDINATES_KEY, type = FieldType.GPS, label = "Field Location (GPS)")
        )
    }

    companion object {

        val pageOverrides: Map<WizardNavKey, @Composable (WizardNavKey) -> Unit> =
            mapOf( GpsCoordinates to { page -> GpsCoordinatesPage(page as GpsCoordinates) } )

        fun createFieldDataWizardMetadata(repeatCount: Int = 1) = wizardMetadata(
            startKey = FarmerInformation,
            keys = listOf(
                FarmerInformation,
                PersonalDetails,
                FieldTiming,
                FieldArea,
                FieldCondition,
                FieldLocation,
                GpsCoordinates
            ),
            repeatCount = repeatCount
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


@Composable
fun DefaultFieldDataEntry(key: WizardNavKey) {
    PagerEntry(key) { page ->
        page.fields.fastForEach {
            Text("Field: ${it.label}")
        }
    }
}

@Composable
fun GpsCoordinatesPage(page: FieldData.GpsCoordinates) {
    PagerEntry(page) {
        Text("Map goes here")
    }
}

@Composable
fun PagerEntry(key: WizardNavKey, content: @Composable ColumnScope.(WizardNavKey) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScoutTheme.margin),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column(
            modifier = Modifier.padding(top = ScoutTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.extraSmall)
        )
        {
            Text(text = key.title, style = ScoutTheme.material.typography.headlineMedium)
            Text(
                text = key.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(ScoutTheme.spacing.medium))
        content(key)
    }
}
