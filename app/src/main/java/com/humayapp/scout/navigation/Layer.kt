package com.humayapp.scout.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator
import com.humayapp.scout.feature.form.api.FormType
import kotlinx.serialization.Serializable

@Serializable
sealed interface RootNavKey : NavKey {

    @Serializable
    data object Main : RootNavKey

    @Serializable
    data object Auth : RootNavKey

    @Serializable
    data class Form(val formType: FormType) : RootNavKey
}

// helpers
// auth and main are hardcoded `popAll` since this would be the only behavior they need
// forms is push and no need to call `.pop` in root navigators

fun StackNavigator.navigateToMain() = this.popAll(RootNavKey.Main)
fun StackNavigator.navigateToAuth() = this.popAll(RootNavKey.Auth)
fun StackNavigator.navigateToForms(formType: FormType) = this.push(RootNavKey.Form(formType))



