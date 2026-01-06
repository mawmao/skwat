package com.humayapp.scout.core.ui.util

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun rememberTextFieldAdapter(
    value: String? = null,
    onValueChange: (String) -> Unit,
    initialText: String = "",
): TextFieldAdapter {
    val textFieldState = rememberTextFieldState(initialText = value ?: initialText)

    return remember(textFieldState) { TextFieldAdapter(textFieldState) }.apply {
        LaunchedEffect(value) {
            if (value != null && value != textFieldState.text.toString()) {
                textFieldState.apply {
                    val wasComposing = composition != null
                    edit {
                        replace(0, length, value)
                        if (selection.min <= value.length) {
                            selection = TextRange(selection.min.coerceAtMost(value.length))
                        } else {
                            placeCursorAtEnd()
                        }
                    }
                }
            }
        }

        LaunchedEffect(textFieldState) {
            snapshotFlow { textFieldState.text.toString() }
                .distinctUntilChanged()
                .collect { newText ->
                    if (value == null || newText != value) {
                        onValueChange(newText)
                    }
                }
        }
    }
}

class TextFieldAdapter(internal val textFieldState: TextFieldState) {
    val value: String
        get() = textFieldState.text.toString()

    fun updateText(newText: String) {
        textFieldState.edit {
            replace(0, length, newText)
            if (newText.length <= selection.min) {
                selection = TextRange(newText.length)
            }
        }
    }
}
