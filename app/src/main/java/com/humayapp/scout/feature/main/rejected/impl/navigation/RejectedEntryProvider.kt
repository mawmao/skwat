package com.humayapp.scout.feature.main.rejected.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.MainSectionViewModel
import com.humayapp.scout.feature.main.rejected.api.navigation.RejectedNavKey
import com.humayapp.scout.feature.main.rejected.impl.RejectedScreen


fun EntryProviderScope<NavKey>.rejectedEntryProvider(
    metadata: Map<String, Any>,
    vm: MainSectionViewModel,
) {
    entry<RejectedNavKey>(metadata = metadata) {
        RejectedScreen(vm = vm)
    }
}