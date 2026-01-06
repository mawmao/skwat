package com.humayapp.scout.feature.form.impl.data.registry.fielddata

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry

@Composable
fun GpsCoordinatesPage(page: FieldData.GpsCoordinates) {
    WizardEntry(page) {
        Text("Map goes here")
    }
}

