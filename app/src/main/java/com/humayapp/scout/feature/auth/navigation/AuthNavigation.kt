package com.humayapp.scout.feature.auth.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.feature.auth.login.api.navigation.LoginNavKey
import com.humayapp.scout.feature.auth.login.impl.navigation.loginEntryProvider
import com.humayapp.scout.feature.auth.recovery.impl.navigation.recoveryEntryProvider
import com.humayapp.scout.navigation.RootNavKey

fun EntryProviderScope<NavKey>.authSection(metadata: Map<String, Any> = emptyMap()) {
    entry<RootNavKey.Auth>(metadata = metadata) {

        val authNavigator = rememberStackNavigator(id = "auth", initialKey = LoginNavKey)

        CompositionLocalProvider(LocalStackNavigator provides authNavigator) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavDisplay(
                    modifier = Modifier.padding(innerPadding),
                    backStack = authNavigator.asBackStack(),
                    onBack = authNavigator::pop,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        loginEntryProvider(metadata = NavTransition.fade())
                        recoveryEntryProvider()
                    },
                )
            }
        }
    }
}


