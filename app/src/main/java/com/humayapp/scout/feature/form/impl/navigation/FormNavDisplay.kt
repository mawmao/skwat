package com.humayapp.scout.feature.form.impl.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.feature.form.api.navigation.FormConfirmNavKey
import com.humayapp.scout.feature.form.api.navigation.FormReviewNavKey
import com.humayapp.scout.feature.form.api.navigation.FormScanNavKey
import com.humayapp.scout.feature.form.api.navigation.FormWizardNavKey
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.ui.components.FormSectionTopAppBar
import com.humayapp.scout.feature.form.impl.ui.screens.FormConfirmScreen
import com.humayapp.scout.feature.form.impl.ui.screens.FormReviewScreen
import com.humayapp.scout.feature.form.impl.ui.screens.FormReviewViewModel
import com.humayapp.scout.feature.form.impl.ui.screens.FormScanScreen
import com.humayapp.scout.feature.form.impl.ui.screens.FormWizardScreen


@Composable
fun FormNavDisplay(modifier: Modifier, onBack: () -> Unit) {

    val formBackStack = LocalStackNavigator.current.asBackStack()
    val formTransition = NavTransition.anchoredRight()
    val state = LocalFormState.current

    Scaffold(modifier = modifier, topBar = { FormSectionTopAppBar(onBack = onBack) }) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = formBackStack,
            onBack = onBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<FormScanNavKey>(metadata = formTransition) { FormScanScreen() }
                entry<FormConfirmNavKey>(metadata = formTransition) { FormConfirmScreen() }
                entry<FormWizardNavKey>(metadata = formTransition) { FormWizardScreen() }
                entry<FormReviewNavKey>(metadata = formTransition) {
                    val vm = hiltViewModel<FormReviewViewModel, FormReviewViewModel.Factory>(
                        key = "${state.formType}-${state.mfid}"
                    ) { it.create(formType = state.formType, mfid = state.mfid) }
                    FormReviewScreen(vm = vm)
                }
            }
        )
    }
}