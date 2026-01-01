package com.humayapp.scout.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.humayapp.scout.core.ui.theme.ScoutIcons
import kotlinx.coroutines.delay

/**
 * TODO: factor out to small composables to keep the main composable lean)
 */


@Composable
fun ScoutDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
        ),
        onDismissRequest = onDismissRequest
    ) {
        Surface(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
            content()
        }
    }
}

// has [onFinished] lambda
@Composable
fun ScoutDialog(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true,
            ),
        ) {
            Surface(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
                content()
            }
        }
    }
}


@Composable
fun ScoutConfirmDialog(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    @DrawableRes icon: Int = ScoutIcons.QuestionMark,
    title: String = "Confirm",
    message: String,
    onDismissRequest: () -> Unit,
) {

    ScoutDialog(
        isVisible = isVisible,
        onDismiss = onDismissRequest,
        content = {
            Column {
                Icon(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .size(42.dp),
                    painter = painterResource(icon),
                    contentDescription = "$title Icon",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth()) {
                    ScoutDialogButton(
                        modifier = Modifier.weight(1F),
                        text = "Cancel",
                        onClick = onDismissRequest
                    )
                    ScoutDialogButton(modifier = Modifier.weight(1F), text = "OK", onClick = onDismissRequest)
                }
            }
        }
    )
}


@Composable
fun ScoutAlertDialog(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: @Composable () -> Unit,
    message: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ScoutDialog(modifier = modifier, onDismissRequest = onDismissRequest) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .size(42.dp),
                imageVector = icon,
                contentDescription = "$title Icon",
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                title()
                Spacer(modifier = Modifier.height(6.dp))
                message()
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            ScoutDialogButton(text = "OK", onClick = onDismissRequest)
        }
    }
}

@Composable
fun ScoutAlertDialog(
    modifier: Modifier = Modifier,
    icon: ImageVector, // change to composable if needed
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
) {
    ScoutAlertDialog(
        modifier = modifier,
        icon = icon,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        message = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        onDismissRequest = onDismissRequest
    )
}


@Composable
fun ScoutDialogButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val ripple = ripple(bounded = true)
    val interactionSource = remember { MutableInteractionSource() }
    var isDismissing by remember { mutableStateOf(false) }

    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            delay(120L)
            onClick()
        }
    }

    Text(
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple,
                enabled = !isDismissing,
                onClick = { if (!isDismissing) isDismissing = true }
            )
            .padding(vertical = 16.dp)
    )
}
