package com.humayapp.scout.core

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.navigation.RootNavKey

const val SANDBOX_ENABLE = false

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.sandbox() {
    entry<RootNavKey.Sandbox>(metadata = NavTransition.fade()) {
//        CompositionLocalProvider(
//            LocalFormState provides rememberFormState(FormType.FIELD_DATA),
//            LocalStackNavigator provides rememberStackNavigator<NavKey>("field_data form", FormConfirmNavKey("1231"))
//        ) {
//            Scaffold(
//                modifier = Modifier.fillMaxSize(),
//                topBar = { FormSectionTopAppBar(onBack = {}) }
//            ) { innerPadding ->
//                Box(
//                    modifier = Modifier.fillMaxSize().padding(innerPadding),
//                ) {
//                    ImagesPage(FieldData.Images)
//                }
//            }
//        }
    }
}
