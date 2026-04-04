package com.humayapp.scout.feature.form.impl.ui.screens.detail


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.component.ImageBox
import com.humayapp.scout.core.ui.component.ScoutAlertDialog
import com.humayapp.scout.core.ui.component.ScoutLabel
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.model.CulturalManagementForm
import com.humayapp.scout.feature.form.impl.model.DamageAssessmentForm
import com.humayapp.scout.feature.form.impl.model.FieldActivityDetails
import com.humayapp.scout.feature.form.impl.model.FieldDataForm
import com.humayapp.scout.feature.form.impl.model.FormData
import com.humayapp.scout.feature.form.impl.model.Monitorable
import com.humayapp.scout.feature.form.impl.model.MonitoringVisit
import com.humayapp.scout.feature.form.impl.model.NutrientManagementForm
import com.humayapp.scout.feature.form.impl.model.ProductionForm
import com.humayapp.scout.feature.form.impl.ui.components.FormImagesLayout
import com.humayapp.scout.feature.form.impl.ui.components.FormReviewItem
import com.humayapp.scout.navigation.navigateToForms


@Composable
fun FormDetailsScreen(
    collectionTaskId: Int,
    activityId: Int?,
    onBack: () -> Unit,
) {
    val viewModel: FormDetailsViewModel = hiltViewModel(
        creationCallback = { factory: FormDetailsViewModel.Factory ->
            factory.create(collectionTaskId, activityId)
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rootNavigator = LocalRootStackNavigator.current

    var showCannotRetakeDialog by remember { mutableStateOf(false) }
    var showPendingApprovalDialog by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is FormDetailsUiState.Loading -> Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        is FormDetailsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${state.message}")
        }

        is FormDetailsUiState.Success -> {
            FormDetailsContent(
                rawDetails = state.rawDetails,
                formData = state.formData,
                onBack = onBack,
                onRetakeClick = {
                    if (state.retakeAvailable && state.originalTask != null) {
                        rootNavigator.navigateToForms(state.originalTask)
                    } else if (state.retakePending) {
                        showPendingApprovalDialog = true
                    } else {
                        showCannotRetakeDialog = true
                    }
                }
            )
        }
    }

    ScoutAlertDialog(
        isVisible = showCannotRetakeDialog,
        title = "Retake Not Available",
        message = "The manager hasn't scheduled a retake for this form yet. Please wait.",
        onDismissRequest = { showCannotRetakeDialog = false },
        confirmButtonText = "OK",
        onConfirm = { showCannotRetakeDialog = false }
    )

    ScoutAlertDialog(
        isVisible = showPendingApprovalDialog,
        title = "Retake Pending Approval",
        message = "A retake has been submitted and is waiting for approval. No further action needed.",
        onDismissRequest = { showPendingApprovalDialog = false },
        confirmButtonText = "OK",
        onConfirm = { showPendingApprovalDialog = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDetailsContent(
    rawDetails: FieldActivityDetails,
    formData: FormData,
    onRetakeClick: () -> Unit,
    onBack: () -> Unit,
) {
    val monitoringVisit = (formData as? Monitorable)?.monitoringVisit
    val activityTypeLabel = when (rawDetails.activityType) {
        "field-data" -> "Field Data"
        "cultural-management" -> "Cultural Management"
        "nutrient-management" -> "Nutrient Management"
        "production" -> "Production"
        "damage-assessment" -> "Damage Assessment"
        else -> rawDetails.activityType
    }

    val statusText = when (rawDetails.verificationStatus) {
        "approved" -> "Approved"
        "rejected" -> "Rejected"
        else -> "Pending Approval"   // verificationStatus == "pending" and form is submitted
    }
    val statusColor = when (rawDetails.verificationStatus) {
        "approved" -> Color(0xFF4CAF50)  // green
        "rejected" -> Color(0xFFF44336)  // red
        else -> Color(0xFFFFC107)        // amber
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(activityTypeLabel) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(ScoutIcons.Back), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = statusColor.copy(alpha = 0.2f),
                        contentColor = statusColor,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (rawDetails.verificationStatus == "rejected") {
                FloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                    modifier = Modifier.padding(ScoutTheme.spacing.mediumLarge),
                    onClick = onRetakeClick
                ) {
                    Text(text = "Retake Form", modifier = Modifier.padding(horizontal = ScoutTheme.spacing.medium))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ScoutTheme.margin),
            verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.small)

        ) {
            rawDetails.collectedBy?.let { user ->
                item {
                    FormReviewItem(label = "Collected by", value = user.name ?: user.id)
                }
            }
            rawDetails.collectedAt?.let { time ->
                item {
                    FormReviewItem(label = "Collected at", value = time.toString())
                }
            }
            if (rawDetails.verificationStatus != "pending") {
                item {
                    FormReviewItem(label = "Verification status", value = rawDetails.verificationStatus)
                }
                rawDetails.verifiedBy?.let { user ->
                    item {
                        FormReviewItem(label = "Verified by", value = user.name ?: user.id)
                    }
                }
                rawDetails.verifiedAt?.let { time ->
                    item {
                        FormReviewItem(label = "Verified at", value = time.toString())
                    }
                }
            }
            rawDetails.remarks?.let {
                item {
                    FormReviewItem(label = "Remarks", value = it)
                }
            }

            val fields = formData.toFieldList()
            items(fields) { (label, value) ->
                FormReviewItem(label = label, value = value)
            }

            monitoringVisit?.toFieldList()?.forEach { (label, value) ->
                item {
                    FormReviewItem(label = label, value = value)
                }
            }

            if (rawDetails.imageUrls.isNotEmpty()) {
                item {
                    Text(
                        "Images",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    FormImagesLayout(
                        items = rawDetails.imageUrls.mapIndexed { index, url ->
                            object {
                                val key = "img_${index + 1}"
                                val label =
                                    listOf("Front View", "Right View", "Left View", "Back View", "Close up")[index % 5]
                                val value = url
                            }
                        },
                    ) { item, aspectRatio, modifier ->
                        Log.d("Scout: FormDetailScreen", "Item = ${item.value.toUri()}")
                        Column(modifier = modifier) {
                            ScoutLabel(label = item.label, enableHorizontalPadding = false)
                            ImageBox(
                                uri = item.value.toUri(),
                                modifier = Modifier,
                                aspectRatio = aspectRatio
                            )
                        }
                    }
                }
            }
        }
    }
}

fun FormData.toFieldList(): List<Pair<String, String>> = when (this) {
    is FieldDataForm -> toFieldList()
    is CulturalManagementForm -> toFieldList()
    is NutrientManagementForm -> toFieldList()
    is ProductionForm -> toFieldList()
    is DamageAssessmentForm -> toFieldList()
}

fun FieldDataForm.toFieldList(): List<Pair<String, String>> = listOf(
    "Land Preparation Start Date" to (landPreparationStartDate ?: "No data"),
    "Est. Crop Establishment Date" to (estCropEstablishmentDate ?: "No data"),
    "Est. Method" to (estCropEstablishmentMethod ?: "No data"),
    "Total Field Area (ha)" to totalFieldAreaHa.toString(),
    "Soil Type" to (soilType ?: "No data"),
    "Current Field Condition" to (currentFieldCondition ?: "No data"),
)

fun CulturalManagementForm.toFieldList(): List<Pair<String, String>> = buildList {
    add("Ecosystem" to (ecosystem ?: "No data"))
    add("Monitoring Field Area (sqm)" to monitoringFieldAreaSqm.toString())
    add("Actual Crop Establishment Date" to actualCropEstablishmentDate.toString())
    add("Actual Method" to (actualCropEstablishmentMethod ?: "No data"))
    add("Sowing Date" to (sowingDate?.toString() ?: "No data"))
    add("Seedling Age (days)" to (seedlingAgeAtTransplanting?.toString() ?: "No data"))
    add(
        "Distance Between Rows" to listOfNotNull(
            distanceBetweenPlantRow1, distanceBetweenPlantRow2, distanceBetweenPlantRow3
        ).joinToString(", ").ifEmpty { "No data" })
    add(
        "Distance Within Rows" to listOfNotNull(
            distanceWithinPlantRow1, distanceWithinPlantRow2, distanceWithinPlantRow3
        ).joinToString(", ").ifEmpty { "No data" })
    add("Seeding Rate (kg/ha)" to (seedingRateKgHa?.toString() ?: "No data"))
    add("Direct Seeding Method" to (directSeedingMethod ?: "No data"))
    add("Number of Plants #1" to (numPlants1?.toString() ?: "No data"))
    add("Number of Plants #2" to (numPlants2?.toString() ?: "No data"))
    add("Number of Plants #3" to (numPlants3?.toString() ?: "No data"))
    add("Rice Variety" to (riceVariety ?: "No data"))
    add("Rice Variety No." to (riceVarietyNo ?: "No data"))
    add("Maturity Duration (days)" to riceVarietyMaturityDuration.toString())
    add("Seed Class" to seedClass)
}

fun NutrientManagementForm.toFieldList(): List<Pair<String, String>> = buildList {
    add("Applied Area (sqm)" to appliedAreaSqm.toString())
    if (applications.isEmpty()) {
        add("Fertilizer Applications" to "No data")
    } else {
        applications.forEachIndexed { idx, app ->
            add("Fertilizer ${idx + 1} - Type" to (app.fertilizerType ?: "No data"))
            add("  Brand" to (app.brand ?: "No data"))
            add("  N (%)" to app.nitrogenContentPct.toString())
            add("  P (%)" to app.phosphorusContentPct.toString())
            add("  K (%)" to app.potassiumContentPct.toString())
            add("  Amount" to "${app.amountApplied} ${app.amountUnit ?: ""}")
            add("  Crop Stage" to (app.cropStageOnApplication ?: "No data"))
        }
    }
}

fun ProductionForm.toFieldList(): List<Pair<String, String>> = listOf(
    "Harvest Date" to (harvestDate.toString() ?: "No data"),
    "Harvesting Method" to (harvestingMethod ?: "No data"),
    "Bags Harvested" to bagsHarvested.toString(),
    "Avg Bag Weight (kg)" to avgBagWeightKg.toString(),
    "Area Harvested (ha)" to areaHarvestedHa.toString(),
    "Irrigation Supply" to (irrigationSupply ?: "No data"),
)

fun DamageAssessmentForm.toFieldList(): List<Pair<String, String>> = listOf(
    "Cause" to (cause ?: "No data"),
    "Crop Stage" to (cropStage ?: "No data"),
    "Soil Type" to (soilType ?: "No data"),
    "Severity" to (severity ?: "No data"),
    "Affected Area (ha)" to affectedAreaHa.toString(),
    "Observed Pest" to (observedPest ?: "No data"),
)

fun MonitoringVisit.toFieldList(): List<Pair<String, String>> = listOf(
    "Date Monitored" to (dateMonitored ?: "No data"),
    "Crop Stage" to (cropStage ?: "No data"),
    "Soil Moisture Status" to (soilMoistureStatus ?: "No data"),
    "Avg Plant Height (cm)" to (avgPlantHeight?.toString() ?: "No data"),
)
