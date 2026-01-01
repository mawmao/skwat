package com.humayapp.scout.feature.form.impl.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.FormConfirmNavKey
import com.humayapp.scout.feature.form.api.navigation.FormReviewNavKey
import com.humayapp.scout.feature.form.api.navigation.FormScanNavKey
import com.humayapp.scout.feature.form.api.navigation.FormWizardNavKey
import com.humayapp.scout.feature.form.impl.FormConfirmScreen
import com.humayapp.scout.feature.form.impl.FormReviewScreen
import com.humayapp.scout.feature.form.impl.FormScanScreen
import com.humayapp.scout.feature.form.impl.FormWizardScreen
import com.humayapp.scout.feature.form.impl.rememberFormState
import com.humayapp.scout.navigation.RootNavKey

fun EntryProviderScope<NavKey>.formSection(metadata: Map<String, Any>) {
    entry<RootNavKey.Form>(metadata = metadata) { key ->

        val formType = key.formType

        val formNavigator = rememberStackNavigator("${formType.id} form", initialKey = FormScanNavKey)
        val formState = rememberFormState(formType = formType)

        CompositionLocalProvider(LocalStackNavigator provides formNavigator) {
            NavDisplay(
                backStack = formNavigator.asBackStack(),
                onBack = formNavigator::pop,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<FormScanNavKey>(metadata = NavTransition.anchoredRight()) {
                        FormScanScreen(state = formState)
                    }
                    entry<FormConfirmNavKey>(metadata = NavTransition.anchoredRight()) {
                        FormConfirmScreen(state = formState)
                    }
                    entry<FormWizardNavKey>(metadata = NavTransition.anchoredRight()) {
                        FormWizardScreen(state = formState)
                    }
                    entry<FormReviewNavKey>(metadata = NavTransition.anchoredRight()) {
                        FormReviewScreen(state = formState)
                    }
                },
            )
        }
    }
}
