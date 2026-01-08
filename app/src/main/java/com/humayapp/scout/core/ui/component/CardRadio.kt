package com.humayapp.scout.core.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.ui.theme.InputFieldColors
import com.humayapp.scout.core.ui.theme.InputFieldTokens
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.animateInputFieldBorderStrokeAsState


object ScoutCardRadioGroupDefaults {

    @Composable
    fun colors(
        focusedColor: Color = InputFieldTokens.focusedColor,
        unfocusedColor: Color = InputFieldTokens.unfocusedColor,
        errorColor: Color = InputFieldTokens.errorColor,
        disabledColor: Color = InputFieldTokens.disabledColor,
    ): InputFieldColors = InputFieldColors(
        focusedColor = focusedColor,
        unfocusedColor = unfocusedColor,
        errorColor = errorColor,
        disabledColor = disabledColor,
    )

}

@Composable
fun ScoutCardRadioGroup(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = InputFieldTokens.fastSpatial()),
    ) {
        ScoutLabel(
            label = label,
            isError = isError,
            isFocused = selectedOption.isNotEmpty(),
            colors = ScoutCardRadioGroupDefaults.colors(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.smallMedium),
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val borderStroke = animateInputFieldBorderStrokeAsState(
                    enabled = enabled,
                    selected = isSelected,
                    isError = isError,
                    colors = ScoutCardRadioGroupDefaults.colors()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    border = borderStroke.value,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onOptionSelected(if (isSelected) "" else option)
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                isSelected -> InputFieldTokens.focusedColor
                                isError -> InputFieldTokens.errorColor
                                else -> InputFieldTokens.unfocusedColor
                            }
                        )
                    }
                }
            }
        }

        if (isError && errorMessage != null) {
            InputError(errorMessage = errorMessage)
        }
    }
}
