package com.humayapp.scout.feature.form.impl.data.registry.nutrient.overrides

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField
import kotlinx.coroutines.launch

// This would show an empty bottom sheet whenever the plus icon is clicked,
// If a `FertilizerApplicationCard` is clicked, it should show the filled bottom sheet depending on the entry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerApplicationPage(
    page: NutrientManagement.FertilizerApplication,
) {
    val applications = remember { mutableStateListOf<Int>() }
    var nextIndex by remember { mutableIntStateOf(1) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val state = LocalFormState.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }

    WizardEntry(
        key = page,
        actions = {
            ScoutIconButton(
                iconSize = 28.dp,
                icon = ScoutIcons.Plus,
                onClick = {
                    selectedIndex = nextIndex
                    showBottomSheet = true
                },
                contentDescription = null
            )
        }
    ) { entry ->
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
                        FertilizerApplicationCard(
                            index = index,
                            onClick = {
                                selectedIndex = index
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 124.dp),
            onDismissRequest = { showBottomSheet = false },
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
                        text = if (applications.contains(selectedIndex)) "Edit Application" else "New Application",
                        style = ScoutTheme.material.typography.headlineSmall,
                        color = ScoutTheme.material.colorScheme.onBackground
                    )
                    ScoutIconButton(
                        icon = ScoutIcons.Close,
                        contentDescription = null,
                        onClick = { closeSheet() }
                    )
                }

                selectedIndex?.let { currentIndex ->
                    val allFields = NutrientManagement.fertilizerApplicationFields(currentIndex)

                    @Composable
                    fun BoundWizardField(field: WizardField, modifier: Modifier = Modifier) {
                        WizardField(
                            modifier = modifier,
                            field = field,
                            value = { state.getAnswer(field.key) },
                            onValueChange = { v -> state.setAnswer(field.key, v) },
                        )
                        Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
                    }

                    allFields.take(2).forEach { field ->
                        BoundWizardField(field, Modifier.fillMaxWidth())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
                    ) {
                        allFields.subList(2, 5).forEach { field ->
                            Box(modifier = Modifier.weight(1f)) {
                                BoundWizardField(
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
                            BoundWizardField(
                                field = amountField,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(modifier = Modifier.weight(0.3f)) {
                            BoundWizardField(
                                field = unitField,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))


                    allFields.lastOrNull()?.let { field ->
                        BoundWizardField(field, Modifier.fillMaxWidth())
                    }
                }

                Spacer(Modifier.weight(1f))
                ScoutButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (applications.contains(selectedIndex)) "Save Changes" else "Add Application",
                    onClick = {
                        selectedIndex?.let { currentIndex ->

                            val currentFields = NutrientManagement.fertilizerApplicationFields(currentIndex)

                            var allOk = true
                            currentFields.fastForEach { field ->
                                val ok = state.validateField(field)
                                if (!ok) allOk = false
                            }

                            if (allOk) {
                                if (!applications.contains(currentIndex)) {
                                    applications.add(currentIndex)
                                    nextIndex++
                                }
                                closeSheet()
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

@Composable
fun EmptyApplicationPlaceholder() {
    val strokeColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val strokeWidth = 2.dp
    val density = LocalDensity.current

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