package com.humayapp.scout.core.navigation

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer

private fun createLogTag(navigatorId: String) = "Scout: [$navigatorId] navigator"

val LocalRootStackNavigator = staticCompositionLocalOf<StackNavigator<NavKey>> {
    error("LocalRootStackNavigator not provided")
}

val LocalStackNavigator = staticCompositionLocalOf<StackNavigator<NavKey>> {
    error("LocalStackNavigator not provided")
}


// Created for logging purposes
@Composable
fun <T : NavKey> rememberStackNavigator(id: String, initialKey: T): StackNavigator<T> {

    Log.d(createLogTag(id), "Created [$id] navigator")
    DisposableEffect(Unit) {
        onDispose {
            Log.d(createLogTag(id), "Disposed [$id] navigator")
        }
    }

    val stack = rememberNavBackStack<T>(initialKey)
    return remember(stack) { StackNavigator(id, stack) }
}

@Composable
fun <T : NavKey> rememberNavBackStack(vararg elements: T): NavBackStack<T> {
    return rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        NavBackStack(*elements)
    }
}

enum class NavAction { Push, Pop, PopAll }

open class StackNavigator<T: NavKey>(
    val id: String,
    val stack: NavBackStack<T>,
) {
    private var lastNavTime = 0L
    private val debounceMs = 300L

    private fun canNavigate(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastNavTime < debounceMs) return false
        lastNavTime = now
        return true
    }

    val current: T get() = stack.last()

    fun asBackStack(): NavBackStack<T> = stack

    // debugging only
    private fun logStack(action: String) {
        val suffix = "NavKey"

        val entries = stack.joinToString(separator = " -> ") { it.toString().removeSuffix(suffix) }

        val currentAction = "Action: $action"
        val currentKey = "Current: ${current.toString().removeSuffix(suffix)}"
        val currentStack = "Stack: $entries"

        Log.d(
            createLogTag(id),
            "[$id] navigator\n    $currentAction\n    $currentKey\n    $currentStack"
        )
    }

    fun push(route: T) {
        if (!canNavigate()) return
        stack.add(route)
        logStack(action = "Push(${route.toString().removeSuffix("NavKey")})")
    }

    fun pop(): T? {
        if (!canNavigate() || stack.isEmpty())  return null
        val removed = stack.removeAt(stack.lastIndex)
        logStack("Pop(${removed.toString().removeSuffix("NavKey")})")
        return removed
    }

    fun popAll(route: T) {
        if (!canNavigate()) return
        stack.clear()
        stack.add(route)
        logStack("popAll(${route.toString().removeSuffix("NavKey")})")
    }
}
