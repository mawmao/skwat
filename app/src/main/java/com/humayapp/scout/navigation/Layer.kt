package com.humayapp.scout.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator
import com.humayapp.scout.feature.form.api.FormType
import kotlinx.serialization.Serializable

@Serializable
sealed interface RootNavKey : NavKey {

    // Sandbox (for development only)
    @Serializable
    data object Sandbox : RootNavKey

    @Serializable
    data object Main : RootNavKey

    @Serializable
    data object Auth : RootNavKey

    @Serializable
    data class Form(val formType: FormType, val mfid: String) : RootNavKey

    @Serializable
    data class Detail(val content: @Composable () -> Unit) : RootNavKey

    @Serializable
    data class Overlay(val overlayType: OverlayType) : RootNavKey
}

@Serializable
sealed class OverlayType {

    @Serializable
    data class Scan(val formTypeName: String) : OverlayType()
}



// auth and main are hardcoded `popAll` since this would be the only behavior they need
fun StackNavigator<NavKey>.navigateToMain() = this.popAll(RootNavKey.Main)
fun StackNavigator<NavKey>.navigateToAuth() = this.popAll(RootNavKey.Auth)

fun StackNavigator<NavKey>.navigateToForms(formType: FormType, mfid: String) = this.push(RootNavKey.Form(formType, mfid))
fun StackNavigator<NavKey>.navigateToDetail(content: @Composable () -> Unit) = this.push(RootNavKey.Detail(content))


//fun StackNavigator<NavKey>.navigateToOverlay(content: @Composable () -> Unit) = this.push(RootNavKey.Overlay(content))



