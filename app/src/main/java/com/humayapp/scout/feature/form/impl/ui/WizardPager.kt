package com.humayapp.scout.feature.form.impl.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.feature.form.impl.DefaultFieldDataEntry
import com.humayapp.scout.feature.form.impl.FieldData.Companion.pageOverrides
import com.humayapp.scout.feature.form.impl.WizardMetadata
import com.humayapp.scout.feature.form.impl.ui.components.WizardProgressBar
import com.humayapp.scout.feature.form.impl.ui.screens.WizardState


@Composable
fun ColumnScope.WizardPager(
    pagerState: PagerState,
    metadata: WizardMetadata
) {
    WizardProgressBar(
        height = 4.dp,
        totalCount = pagerState.pageCount,
        currentCount = pagerState.currentPage + 1
    )
    HorizontalPager(
        modifier = Modifier.weight(1f),
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        val pageKey = metadata.expandedKeys[page]
        val renderer = pageOverrides[pageKey] ?: { DefaultFieldDataEntry(it) }
        renderer(pageKey)
    }
}

@Composable
fun WizardPagerButtons(wizardState: WizardState) {
    val currentPage by wizardState.currentPage

    val fixed = wizardState.metadata.pageCounts.fixed
    val perInstance = wizardState.metadata.pageCounts.repeatablePerInstance
    val repeatCount = wizardState.repeatCount

    val lastRepeatable = fixed + perInstance * repeatCount.value - 1

    val showAddButton = currentPage == lastRepeatable
    val showRemoveButton = currentPage in fixed..lastRepeatable && repeatCount.value > 1

    if (showRemoveButton) {
        ScoutOutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Remove Field",
            onClick = { wizardState.decrementRepeat() }
        )
    }

    if (showAddButton) {
        ScoutOutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Add Another Field",
            onClick = { wizardState.incrementRepeat() }
        )
    }
}
