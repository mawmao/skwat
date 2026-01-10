package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.ui.theme.InputFieldColors
import com.humayapp.scout.core.ui.theme.InputFieldTokens

// todo: review

@Composable
fun ScoutLabel(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isFocused: Boolean = false,
    isError: Boolean = false,
    colors: InputFieldColors = InputFieldColors.default(),
) {
    val targetColor = when {
        !enabled -> colors.disabledColor
        isFocused -> colors.focusedColor
        isError -> colors.errorColor
        else -> colors.unfocusedColor
    }

    Box(
        Modifier
            .wrapContentHeight()

            // extract soon
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            modifier = modifier,
            style = InputFieldTokens.labelTextStyle,
            color = targetColor
        )
    }
}
