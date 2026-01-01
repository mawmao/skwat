package com.humayapp.scout.feature.auth.login.api.navigation

import com.humayapp.scout.core.navigation.StackNavigator

fun StackNavigator.navigateToLogin(popAll: Boolean) {
    if (popAll) {
        this.popAll(LoginNavKey)
    } else {
        this.push(LoginNavKey)
    }
}
