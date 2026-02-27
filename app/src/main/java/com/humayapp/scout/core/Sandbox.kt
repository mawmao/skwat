package com.humayapp.scout.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.navigation.rememberStackNavigator
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.navigation.FormConfirmNavKey
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ImagesPage
import com.humayapp.scout.feature.form.impl.rememberFormState
import com.humayapp.scout.feature.form.impl.ui.components.FormSectionTopAppBar
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
