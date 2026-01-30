package com.humayapp.scout.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.ui.theme.ScoutTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutBottomSheet(
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(ScoutTheme.spacing.smallMedium),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val margin = ScoutTheme.margin
    val bottomPadding = ScoutTheme.spacing.large

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = dragHandle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = margin)
                .padding(bottom = bottomPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            content()
        }
    }
}