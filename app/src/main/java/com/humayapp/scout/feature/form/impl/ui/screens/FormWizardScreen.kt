package com.humayapp.scout.feature.form.impl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutErrorDialog
import com.humayapp.scout.core.ui.component.ScoutOutlinedButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.navigation.navigateToFormReview
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.ui.components.DefaultWizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardProgressBar


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormWizardScreen() {

    val formNavigator = LocalStackNavigator.current
    val formState = LocalFormState.current

    val maxWidthModifier = Modifier.fillMaxWidth()

    formState.dialogState?.let { dialog ->
        ScoutErrorDialog(
            title = dialog.title,
            message = dialog.message,
            onDismissRequest = {
                dialog.fieldKey?.let { key ->
                    formState.acknowledgedWarnings.add(key)
                }
                formState.clearDialog()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WizardPager(modifier = Modifier.weight(1f))
        Column(
            modifier = maxWidthModifier.padding(ScoutTheme.margin),
            verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.small)
        ) {
            if (formState.canScrollBack) {
                ScoutOutlinedButton(
                    modifier = maxWidthModifier,
                    text = "Previous Page",
                    onClick = formState::scrollWizardBack
                )
            }

            ScoutButton(
                modifier = maxWidthModifier,
                text = if (formState.canScrollNext) "Next Page" else "Review",
                onClick = {
                    val current = formState.currentScreen
                    val allValid = current.nextRule(formState) && formState.validatePage(current)
                    if (allValid) {
                        if (formState.canScrollNext) formState.scrollWizardNext()
                        else formNavigator.navigateToFormReview()
                    }
                }
            )
        }
    }
}


@Composable
fun WizardPager(modifier: Modifier = Modifier) {
    val formState = LocalFormState.current
    val entries = formState.formType.entries
    val pagerState = formState.pagerState
    val currentScreen = formState.currentScreen
    val formOverrides = formState.formType.overrides

    LaunchedEffect(currentScreen) {
        val pageIndex = entries.indexOf(currentScreen)
        if (pageIndex != -1 && pageIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(
                page = pageIndex,
                animationSpec = NavTransition.defaultTween()
            )
        }
    }

    WizardProgressBar(currentCount = pagerState.currentPage + 1, totalCount = pagerState.pageCount)

    HorizontalPager(modifier = modifier, state = pagerState, userScrollEnabled = false) { page ->
        val pageKey = entries[page]
        val renderer = formOverrides?.get(pageKey) ?: { DefaultWizardEntry(it) }
        renderer(pageKey)
    }
}
