package com.humayapp.scout.feature.main.collected.impl

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.common.EmptyState
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.ui.screens.detail.FormDetailsScreen
import com.humayapp.scout.feature.main.MainSectionViewModel
import com.humayapp.scout.feature.main.ui.TaskCard
import com.humayapp.scout.navigation.navigateToDetail

@Composable
fun CollectedScreen(
    modifier: Modifier = Modifier,
    vm: MainSectionViewModel,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val collectedTasks = uiState.tasks.filter { it.status == "completed" && it.verificationStatus == "pending" }
    val rootNavigator = LocalRootStackNavigator.current

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

            collectedTasks.isEmpty() -> EmptyState(message = "No tasks awaiting verification")
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.smallMedium)
            ) {
                items(collectedTasks) { task ->
                    TaskCard(task = task, onClick = {
                        Log.d("Scout: CollectedScreen", "Navigating to detail screen - task $task")
                        rootNavigator.navigateToDetail(collectionTaskId = task.id, activityId = task.activityId)
                    })
                }
            }
        }
    }
}
