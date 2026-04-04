package com.humayapp.scout.feature.main.pending.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.MainSectionViewModel
import com.humayapp.scout.feature.main.pending.api.navigation.PendingNavKey
import com.humayapp.scout.feature.main.pending.impl.PendingScreen


fun EntryProviderScope<NavKey>.pendingEntryProvider(
    metadata: Map<String, Any>,
    vm: MainSectionViewModel,
) {
    entry<PendingNavKey>(metadata = metadata) {
        PendingScreen(vm = vm)
    }
}