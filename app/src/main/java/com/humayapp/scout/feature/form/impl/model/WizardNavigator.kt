package com.humayapp.scout.feature.form.impl.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf


@Stable
class WizardNavigator(initial: WizardEntry) {

    private val stack = mutableStateListOf(initial)
    private val current = mutableStateOf(initial)

    val currentScreen: WizardEntry get() = current.value

    fun hasNext(data: Map<String, Any?>): Boolean =
        current.value.nextScreen(data) != null

    fun next(data: Map<String, Any?>) {
        current.value.nextScreen(data)?.let {
            stack.add(it)
            current.value = it
        }
    }

    fun back() {
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            current.value = stack.last()
        }
    }
}
