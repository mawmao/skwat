package com.humayapp.scout.feature.main.history.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.history.api.navigation.HistoryNavKey
import com.humayapp.scout.feature.main.history.impl.HistoryScreen

fun EntryProviderScope<NavKey>.historyEntryProvider(metadata: Map<String, Any>) {
    entry<HistoryNavKey>(metadata = metadata) {
        HistoryScreen()
    }
}