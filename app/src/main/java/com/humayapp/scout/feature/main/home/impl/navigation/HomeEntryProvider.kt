package com.humayapp.scout.feature.main.home.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.home.api.navigation.HomeNavKey
import com.humayapp.scout.feature.main.home.impl.HomeScreen


fun EntryProviderScope<NavKey>.homeEntryProvider(metadata: Map<String, Any>) {
    entry<HomeNavKey>(metadata = metadata) {
        HomeScreen()
    }
}