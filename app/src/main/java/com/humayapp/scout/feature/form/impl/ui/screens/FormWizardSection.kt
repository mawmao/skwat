package com.humayapp.scout.feature.form.impl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition.defaultTween
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.navigation.navigateToFormReview
import com.humayapp.scout.feature.form.impl.FieldData
import com.humayapp.scout.feature.form.impl.WizardMetadata
import com.humayapp.scout.feature.form.impl.ui.WizardPager
import com.humayapp.scout.feature.form.impl.ui.WizardPagerButtons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberWizardState(
    metadata: WizardMetadata,
    initialRepeatCount: Int = 1
): WizardState {
    val metadataState = remember { mutableStateOf(metadata) }

    val repeatCount = remember { mutableIntStateOf(initialRepeatCount) }
    val prevRepeatCount = remember { mutableIntStateOf(repeatCount.value) }

    val pagerState = rememberPagerState(pageCount = { metadataState.value.expandedKeys.size })
    val scope = rememberCoroutineScope()

    val currentPage = remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentPage.value = page
        }
    }

    LaunchedEffect(repeatCount.value) {
        if (repeatCount.value > prevRepeatCount.value) {
            val nextPage = (pagerState.currentPage + 1).coerceAtMost(metadataState.value.expandedKeys.size - 1)
            pagerState.animateScrollToPage(
                page = nextPage, animationSpec = defaultTween(300)
            )
        }
        prevRepeatCount.value = repeatCount.value
    }

    return remember(metadataState, pagerState, scope, repeatCount, currentPage) {
        WizardState(pagerState, metadataState, scope, repeatCount, currentPage)
    }
}


@Stable
class WizardState(
    val pagerState: PagerState,
    private val metadataState: MutableState<WizardMetadata>,
    val scope: CoroutineScope,
    val repeatCount: MutableIntState,
    val currentPage: MutableIntState,
) {

    val metadata: WizardMetadata get() = metadataState.value

    fun incrementRepeat() {
        repeatCount.value += 1
        refreshMetadata()
    }

    fun decrementRepeat() {
        if (repeatCount.value <= 1) return
        repeatCount.value -= 1
        refreshMetadata()
    }

    private fun refreshMetadata() {
        // to change
        metadataState.value = FieldData.createFieldDataWizardMetadata(repeatCount.value)
    }

    fun scrollBackward() {
        scope.launch {
            val prevPage = (currentPage.value - 1).coerceAtLeast(0)
            pagerState.animateScrollToPage(page = prevPage, animationSpec = defaultTween(300))
        }
    }

    fun scrollForward() {
        scope.launch {
            val nextPage = (currentPage.value + 1).coerceAtMost(metadata.expandedKeys.size - 1)
            pagerState.animateScrollToPage(page = nextPage, animationSpec = defaultTween(300))
        }
    }
}

@Composable
fun FormWizardSection() {

    val formNavigator = LocalStackNavigator.current
    val wizardState = rememberWizardState(metadata = FieldData.createFieldDataWizardMetadata())

    Column(modifier = Modifier.fillMaxSize()) {
        WizardPager(wizardState.pagerState, wizardState.metadata)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScoutTheme.margin),
            verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.small)
        ) {
            if (wizardState.pagerState.currentPage != 0) {
                ScoutOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Previous Page",
                    onClick = { wizardState.scrollBackward() }
                )
            }

            WizardPagerButtons(wizardState)

            if (wizardState.pagerState.currentPage < wizardState.metadata.expandedKeys.size - 1) {
                ScoutButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Next Page",
                    onClick = { wizardState.scrollForward() }

                )
            } else {
                ScoutButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Review",
                    onClick = {
                        formNavigator.navigateToFormReview()
                    }
                )
            }
        }
    }
}
