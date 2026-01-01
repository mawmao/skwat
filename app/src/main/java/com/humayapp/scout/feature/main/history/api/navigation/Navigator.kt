package com.humayapp.scout.feature.main.history.api.navigation

import com.humayapp.scout.core.navigation.StackNavigator

// did not use extension function since this will be used in a navigation bar

fun navigateToHistory(navigator: StackNavigator) = navigator.push(HistoryNavKey)
