package com.humayapp.scout.core.ui.util

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import com.humayapp.scout.core.ui.component.ScoutErrorDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoutErrorEvent(
    errorMessage: String?,
    onDismiss: () -> Unit,
) {
    val imeVisible = WindowInsets.isImeVisible
    val showDialog = retain { mutableStateOf(false) }

    LaunchedEffect(imeVisible, errorMessage) {
        if (!imeVisible && errorMessage != null) {
            showDialog.value = true
        }
    }

    if (showDialog.value && errorMessage != null) {
        ScoutErrorDialog(
            title = "Error!",
            message = errorMessage,
            onDismissRequest = onDismiss
        )
    }
}


@Composable
fun <Event> ScoutUiEvents(
    eventFlow: Flow<Event>,
    onEvent: suspend (Event) -> Unit,
) {
    LaunchedEffect(eventFlow) {
        eventFlow.collectLatest(onEvent)
    }
}