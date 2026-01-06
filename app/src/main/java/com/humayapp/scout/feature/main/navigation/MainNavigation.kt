package com.humayapp.scout.feature.main.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.ui.component.NavigationItem
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.feature.main.MainSection
import com.humayapp.scout.feature.main.history.api.navigation.HistoryNavKey
import com.humayapp.scout.feature.main.history.api.navigation.navigateToHistory
import com.humayapp.scout.feature.main.home.api.navigation.HomeNavKey
import com.humayapp.scout.feature.main.home.api.navigation.navigateToHome
import com.humayapp.scout.navigation.RootNavKey


fun EntryProviderScope<NavKey>.mainSection(metadata: Map<String, Any>) {
    entry<RootNavKey.Main>(metadata = metadata) {
        MainSection()
    }
}

