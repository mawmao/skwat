package com.humayapp.scout.feature.form.impl.data.registry.nutrient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.model.field
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry

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

        override fun nextScreen(answers: Map<String, Any?>): WizardEntry? = FertilizerApplication
    }

    data object FertilizerApplication : NutrientManagement() {
        override val title = "Fertilizer Application"
        override val description = "Description: TODO"
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

        const val APPLIED_AREA_KEY = "applied_area_sqm"

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

@Composable
fun rememberFertilizerApplications(): MutableList<NutrientManagement.Companion.FertilizerApplicationItem> {
    return remember { mutableStateListOf() }
}


// has a button to add a fertilizer application entry
// should be a lazy list where i can add 'fertilizer application'
// each entry should be a card and can be clicked to open a dialog to see the details
@Composable
fun FertilizerApplicationPage(
    page: NutrientManagement.FertilizerApplication,
) {
    val applications = rememberFertilizerApplications()
    var selectedApplication by remember { mutableStateOf<NutrientManagement.Companion.FertilizerApplicationItem?>(null) }

    WizardEntry(page) {
        Column {
            LazyColumn {
                itemsIndexed(applications) { index, app ->
                    FertilizerApplicationCard(
                        application = app,
                        onClick = { selectedApplication = app }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add button
            Button(onClick = {
                applications.add(NutrientManagement.Companion.FertilizerApplicationItem())
            }) {
                Text("Add Fertilizer Application")
            }
        }

        // Dialog for editing details
        selectedApplication?.let { app ->
            FertilizerApplicationDialog(
                application = app,
                onDismiss = { selectedApplication = null }
            )
        }
    }
}


@Composable
fun FertilizerApplicationCard(
    application: NutrientManagement.Companion.FertilizerApplicationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Fertilizer: ${application.fertilizerType}")
            Text("Brand: ${application.brand}")
            Text("Amount: ${application.amount} ${application.amountUnit}")
        }
    }
}

@Composable
fun FertilizerApplicationDialog(
    application: NutrientManagement.Companion.FertilizerApplicationItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {

                TextField(
                    value = application.fertilizerType,
                    onValueChange = { application.fertilizerType = it },
                    label = { Text("Fertilizer Type") }
                )

                TextField(
                    value = application.brand,
                    onValueChange = { application.brand = it },
                    label = { Text("Brand") }
                )

                TextField(
                    value = application.nitrogen,
                    onValueChange = { application.nitrogen = it },
                    label = { Text("Nitrogen %") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        }
    }
}
