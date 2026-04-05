package com.humayapp.scout.feature.main.notification.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.main.notification.api.navigation.NotificationsNavKey
import com.humayapp.scout.feature.main.notification.impl.NotificationsScreen
import com.humayapp.scout.feature.main.pending.api.navigation.PendingNavKey
import com.humayapp.scout.navigation.RootNavKey


fun EntryProviderScope<NavKey>.notificationsEntryProvider(metadata: Map<String, Any>) {
    entry<RootNavKey.Notification>(metadata = metadata) {
        NotificationsScreen()
    }
}
