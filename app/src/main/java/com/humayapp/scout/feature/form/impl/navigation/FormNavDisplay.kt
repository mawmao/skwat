package com.humayapp.scout.feature.form.impl.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.humayapp.scout.feature.form.impl.ui.components.FormSectionTopAppBar
import com.humayapp.scout.feature.form.impl.ui.screens.FormConfirmScreen
import com.humayapp.scout.feature.form.impl.ui.screens.FormReviewScreen
import com.humayapp.scout.feature.form.impl.ui.screens.FormScanScreen
import com.humayapp.scout.feature.form.impl.ui.screens.FormWizardSection

@Composable
fun FormNavDisplay(
    modifier: Modifier,
    onBack: () -> Unit
) {
    val formNavigator = LocalStackNavigator.current

    Scaffold(
        modifier = modifier,
        topBar = { FormSectionTopAppBar(onBack = onBack) },
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = formNavigator.asBackStack(),
            onBack = onBack,
            entryProvider = entryProvider {
                entry<FormScanNavKey>(metadata = NavTransition.anchoredRight()) {
                    FormScanScreen()
                }
                entry<FormConfirmNavKey>(metadata = NavTransition.anchoredRight()) {
                    FormConfirmScreen()
                }
                entry<FormWizardNavKey>(metadata = NavTransition.anchoredRight()) {
                    FormWizardSection()
                }
                entry<FormReviewNavKey>(metadata = NavTransition.anchoredRight()) {
                    FormReviewScreen()
                }
            }
        )
    }
}