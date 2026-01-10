package com.humayapp.scout.feature.form.impl.data.registry.nutrient.overrides.application

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutIconButton
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement
import com.humayapp.scout.feature.form.impl.ui.components.DefaultWizardField
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerApplicationPage(page: NutrientManagement.FertilizerApplication) {

    val formState = LocalFormState.current
    val state = rememberFertilizerApplicationState(formState.answers.keys)

    WizardEntry(
        key = page,
        actions = {
            ScoutIconButton(
                iconSize = 28.dp,
                icon = ScoutIcons.Plus,
                onClick = {
                    state.addNewApplication()
                },
                contentDescription = null
            )
        }
    ) { entry ->
        FertilizerApplicationList(
            applications = state.applications,
            onSelect = state::selectApplication
        )
    }

    FertilizerApplicationBottomSheet(page = page, state = state)
}

@Composable
fun FertilizerApplicationList(
    applications: List<Int>,
    onSelect: (Int) -> Unit,
) {
    Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (applications.isEmpty()) {
                item {
                    EmptyApplicationPlaceholder()
                }
            } else {
                items(applications) { index ->
                    FertilizerApplicationCard(index = index, onClick = { onSelect(index) })
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerApplicationBottomSheet(
    page: NutrientManagement.FertilizerApplication,
    state: FertilizerApplicationState,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )
    val formState = LocalFormState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.showBottomSheet) {
        if (state.showBottomSheet) scope.launch { sheetState.show() }
        else scope.launch { sheetState.hide() }
    }

    if (state.showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 124.dp),
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    state.hideBottomSheet()
                }
            },
            dragHandle = {},
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(horizontal = ScoutTheme.margin, vertical = ScoutTheme.spacing.small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.isEditMode) "Edit Application" else "New Application",
                        style = ScoutTheme.material.typography.headlineSmall,
                        color = ScoutTheme.material.colorScheme.onBackground
                    )
                    ScoutIconButton(
                        icon = ScoutIcons.Close,
                        contentDescription = null,
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                state.hideBottomSheet()
                            }
                        }
                    )
                }

                state.selectedIndex?.let { currentIndex ->
                    val allFields = page.indexedFields(currentIndex)


                    allFields.take(2).forEach { field ->
                        DefaultWizardField(field, Modifier.fillMaxWidth())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
                    ) {
                        allFields.subList(2, 5).forEach { field ->
                            Box(modifier = Modifier.weight(1f)) {
                                DefaultWizardField(
                                    field = field,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
                    ) {
                        val amountField = allFields[5]
                        val unitField = allFields[6]

                        Box(modifier = Modifier.weight(0.7f)) {
                            DefaultWizardField(
                                field = amountField,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(modifier = Modifier.weight(0.3f)) {
                            DefaultWizardField(
                                field = unitField,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))


                    allFields.lastOrNull()?.let { field ->
                        DefaultWizardField(field, Modifier.fillMaxWidth())
                    }
                }

                Spacer(Modifier.weight(1f))
                ScoutButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (state.isEditMode) "Save Changes" else "Add Application",
                    onClick = {
                        state.selectedIndex?.let { currentIndex ->

                            val currentFields = page.indexedFields(currentIndex)

                            var allOk = true
                            currentFields.fastForEach { field ->
                                val ok = formState.validateField(field)
                                if (!ok) allOk = false
                            }

                            if (allOk) {
                                if (!state.applications.contains(currentIndex)) {
                                    state.applications.add(currentIndex)
                                    state.nextIndex++
                                }
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    state.hideBottomSheet()
                                }
                            } else {
                                Log.d("Scout: Validation", "Validation failed for index $currentIndex")
                            }
                        }
                    }
                )
            }
        }
    }
}

// to be improved
@Composable
fun FertilizerApplicationCard(index: Int, onClick: () -> Unit) {

    val state = LocalFormState.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(ScoutTheme.shapes.cornerMedium)
            .clickable { onClick() }
            .border(1.dp, ScoutTheme.material.colorScheme.onSurfaceVariant, ScoutTheme.shapes.cornerMedium),
    ) {

        val type = state.getAnswer("${NutrientManagement.FERTILIZER_TYPE_KEY}_$index")
        val brand = state.getAnswer("${NutrientManagement.BRAND_KEY}_$index")
        val amount = state.getAnswer("${NutrientManagement.AMOUNT_APPLIED_KEY}_$index")
        val unit = state.getAnswer("${NutrientManagement.AMOUNT_UNIT_KEY}_$index")
        val n = state.getAnswer("${NutrientManagement.NITROGEN_CONTENT_KEY}_$index")
        val p = state.getAnswer("${NutrientManagement.PHOSPHORUS_CONTENT_KEY}_$index")
        val k = state.getAnswer("${NutrientManagement.POTASSIUM_CONTENT_KEY}_$index")
        val stage = state.getAnswer("${NutrientManagement.CROP_STAGE_ON_APPLICATION_KEY}_$index")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(type, style = ScoutTheme.material.typography.titleMedium)
                Text(
                    brand,
                    style = ScoutTheme.material.typography.bodyLarge,
                    color = ScoutTheme.material.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$n% $p% $k%",
                    style = ScoutTheme.material.typography.bodyMedium,
                    color = ScoutTheme.material.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$amount $unit", style = ScoutTheme.material.typography.headlineSmall)
                Text(stage)
            }
        }
    }
}

// to be improved
@Composable
fun EmptyApplicationPlaceholder() {
    val strokeColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val strokeWidth = 2.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .drawBehind {
                val strokeWidthPx = strokeWidth.toPx()
                val halfStroke = strokeWidthPx / 2f

                val insetSize = Size(
                    width = size.width - strokeWidthPx,
                    height = size.height - strokeWidthPx
                )

                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = insetSize,
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(32f, 16f), 0f)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(ScoutIcons.Inventory),
                contentDescription = null,
                tint = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
            Text(
                text = "No applications added yet",
                style = ScoutTheme.material.typography.bodyMedium,
                color = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(ScoutTheme.spacing.small))
            Text(
                text = "Tap the + button to start",
                style = ScoutTheme.material.typography.bodySmall,
                color = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            )
        }
    }
}