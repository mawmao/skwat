package com.humayapp.scout.feature.main.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.MainSection
import com.humayapp.scout.navigation.RootNavKey

fun EntryProviderScope<NavKey>.mainSection(metadata: Map<String, Any>) {
    entry<RootNavKey.Main>(metadata = metadata) {
        MainSection()
    }
}

