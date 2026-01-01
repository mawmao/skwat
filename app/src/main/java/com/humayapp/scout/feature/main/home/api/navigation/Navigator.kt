package com.humayapp.scout.feature.main.home.api.navigation

import com.humayapp.scout.core.navigation.StackNavigator

// did not use extension function since this will be used in a navigation bar

fun navigateToHome(navigator: StackNavigator) {
    navigator.popAll(HomeNavKey)
}