package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.rememberTextFieldAdapter


object ScoutTextFieldDefaults {

    val KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
    val CornerShape: RoundedCornerShape
        @Composable get() = ScoutTheme.shapes.medium

    @Composable
    fun colors(isValueEmpty: Boolean = true): TextFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = if (isValueEmpty) {
            ScoutTheme.extras.colors.mutedOnSurfaceVariant
        } else {
            ScoutTheme.material.colorScheme.onSurface
        },
        focusedBorderColor = ScoutTheme.material.colorScheme.onBackground,
        focusedLabelColor = ScoutTheme.material.colorScheme.onBackground,
        focusedTextColor = ScoutTheme.material.colorScheme.onBackground,
        cursorColor = ScoutTheme.material.colorScheme.onBackground,
        disabledContainerColor = ScoutTheme.material.colorScheme.secondary.copy(alpha = 0.33f),
        disabledBorderColor = ScoutTheme.extras.colors.mutedOnSurfaceVariant,
        disabledTextColor = ScoutTheme.material.colorScheme.onSurface.copy(alpha = 0.75f),
        disabledLabelColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F),
        disabledTrailingIconColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F),
        errorTextColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
        errorSupportingTextColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun ScoutTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    colors: TextFieldColors = ScoutTextFieldDefaults.colors(state.text.isEmpty()),
    label: String,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    keyboardOptions: KeyboardOptions = ScoutTextFieldDefaults.KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = ScoutTextFieldDefaults.CornerShape
) {
    OutlinedTextField(
        modifier = modifier,
        state = state,
        enabled = enabled,
        readOnly = readOnly,
        lineLimits = TextFieldLineLimits.SingleLine,
        colors = colors,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        label = { Text(text = label, style = ScoutTheme.material.typography.bodyMedium) }
    )
}


@Composable
fun ScoutTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    colors: TextFieldColors = ScoutTextFieldDefaults.colors(value.isEmpty()),
    label: String,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    keyboardOptions: KeyboardOptions = ScoutTextFieldDefaults.KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = ScoutTextFieldDefaults.CornerShape
) {
    val adapter = rememberTextFieldAdapter(
        value = value,
        onValueChange = onValueChange,
        initialText = value
    )

    ScoutTextField(
        modifier = modifier,
        state = adapter.textFieldState,
        colors = colors,
        label = label,
        readOnly = readOnly,
        enabled = enabled,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape
    )
}

@Composable
fun ScoutSecureTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    colors: TextFieldColors = ScoutTextFieldDefaults.colors(state.text.isEmpty()),
    label: String,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = ScoutTextFieldDefaults.KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = ScoutTextFieldDefaults.CornerShape
) {
    OutlinedSecureTextField(
        modifier = modifier,
        state = state,
        enabled = enabled,
        colors = colors,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        label = { Text(text = label, style = MaterialTheme.typography.bodyMedium) }
    )
}

