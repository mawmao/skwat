package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
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


object ScoutTextFieldDefaults {

    val KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
    val CornerShape: RoundedCornerShape
        @Composable get() = ScoutTheme.shapes.medium

    @Composable
    fun colors(state: TextFieldState): TextFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = if (state.text.isEmpty()) {
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

//    @Composable
//    fun colors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
//        unfocusedBorderColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
//        focusedBorderColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
//        disabledBorderColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F),
//        disabledTextColor = ScoutTheme.material.colorScheme.onSurface.copy(alpha = 0.75f),
//        disabledLabelColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F),
//        disabledTrailingIconColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F),
//        errorTextColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
//        errorSupportingTextColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
//        errorBorderColor = ScoutTheme.material.colorScheme.onSurfaceVariant,
//        errorLabelColor = ScoutTheme.material.colorScheme.onSurface
//    )
//
//    @Composable
//    fun colors(isValueEmpty: Boolean): TextFieldColors = OutlinedTextFieldDefaults.colors(
//        unfocusedBorderColor = if (isValueEmpty) {
//            ScoutTheme.material.colorScheme.onSurfaceVariant
//        } else {
//            ScoutTheme.material.colorScheme.onSurface
//        },
//        disabledBorderColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F),
//
//        disabledTextColor = ScoutTheme.material.colorScheme.onSurface.copy(alpha = 0.75f),
//        disabledLabelColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F),
//        disabledTrailingIconColor = ScoutTheme.material.colorScheme.onSurfaceVariant.copy(alpha = 0.45F)
//    )
}

@Composable
fun ScoutTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    colors: TextFieldColors = ScoutTextFieldDefaults.colors(state),
    label: String,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    keyboardOptions: KeyboardOptions = ScoutTextFieldDefaults.KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = ScoutTextFieldDefaults.CornerShape
) {
    OutlinedTextField(
        modifier = modifier,
        state = state,
        enabled = enabled,
        lineLimits = TextFieldLineLimits.SingleLine,
        colors = colors,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        label = { Text(text = label, style = ScoutTheme.material.typography.bodyMedium) }
    )
}

@Composable
fun ScoutSecureTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    colors: TextFieldColors = ScoutTextFieldDefaults.colors(state),
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

