package com.humayapp.scout.feature.auth.login.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator

fun StackNavigator<NavKey>.navigateToLogin(popAll: Boolean) {
    if (popAll) {
        this.popAll(LoginNavKey)
    } else {
        this.push(LoginNavKey)
    }
}
