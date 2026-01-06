package com.humayapp.scout.feature.form.impl.ui.util

fun coerceDecimalInput(input: String): String {
    val trimmed = input.trim()
    return when {
        trimmed == "." -> "0."
        trimmed.startsWith(".") -> "0$trimmed"
        else -> trimmed
    }
}
