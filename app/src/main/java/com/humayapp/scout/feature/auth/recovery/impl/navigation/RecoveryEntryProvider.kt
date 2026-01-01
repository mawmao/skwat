package com.humayapp.scout.feature.auth.recovery.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.feature.auth.recovery.api.navigation.RecoveryOtpNavKey
import com.humayapp.scout.feature.auth.recovery.api.navigation.RecoveryResetNavKey
import com.humayapp.scout.feature.auth.recovery.api.navigation.RecoverySuccessNavKey
import com.humayapp.scout.feature.auth.recovery.impl.RecoveryOtpScreen
import com.humayapp.scout.feature.auth.recovery.impl.RecoveryResetScreen
import com.humayapp.scout.feature.auth.recovery.impl.RecoverySuccessScreen

fun EntryProviderScope<NavKey>.recoveryEntryProvider() {
    entry<RecoveryOtpNavKey>(metadata = NavTransition.anchoredRight()) {
        RecoveryOtpScreen()
    }
    entry<RecoveryResetNavKey>(metadata = NavTransition.anchoredRight()) {
        RecoveryResetScreen()
    }
    entry<RecoverySuccessNavKey>(metadata = NavTransition.anchoredRight()) {
        RecoverySuccessScreen()
    }
}
