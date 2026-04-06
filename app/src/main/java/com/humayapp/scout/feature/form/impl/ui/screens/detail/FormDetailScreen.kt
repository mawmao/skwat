package com.humayapp.scout.feature.form.impl.ui.screens.detail


import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.humayapp.scout.core.ui.component.ScoutIconButton
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


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

    val refreshError by viewModel.refreshError.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(refreshError) {
        refreshError?.let {
//            viewModel.clearRefreshError()
        }
    }

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
                onRefresh = {},
                isRefreshing = isRefreshing,
                onRetakeClick = {
                    if (state.retakeAvailable && state.originalTask != null) {
                        rootNavigator.navigateToForms(state.originalTask.id)
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
    formData: JsonElement,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    onRetakeClick: () -> Unit,
    onBack: () -> Unit,
) {
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
                title = { Text(activityTypeLabel, style = ScoutTheme.material.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(ScoutIcons.Back), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    if (isRefreshing) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(400, easing = LinearEasing)
                            )
                        )
                        ScoutIconButton(
                            onClick = { },
                            icon = ScoutIcons.Sync,
                            contentDescription = "Refreshing",
                            enabled = false,
                            modifier = Modifier.rotate(rotation)
                        )
                    } else {
                        ScoutIconButton(
                            onClick = onRefresh,
                            icon = ScoutIcons.Sync,
                            contentDescription = "Refresh Icon Button"
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
                    FormReviewItem(label = "Verification status", value = rawDetails.verificationStatus ?: "null")
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

            val fields = jsonElementToDisplayList(formData)
            Log.d("Scout: Details", "fields: $fields")
            items(fields) { (label, value) ->
                FormReviewItem(label = label, value = value)
            }

            val monitoringVisitJson = (formData as? JsonObject)?.get("monitoring_visit")?.jsonObject
            if (monitoringVisitJson != null) {
                item {
                    Text(
                        "Monitoring Visit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                val monitoringFields = jsonElementToDisplayList(monitoringVisitJson)
                monitoringFields.forEach { (label, value) ->
                    item {
                        FormReviewItem(label = label, value = value)
                    }
                }
            }

            val fertilizerApplicationJson = (formData as? JsonObject)?.get("fertilizer_application")?.jsonObject
            if (fertilizerApplicationJson != null) {
                item {
                    Text(
                        "FertilizerApplication",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                val fertAppFields = jsonElementToDisplayList(fertilizerApplicationJson)
                fertAppFields.forEach { (label, value) ->
                    item {
                        FormReviewItem(label = label, value = value)
                    }
                }
            }


            if (!rawDetails.imageUrls.isNullOrEmpty()) {
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


fun getReadableLabel(key: String): String {
    return when (key) {
        "land_preparation_start_date" -> "Land Preparation Start Date"
        "est_crop_establishment_date" -> "Est. Crop Establishment Date"
        "est_crop_establishment_method" -> "Est. Method"
        "total_field_area_ha" -> "Total Field Area (ha)"
        "soil_type" -> "Soil Type"
        "current_field_condition" -> "Current Field Condition"
        "ecosystem" -> "Ecosystem"
        "monitoring_field_area_sqm" -> "Monitoring Field Area (sqm)"
        "actual_crop_establishment_date" -> "Actual Crop Establishment Date"
        "actual_crop_establishment_method" -> "Actual Method"
        "sowing_date" -> "Sowing Date"
        "seedling_age_at_transplanting" -> "Seedling Age (days)"
        "distance_between_plant_row_1" -> "Distance Between Plant Row #1 (cm)"
        "distance_between_plant_row_2" -> "Distance Between Plant Row #2 (cm)"
        "distance_between_plant_row_3" -> "Distance Between Plant Row #3 (cm)"
        "distance_within_plant_row_1" -> "Distance Within Plant Row #1 (cm)"
        "distance_within_plant_row_2" -> "Distance Within Plant Row #2 (cm)"
        "distance_within_plant_row_3" -> "Distance Within Plant Row #3 (cm)"
        "seeding_rate_kg_ha" -> "Seeding Rate (kg/ha)"
        "direct_seeding_method" -> "Direct Seeding Method"
        "num_plants_1" -> "Number of Plants #1"
        "num_plants_2" -> "Number of Plants #2"
        "num_plants_3" -> "Number of Plants #3"
        "rice_variety" -> "Rice Variety"
        "rice_variety_no" -> "Rice Variety No."
        "rice_variety_maturity_duration" -> "Maturity Duration (days)"
        "seed_class" -> "Seed Class"
        "applied_area_sqm" -> "Applied Area (sqm)"
        "harvest_date" -> "Harvest Date"
        "harvesting_method" -> "Harvesting Method"
        "bags_harvested" -> "Bags Harvested"
        "avg_bag_weight_kg" -> "Avg Bag Weight (kg)"
        "area_harvested_ha" -> "Area Harvested (ha)"
        "irrigation_supply" -> "Irrigation Supply"
        "cause" -> "Cause"
        "crop_stage" -> "Crop Stage"
        "severity" -> "Severity"
        "affected_area_ha" -> "Affected Area (ha)"
        "observed_pest" -> "Observed Pest"
        else -> key.replace('_', ' ').split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}



//fun FormData.toFieldList(): List<Pair<String, String>> = when (this) {
//    is FieldDataForm -> toFieldList()
//    is CulturalManagementForm -> toFieldList()
//    is NutrientManagementForm -> toFieldList()
//    is ProductionForm -> toFieldList()
//    is DamageAssessmentForm -> toFieldList()
//}
//
//fun FieldDataForm.toFieldList(): List<Pair<String, String>> = listOf(
//    "Land Preparation Start Date" to (landPreparationStartDate ?: "No data"),
//    "Est. Crop Establishment Date" to (estCropEstablishmentDate ?: "No data"),
//    "Est. Method" to (estCropEstablishmentMethod ?: "No data"),
//    "Total Field Area (ha)" to totalFieldAreaHa.toString(),
//    "Soil Type" to (soilType ?: "No data"),
//    "Current Field Condition" to (currentFieldCondition ?: "No data"),
//)
//
//fun CulturalManagementForm.toFieldList(): List<Pair<String, String>> = buildList {
//    add("Ecosystem" to (ecosystem ?: "No data"))
//    add("Monitoring Field Area (sqm)" to monitoringFieldAreaSqm.toString())
//    add("Actual Crop Establishment Date" to actualCropEstablishmentDate.toString())
//    add("Actual Method" to (actualCropEstablishmentMethod ?: "No data"))
//    add("Sowing Date" to (sowingDate?.toString() ?: "No data"))
//    add("Seedling Age (days)" to (seedlingAgeAtTransplanting?.toString() ?: "No data"))
//    add(
//        "Distance Between Rows" to listOfNotNull(
//            distanceBetweenPlantRow1, distanceBetweenPlantRow2, distanceBetweenPlantRow3
//        ).joinToString(", ").ifEmpty { "No data" })
//    add(
//        "Distance Within Rows" to listOfNotNull(
//            distanceWithinPlantRow1, distanceWithinPlantRow2, distanceWithinPlantRow3
//        ).joinToString(", ").ifEmpty { "No data" })
//    add("Seeding Rate (kg/ha)" to (seedingRateKgHa?.toString() ?: "No data"))
//    add("Direct Seeding Method" to (directSeedingMethod ?: "No data"))
//    add("Number of Plants #1" to (numPlants1?.toString() ?: "No data"))
//    add("Number of Plants #2" to (numPlants2?.toString() ?: "No data"))
//    add("Number of Plants #3" to (numPlants3?.toString() ?: "No data"))
//    add("Rice Variety" to (riceVariety ?: "No data"))
//    add("Rice Variety No." to (riceVarietyNo ?: "No data"))
//    add("Maturity Duration (days)" to riceVarietyMaturityDuration.toString())
//    add("Seed Class" to seedClass)
//}
//
//fun NutrientManagementForm.toFieldList(): List<Pair<String, String>> = buildList {
//    add("Applied Area (sqm)" to appliedAreaSqm.toString())
//    if (applications.isEmpty()) {
//        add("Fertilizer Applications" to "No data")
//    } else {
//        applications.forEachIndexed { idx, app ->
//            add("Fertilizer ${idx + 1} - Type" to (app.fertilizerType ?: "No data"))
//            add("  Brand" to (app.brand ?: "No data"))
//            add("  N (%)" to app.nitrogenContentPct.toString())
//            add("  P (%)" to app.phosphorusContentPct.toString())
//            add("  K (%)" to app.potassiumContentPct.toString())
//            add("  Amount" to "${app.amountApplied} ${app.amountUnit ?: ""}")
//            add("  Crop Stage" to (app.cropStageOnApplication ?: "No data"))
//        }
//    }
//}
//
//fun ProductionForm.toFieldList(): List<Pair<String, String>> = listOf(
//    "Harvest Date" to (harvestDate.toString() ?: "No data"),
//    "Harvesting Method" to (harvestingMethod ?: "No data"),
//    "Bags Harvested" to bagsHarvested.toString(),
//    "Avg Bag Weight (kg)" to avgBagWeightKg.toString(),
//    "Area Harvested (ha)" to areaHarvestedHa.toString(),
//    "Irrigation Supply" to (irrigationSupply ?: "No data"),
//)
//
//fun DamageAssessmentForm.toFieldList(): List<Pair<String, String>> = listOf(
//    "Cause" to (cause ?: "No data"),
//    "Crop Stage" to (cropStage ?: "No data"),
//    "Soil Type" to (soilType ?: "No data"),
//    "Severity" to (severity ?: "No data"),
//    "Affected Area (ha)" to affectedAreaHa.toString(),
//    "Observed Pest" to (observedPest ?: "No data"),
//)
//
//fun MonitoringVisit.toFieldList(): List<Pair<String, String>> = listOf(
//    "Date Monitored" to (dateMonitored ?: "No data"),
//    "Crop Stage" to (cropStage ?: "No data"),
//    "Soil Moisture Status" to (soilMoistureStatus ?: "No data"),
//    "Avg Plant Height (cm)" to (avgPlantHeight?.toString() ?: "No data"),
//)
//
