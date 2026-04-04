package com.humayapp.scout.feature.main.notification.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator

fun StackNavigator<NavKey>.navigateToNotifications() {
    this.push(NotificationsNavKey)
}
