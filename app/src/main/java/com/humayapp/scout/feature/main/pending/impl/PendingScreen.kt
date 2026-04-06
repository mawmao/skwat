package com.humayapp.scout.feature.main.pending.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.core.ui.common.EmptyState
import com.humayapp.scout.core.ui.component.ScoutConfirmDialog
import com.humayapp.scout.core.ui.component.ScoutDialog
import com.humayapp.scout.core.ui.component.ScoutDialogButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.theme.ScoutTypography
import com.humayapp.scout.feature.form.impl.ui.screens.detail.FormDetailsScreen
import com.humayapp.scout.feature.main.MainSectionViewModel
import com.humayapp.scout.feature.main.ui.TaskCard
import com.humayapp.scout.navigation.navigateToDetail
import com.humayapp.scout.navigation.navigateToForms

@Composable
fun PendingScreen(
    modifier: Modifier = Modifier,
    vm: MainSectionViewModel,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val pendingTasks = uiState.tasks.filter { it.status.equals("pending", true) }
    val rootNavigator = LocalRootStackNavigator.current

    var selectedTask by remember { mutableStateOf<CollectionTask?>(null) }
    var showSimpleDialog by remember { mutableStateOf(false) }
    var showRetakeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = ScoutTheme.spacing.mediumLarge)
            .padding(horizontal = ScoutTheme.margin)
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            pendingTasks.isEmpty() -> EmptyState(message = "No pending tasks")
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.smallMedium)
            ) {
                items(pendingTasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = {
                            selectedTask = task
                            if (task.retakeOf != null) {
                                showRetakeDialog = true
                            } else {
                                showSimpleDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    ScoutConfirmDialog(
        isVisible = showSimpleDialog,
        title = "Start Collection",
        message = "Are you ready to collect this form?",
        onDismissRequest = {
            showSimpleDialog = false
            selectedTask = null
        },
        onConfirm = {
            selectedTask?.let { task ->
                rootNavigator.navigateToForms(task.id)  // Pass the ID
            }
            showSimpleDialog = false
            selectedTask = null
        }
    )

    if (showRetakeDialog && selectedTask != null) {
        val originalTask = uiState.tasks.find { it.id == selectedTask?.retakeOf }
        ScoutDialog(
            isVisible = true,
            onDismiss = {
                showRetakeDialog = false
                selectedTask = null
            }
        ) {
            Column(
                modifier = Modifier.padding(top = ScoutTheme.spacing.large, bottom = ScoutTheme.spacing.small),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = ScoutTheme.spacing.large),
                ) {
                    Text(
                        text = "Retake Form",
                        style = ScoutTypography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(ScoutTheme.spacing.mediumLarge))
                    Text(
                        text = "This task is a retake of a previously rejected form.",
                        style = ScoutTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(ScoutTheme.spacing.small))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ScoutDialogButton(
                        modifier = Modifier.weight(1F),
                        text = "View Original",
                        onClick = {
                            showRetakeDialog = false
                            selectedTask = null
                            if (originalTask != null) {
                                rootNavigator.navigateToDetail(collectionTaskId = originalTask.id, activityId = originalTask.activityId)
                            }
                        }
                    )
                    ScoutDialogButton(
                        modifier = Modifier.weight(1F),
                        text = "Collect Anyway",
                        onClick = {
                            showRetakeDialog = false
                            selectedTask?.let { task ->
                                rootNavigator.navigateToForms(task.id)  // Pass the ID
                            }
                            selectedTask = null
                        }
                    )
                }
            }
        }
    }
}
