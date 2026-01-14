package com.humayapp.scout.feature.form.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FormScanNavKey : NavKey

@Serializable
data class FormConfirmNavKey(val mfid: String) : NavKey

@Serializable
data object FormWizardNavKey : NavKey

@Serializable
data object FormReviewNavKey : NavKey
