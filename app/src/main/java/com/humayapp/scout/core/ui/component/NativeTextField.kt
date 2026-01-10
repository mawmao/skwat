package com.humayapp.scout.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.ui.theme.InputFieldTokens
import com.humayapp.scout.core.ui.util.animateBorderStrokeAsState

// modified version of Material3's OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeOutlinedTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
    label: String,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = ScoutTextFieldDefaults.colors(state.text.isEmpty()),
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
    interactionSource: MutableInteractionSource? = null,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

    val textColor = textStyle.color.takeOrElse {
        val focused = interactionSource.collectIsFocusedAsState().value
        when {
            !enabled -> colors.disabledTextColor
            isError -> colors.errorTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }

    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))


    val labelStyle = InputFieldTokens.labelTextStyle
    val labelComposable: @Composable (TextFieldLabelScope.() -> Unit) = {
        Text(text = label, style = labelStyle)
    }

    val topPadding = with(LocalDensity.current) {
        val compositionLocalValue = labelStyle.lineHeight
        val fallbackValue = 16.sp
        val value = if (compositionLocalValue.isSp) compositionLocalValue else fallbackValue
        value.toDp() / 2
    }


    @Suppress("NAME_SHADOWING")
    val modifier = modifier
        .then(
            if (labelPosition !is TextFieldLabelPosition.Above) {
                Modifier
                    .semantics(mergeDescendants = true) {}
                    .padding(top = topPadding)
            } else {
                Modifier
            }
        )
        .then(if (isError) Modifier.semantics { error("Error") } else Modifier)
        .defaultMinSize(
            minHeight = 56.dp,
        )

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            state = state,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(if (isError) colors.errorCursorColor else colors.cursorColor),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            scrollState = scrollState,
            decorator =
                OutlinedTextFieldDefaults.decorator(
                    state = state,
                    enabled = enabled,
                    lineLimits = lineLimits,
                    outputTransformation = outputTransformation,
                    interactionSource = interactionSource,
                    labelPosition = labelPosition,
                    label = labelComposable,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    isError = isError,
                    colors = colors,
                    contentPadding = contentPadding,
                    container = {
                        NativeTextFieldContainer(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = shape,
                        )
                    },
                ),
        )
    }
}


@Composable
fun NativeTextFieldContainer(
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    modifier: Modifier = Modifier,
    colors: TextFieldColors,
    shape: Shape = OutlinedTextFieldDefaults.shape,
) {
    val focused = interactionSource.collectIsFocusedAsState().value
    val borderStroke = animateBorderStrokeAsState(
        enabled,
        isError,
        focused,
        colors,
    )


    val containerColor = animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledContainerColor
            isError -> colors.errorContainerColor
            focused -> colors.focusedContainerColor
            else -> colors.unfocusedContainerColor
        },
        animationSpec = spring(
            dampingRatio = 1.0f,
            stiffness = 3800.0f,
        ),
    )

    Box(
        modifier = modifier
            .border(borderStroke.value, shape)
            .drawWithCache {
                val outline = shape.createOutline(size, layoutDirection, this)
                onDrawBehind { drawOutline(outline, color = containerColor.value) }
            }
    )
}

