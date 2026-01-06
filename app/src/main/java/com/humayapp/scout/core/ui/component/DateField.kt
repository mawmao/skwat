package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.humayapp.scout.core.ui.theme.ScoutIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


// helper
fun formatDate(millis: Long?): String {
    if (millis == null) return ""
    val zdt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return zdt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
}

fun String?.isoDisplay(): String {
    if (this.isNullOrBlank()) return ""
    val localDate = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    return localDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
}

fun String?.isoToMillis() = this?.let {
    LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

@Composable
fun ScoutDatePickerDialog(
    modifier: Modifier = Modifier,
    selectedDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val initialMillis = selectedDateMillis ?: System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        confirmButton = {
            TextButton(onClick = {
                onConfirm(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {

        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                headlineContentColor = MaterialTheme.colorScheme.onBackground,
                selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                dateTextFieldColors = ScoutTextFieldDefaults.colors()
            ),
        )
    }
}

@Composable
fun ScoutDateField(
    modifier: Modifier = Modifier,
    value: String?,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
) {

    val millisValue = value.isoToMillis() ?: System.currentTimeMillis()
    val display = formatDate(value.isoToMillis())
    var open by remember { mutableStateOf(false) }

    ScoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(value) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        open = true
                    }
                }
            },
        value = display,
        onValueChange = { }, // read-only
        enabled = enabled,
        trailingIcon = trailingIcon ?: {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(ScoutIcons.DateRange),
                contentDescription = "Open date picker"
            )
        },
        label = label,
        readOnly = true
    )

    if (open) {
        ScoutDatePickerDialog(
            selectedDateMillis = millisValue,
            onDismiss = { open = false },
            onConfirm = { millis ->
                val isoValue = Instant.ofEpochMilli(millis ?: return@ScoutDatePickerDialog)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
                onValueChange(isoValue)
                open = false
            }
        )
    }
}
