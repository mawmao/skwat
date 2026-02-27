package com.humayapp.scout.feature.form.impl.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.api.navigation.FormConfirmNavKey
import com.humayapp.scout.feature.form.api.navigation.FormReviewNavKey
import com.humayapp.scout.feature.form.api.navigation.FormWizardNavKey
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.rememberFormState
import com.humayapp.scout.navigation.RootNavKey
import com.humayapp.scout.navigation.navigateToMain

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.formSection(metadata: Map<String, Any>) {

    entry<RootNavKey.Form>(metadata = metadata) { key ->

        val rootNavigator = LocalRootStackNavigator.current

        val formType = key.formType

        val formState = rememberFormState(formType, key.mfid)
        val formNavigator = rememberStackNavigator<NavKey>("${formType.id} form", FormWizardNavKey)

        CompositionLocalProvider(
            LocalStackNavigator provides formNavigator,
            LocalFormState provides formState
        ) {
            FormNavDisplay(
                modifier = Modifier.fillMaxSize(),
                onBack = {
                    when (formNavigator.current) {
                        // currently unused. to remove soon.
                        is FormConfirmNavKey -> rootNavigator.navigateToMain()

                        // it does not make sense to go back to confirmation screen
                        // on first wizard page, this should ask the user if they
                        // want to cancel the form collection, then go back to the
                        // home screen if yes, if not on first page, `formNavigator` pop works
                        is FormWizardNavKey -> {
                            if (formState.canScrollBack) formState.scrollWizardBack()
                            else rootNavigator.navigateToMain()
                        }

                        is FormReviewNavKey -> formNavigator.pop()
                    }
                }
            )
        }
    }
}

