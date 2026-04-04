package com.humayapp.scout.feature.main.approved.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator

fun navigateToApproved(navigator: StackNavigator<NavKey>) {
    navigator.popAll(ApprovedNavKey)
}
