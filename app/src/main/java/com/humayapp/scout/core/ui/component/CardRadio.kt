package com.humayapp.scout.core.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.ui.theme.ScoutTheme

// todo: review

@Composable
fun ColumnScope.ScoutCardRadioGroup(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Spacer(Modifier.height(12.dp))
    Column(modifier = modifier.fillMaxWidth()) {
        ScoutLabel(label = label, active = selectedOption.isNotEmpty())

        Spacer(Modifier.height(4.dp)) // to take into consideration the default textfield's gap

        Row(
            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.smallMedium),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption

                val transition = updateTransition(
                    targetState = isSelected,
                    label = "CardSelectTransition"
                )

                val borderWidth by transition.animateDp(label = "BorderWidth") { selected ->
                    if (selected) 2.dp else 1.dp
                }

                val borderColor by transition.animateColor(label = "BorderColor") { selected ->
                    if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(borderWidth, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 56.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onOptionSelected(if (isSelected) "" else option) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = borderColor
                        )
                    }
                }
            }
        }
    }
}
