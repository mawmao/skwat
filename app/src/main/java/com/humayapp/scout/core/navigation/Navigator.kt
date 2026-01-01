package com.humayapp.scout.core.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

private fun createLogTag(navigatorId: String) = "Scout: [$navigatorId] navigator"

val LocalRootStackNavigator = compositionLocalOf<StackNavigator> {
    error("LocalRootStackNavigator not provided")
}

val LocalStackNavigator = compositionLocalOf<StackNavigator> {
    error("LocalStackNavigator not provided")
}

// Created for logging purposes
@Composable
fun <T : NavKey> rememberStackNavigator(id: String, initialKey: T): StackNavigator {

    Log.d(createLogTag(id), "Created [$id] navigator")
    DisposableEffect(Unit) {
        onDispose {
            Log.d(createLogTag(id), "Disposed [$id] navigator")
        }
    }

    val stack = rememberNavBackStack(initialKey)
    return remember(stack) { StackNavigator(id, stack) }
}

class StackNavigator(
    val id: String,
    private val stack: NavBackStack<NavKey>,
) {
    val current: NavKey get() = stack.last()

    fun asBackStack(): NavBackStack<NavKey> = stack

    // debugging only
    private fun logStack(action: String) {
        val suffix = "NavKey"

        val entries = stack.joinToString(separator = " -> ") { it.toString().removeSuffix(suffix) }

        val currentAction = "Action: $action"
        val currentKey = "Current: ${current.toString().removeSuffix(suffix)}"
        val currentStack = "Stack: $entries"

        Log.d(createLogTag(id), "[$id] navigator\n    $currentAction\n    $currentKey\n    $currentStack")
    }

    fun push(route: NavKey) {
        stack.add(route)

        logStack(action = "Push(${route.toString().removeSuffix("NavKey")})")
    }

    fun pop() {
        if (stack.isNotEmpty()) {
            val removed = stack.removeAt(stack.lastIndex)
            logStack("Pop(${removed.toString().removeSuffix("NavKey")})")
        } else {
            Log.w(createLogTag(id), "[$id] pop called but stack is empty. Doing nothing.")
        }
    }

    fun popAll(route: NavKey) {
        stack.clear()
        stack.add(route)

        logStack("popAll(${route.toString().removeSuffix("NavKey")})")
    }
}
