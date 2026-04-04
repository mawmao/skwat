package com.humayapp.scout.feature.form.impl.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.main.MainSectionViewModel
import com.humayapp.scout.feature.main.ui.TaskCard

enum class TaskStatus {
    PENDING, COLLECTED, APPROVED, REJECTED
}

fun String.toTaskStatus(): TaskStatus = when (lowercase()) {
    "pending" -> TaskStatus.PENDING
    "collected", "completed" -> TaskStatus.COLLECTED
    "approved" -> TaskStatus.APPROVED
    "rejected", "cancelled" -> TaskStatus.REJECTED
    else -> TaskStatus.PENDING
}


fun getTasksByStatus(tasks: List<CollectionTask>, status: TaskStatus): List<CollectionTask> {
    return tasks.filter {
        it.status.toTaskStatus() == status
    }
}

@Composable
fun TaskListScreen(
    vm: MainSectionViewModel,
    status: TaskStatus,
    onTaskClick: (CollectionTask) -> Unit
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val tasks = remember(uiState.tasks, status) {
        uiState.tasks.filter { it.status.toTaskStatus() == status }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = ScoutTheme.spacing.mediumLarge)
            .padding(horizontal = ScoutTheme.margin),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks) { task ->
            TaskCard(
                task = task,
                onClick = { onTaskClick(task) }
            )
        }
    }
}
