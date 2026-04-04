package com.humayapp.scout.feature.main.collected.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.MainSectionViewModel
import com.humayapp.scout.feature.main.collected.api.navigation.CollectedNavKey
import com.humayapp.scout.feature.main.collected.impl.CollectedScreen


fun EntryProviderScope<NavKey>.collectedEntryProvider(
    metadata: Map<String, Any>,
    vm: MainSectionViewModel,
) {
    entry<CollectedNavKey>(metadata = metadata) {
        CollectedScreen(vm = vm)
    }
}