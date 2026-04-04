package com.humayapp.scout.feature.main.approved.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.MainSectionViewModel
import com.humayapp.scout.feature.main.approved.api.navigation.ApprovedNavKey
import com.humayapp.scout.feature.main.approved.impl.ApprovedScreen


fun EntryProviderScope<NavKey>.approvedEntryProvider(
    metadata: Map<String, Any>,
    vm: MainSectionViewModel,
) {
    entry<ApprovedNavKey>(metadata = metadata) {
        ApprovedScreen(vm = vm)
    }
}