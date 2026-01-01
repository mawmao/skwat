package com.humayapp.scout.feature.form.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FormScanNavKey : NavKey

@Serializable
data object FormConfirmNavKey : NavKey

@Serializable
data object FormWizardNavKey : NavKey

@Serializable
data object FormReviewNavKey : NavKey
