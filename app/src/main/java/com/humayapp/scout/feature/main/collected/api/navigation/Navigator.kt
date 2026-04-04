package com.humayapp.scout.feature.main.collected.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator

fun navigateToCollected(navigator: StackNavigator<NavKey>) {
    navigator.popAll(CollectedNavKey)
}
