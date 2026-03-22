package com.humayapp.scout.feature.form.impl.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.core.ui.component.ScoutConfirmDialog
import com.humayapp.scout.core.ui.component.ScoutConfirmationDialog
import com.humayapp.scout.feature.form.api.id
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

        var showExitDialog by remember { mutableStateOf(false) }
        var showReviewBackDialog by remember { mutableStateOf(false) }
        var pendingExitAction by remember { mutableStateOf<(() -> Unit)?>(null) }

        CompositionLocalProvider(
            LocalStackNavigator provides formNavigator,
            LocalFormState provides formState
        ) {
            FormNavDisplay(
                modifier = Modifier.fillMaxSize(),
                onBack = {
                    when (formNavigator.current) {
                        is FormWizardNavKey -> {
                            if (formState.canScrollBack) {
                                formState.scrollWizardBack()
                            } else {
                                pendingExitAction = { rootNavigator.navigateToMain() }
                                showExitDialog = true
                            }
                        }
                        is FormReviewNavKey -> {
                            showReviewBackDialog = true
                        }
                    }
                }
            )
        }

        ScoutConfirmDialog(
            isVisible = showExitDialog,
            message = "Are you sure you want to cancel this form? Your progress will be lost.",
            onConfirm = {
                showExitDialog = false
                pendingExitAction?.invoke()
                pendingExitAction = null
            },
            onDismissRequest = {
                showExitDialog = false
                pendingExitAction = null
            }
        )

        ScoutConfirmDialog(
            isVisible = showReviewBackDialog,
            message = "Edit again?",
            onConfirm = {
                showReviewBackDialog = false
                formNavigator.pop()
            },
            onDismissRequest = {
                showReviewBackDialog = false
            }
        )
    }
}
