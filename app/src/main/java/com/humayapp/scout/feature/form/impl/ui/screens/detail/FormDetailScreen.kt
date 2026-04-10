package com.humayapp.scout.feature.form.impl.ui.screens.detail


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.database.model.CollectionTaskUiModel
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.component.ScoutAlertDialog
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement.Companion.culturalManagementJsonToAnswers
import com.humayapp.scout.feature.form.impl.data.registry.damage.DamageAssessment.Companion.damageAssessmentJsonToAnswers
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.fieldDataJsonToAnswers
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.FERTILIZER_TYPE_KEY
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement.Companion.nutrientManagementJsonToAnswers
import com.humayapp.scout.feature.form.impl.data.registry.production.Production.Companion.productionJsonToAnswers
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.ui.components.FormReviewItem
import com.humayapp.scout.navigation.navigateToForms
import kotlinx.serialization.json.JsonElement


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
    var showPendingApprovalDialog by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is FormDetailsUiState.Loading -> Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        is FormDetailsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tawga si mel. Error: ${state.message}")
        }

        is FormDetailsUiState.Success -> {
            FormDetailsContent(
                task = state.task,
                formData = state.formData,
                onBack = onBack,
            )
        }
    }

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
    task: CollectionTaskUiModel,
    formData: JsonElement,
    onBack: () -> Unit,
) {
    val activityTypeLabel = when (task.activityType) {
        "field-data" -> "Field Data"
        "cultural-management" -> "Cultural Management"
        "nutrient-management" -> "Nutrient Management"
        "production" -> "Production"
        "damage-assessment" -> "Damage Assessment"
        else -> task.activityType
    }

    val formType = FormType.fromActivityType(task.activityType)

    val answers = when (formType) {
        FormType.FIELD_DATA -> fieldDataJsonToAnswers(formData, task.imageUrls)
        FormType.CULTURAL_MANAGEMENT -> culturalManagementJsonToAnswers(formData, task.imageUrls)
        FormType.NUTRIENT_MANAGEMENT -> nutrientManagementJsonToAnswers(formData, task.imageUrls)
        FormType.PRODUCTION -> productionJsonToAnswers(formData, task.imageUrls)
        FormType.DAMAGE_ASSESSMENT -> damageAssessmentJsonToAnswers(formData)
    }

    val displayState = createDisplayFormState(
        formType = formType,
        answers = answers,
        mfid = task.mfid,
        collectionTaskId = task.id
    )

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
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ScoutTheme.margin),
            verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.small)

        ) {
            task.collectedBy?.let { user ->
                item {
                    FormReviewItem(label = "Collected By", value = user)
                }
            }
            task.collectedAt?.let { time ->
                item {
                    FormReviewItem(label = "Collected At", value = time.toString())
                }
            }
            if (task.verificationStatus != "pending") {
                item {
                    // to improve
                    FormReviewItem(label = "Verification Status", value = task.verificationStatus ?: "-")
                }
                task.verifiedBy?.let { user ->
                    item {
                        FormReviewItem(label = "Verified By", value = user)
                    }
                }
                task.verifiedAt?.let { time ->
                    item {
                        FormReviewItem(label = "Verified At", value = time.toString())
                    }
                }
            }
            task.remarks?.let {
                item {
                    FormReviewItem(label = "Remarks", value = it)
                }
            }
            item {
                formType.reviewContent(displayState)
            }
        }
    }
}

@Composable
fun createDisplayFormState(
    formType: FormType,
    answers: Map<String, Any?>,
    mfid: String,
    collectionTaskId: Int,
): FormState {

    val dummyPagerState = rememberPagerState(initialPage = 0) { 1 }

    val initialEntry = formType.startEntry

    return FormState(
        initialWizardEntry = initialEntry,
        pagerEntries = formType.entries,
        formType = formType,
        pagerState = dummyPagerState,
        mfid = mfid,
        collectionTaskId = collectionTaskId,
    ).apply {
        answers.forEach { (key, value) ->
            setFieldData(key, value)
        }
    }
}

