package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InputError(
    modifier: Modifier = Modifier,
    errorMessage: String,
) {
    Box(
        Modifier
            .wrapContentHeight()
            .padding(start = 8.dp, top = 8.dp, end = 0.dp, bottom = 0.dp)
    ) {
        Text(
            modifier = modifier,
            text = errorMessage, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onError
        )
    }
}