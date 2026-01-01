package com.humayapp.scout.feature.auth.login.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.auth.login.api.navigation.LoginNavKey
import com.humayapp.scout.feature.auth.login.impl.LoginScreen

fun EntryProviderScope<NavKey>.loginEntryProvider(metadata: Map<String, Any>) {
    entry<LoginNavKey>(metadata = metadata) {
        LoginScreen()
    }
}
