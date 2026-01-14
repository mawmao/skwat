package com.humayapp.scout.core

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides.ImagesPage
import com.humayapp.scout.feature.form.impl.rememberFormState
import com.humayapp.scout.navigation.RootNavKey

const val SANDBOX_ENABLE = false

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.sandbox() {
    entry<RootNavKey.Sandbox>(metadata = NavTransition.fade()) {
        CompositionLocalProvider(LocalFormState provides rememberFormState(FormType.FIELD_DATA)) {
            ImagesPage(FieldData.Images)
        }
    }
}
