package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.theme.ScoutTypography
import com.humayapp.scout.core.ui.util.scoutClickable

@Composable
fun ScoutDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {

    val shape = ScoutTheme.shapes.cornerMediumLarge

    Dialog(
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
        ),
        onDismissRequest = onDismissRequest
    ) {
        Surface(modifier = modifier, shape = shape) {
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
fun ScoutAlertDialog(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    message: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ScoutDialog(modifier = modifier, onDismissRequest = onDismissRequest) {
        Column(horizontalAlignment = Alignment.Start) {
            icon()
            Column(
                modifier = Modifier.padding(horizontal = ScoutTheme.spacing.large),
            ) {
                title()
                Spacer(modifier = Modifier.height(ScoutTheme.spacing.smallMedium))
                message()
            }
            Spacer(modifier = Modifier.height(ScoutTheme.spacing.largeExtraLarge))
            HorizontalDivider(color = ScoutTheme.material.colorScheme.secondary)
            ScoutDialogButton(text = "OK", onClick = onDismissRequest)
        }
    }
}


@Composable
fun ScoutErrorDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    onDismissRequest: () -> Unit
) {
    ScoutAlertDialog(
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .size(42.dp),
                painter = painterResource(ScoutIcons.Error),
                contentDescription = "$title Icon",
                tint = ScoutTheme.extras.colors.danger,
            )
        },
        title = {
            Text(
                text = title,
                style = ScoutTypography.headlineSmall,
                color = ScoutTheme.extras.colors.danger
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
    Text(
        text = text,
        textAlign = TextAlign.Center,
        style = ScoutTheme.material.typography.labelMedium,
        color = ScoutTheme.material.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .scoutClickable(onClick = onClick)
            .padding(vertical = 16.dp)
    )
}


@Composable
fun ScoutConfirmDialog(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    title: String = "Confirm",
    message: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    ScoutDialog(
        modifier = modifier,
        isVisible = isVisible,
        onDismiss = {
            onDismissRequest()
        },
        content = {
            Column(
                modifier = Modifier.padding(top = ScoutTheme.spacing.large, bottom = ScoutTheme.spacing.small),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = ScoutTheme.spacing.large),
                ) {
                    Text(
                        text = title,
                        style = ScoutTypography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(ScoutTheme.spacing.mediumLarge))
                    Text(
                        text = message,
                        style = ScoutTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(ScoutTheme.spacing.small))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ScoutDialogButton(
                        modifier = Modifier.weight(1F),
                        text = "Cancel",
                        onClick = onDismissRequest
                    )
                    ScoutDialogButton(modifier = Modifier.weight(1F), text = "OK", onClick = onConfirm)
                }
            }
        },
    )
}

@Composable
fun ScoutConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    confirmText: String = "Yes, Cancel",
    cancelText: String = "Keep Editing",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ScoutDialog(
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(ScoutTheme.spacing.large)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
            ) {
                Text(
                    text = title,
                    style = ScoutTheme.material.typography.headlineSmall,
                    color = ScoutTheme.extras.colors.mutedOnBackground
                )
            }
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(ScoutTheme.spacing.medium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoutTextButton(text = confirmText, onClick = onConfirm)
                ScoutTextButton(text = cancelText, onClick = onDismiss)
            }
        }

    }
}