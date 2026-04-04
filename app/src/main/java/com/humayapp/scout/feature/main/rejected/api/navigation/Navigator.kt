package com.humayapp.scout.feature.main.rejected.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator

fun navigateToRejected(navigator: StackNavigator<NavKey>) {
    navigator.popAll(RejectedNavKey)
}
