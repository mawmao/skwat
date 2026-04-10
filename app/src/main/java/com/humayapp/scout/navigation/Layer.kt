package com.humayapp.scout.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.StackNavigator
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
    data class Form(val collectionTaskId: Int) : RootNavKey  // ✅ Only ID

    @Serializable
    data object Notification : RootNavKey

    @Serializable
    data class Detail(
        val collectionTaskId: Int,
        val activityId: Int?,
    ) : RootNavKey

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

fun StackNavigator<NavKey>.navigateToForms(taskId: Int) =    // ✅ Correct
    this.push(RootNavKey.Form(taskId)) // ✅ Pass only ID

fun StackNavigator<NavKey>.navigateToNotifications() = this.push(RootNavKey.Notification)

fun StackNavigator<NavKey>.navigateToDetail(collectionTaskId: Int, activityId: Int?) =
    this.push(RootNavKey.Detail(collectionTaskId, activityId))


//fun StackNavigator<NavKey>.navigateToOverlay(content: @Composable () -> Unit) = this.push(RootNavKey.Overlay(content))



