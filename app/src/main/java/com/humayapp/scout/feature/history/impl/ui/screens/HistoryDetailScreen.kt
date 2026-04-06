package com.humayapp.scout.feature.history.impl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.LocalScoutAppState
import com.humayapp.scout.core.database.model.SyncStatus
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.component.ImageBox
import com.humayapp.scout.core.ui.component.ScoutLabel
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.ScoutUiEvents
import com.humayapp.scout.feature.form.impl.model.getOrEmpty
import com.humayapp.scout.feature.form.impl.ui.components.FormFieldData
import com.humayapp.scout.feature.history.impl.ui.components.HistoryDetailTopAppBar

//@Composable
//fun HistoryDetailScreen(
//    vm: HistoryDetailViewModel,
//    modifier: Modifier = Modifier
//) {
//    val uiState by vm.uiState.collectAsStateWithLifecycle()
//    val syncState by vm.syncState.collectAsStateWithLifecycle()
//    val rootNavigator = LocalRootStackNavigator.current
//    val appState = LocalScoutAppState.current
//
//    ScoutUiEvents(vm.uiEvent) { event ->
//        when (event) {
//            is HistoryDetailUiEvent.SyncStarted -> {
//                appState.showSnackbar("Sync started for this entry")
//            }
//            is HistoryDetailUiEvent.SyncFailed -> {
//                appState.showSnackbar("Sync failed: ${event.message}")
//            }
//        }
//    }
//
//    when (val uiState = uiState) {
//        is HistoryDetailUiState.Loading -> {
//            Box(modifier = Modifier.fillMaxSize()) {
//                CircularProgressIndicator(Modifier.align(Alignment.Center))
//            }
//        }
//        is HistoryDetailUiState.Ready -> {
//            HistoryDetailScreen(
//                modifier = modifier,
//                uiState = uiState,
//                onBack = rootNavigator::pop,
//                onSync = vm::syncEntry,
//                syncState = syncState
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun HistoryDetailScreen(
//    modifier: Modifier = Modifier,
//    uiState: HistoryDetailUiState.Ready,
//    onBack: () -> Unit,
//    onSync: () -> Unit,
//    syncState: SyncState,
//) {
//    val isSyncing = syncState is SyncState.Loading
//    val isSynced = uiState.syncStatus == SyncStatus.SYNCED
//
//
//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//        topBar = {
//            HistoryDetailTopAppBar(
//                onBack = onBack,
//                onSync = onSync,
//                isSyncing = isSyncing,
//                isSynced = isSynced
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = ScoutTheme.margin)
//                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//        ) {
//            HistoryDetailScreenHeader(title = uiState.formType.label)
//            when (uiState.syncStatus) {
//                SyncStatus.PENDING -> {
//                    Text(
//                        text = "Not synced",
//                        style = ScoutTheme.material.typography.bodySmall,
//                        color = ScoutTheme.extras.colors.danger,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                }
//                SyncStatus.SYNCED -> {
//                    Text(
//                        text = "Synced: ${uiState.syncedAt}",
//                        style = ScoutTheme.material.typography.bodySmall,
//                        color = ScoutTheme.material.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                }
//                SyncStatus.DUPLICATE -> {
//                    Text(
//                        text = "Duplicate (already approved on server)",
//                        style = ScoutTheme.material.typography.bodySmall,
//                        color = ScoutTheme.extras.colors.warning,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                }
//            }
//
//            uiState.formType.entries.fastForEach { entry ->
//                FormFieldData(
//                    fields = entry.fields,
//                    getAnswer = { key -> uiState.fieldData.getOrEmpty(key) },
//                    images = uiState.images
//                ) { field, entity, aspect, modifier ->
//                    Column(modifier = modifier) {
//                        ScoutLabel(label = field.label, enableHorizontalPadding = false)
//                        ImageBox(
//                            uri = entity?.localPath?.toUri(),
//                            aspectRatio = aspect,
//                            modifier = Modifier
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun HistoryDetailScreenHeader(title: String) {
//    Text(
//        title,
//        style = ScoutTheme.material.typography.headlineMedium,
//        fontWeight = FontWeight.Medium,
//        color = ScoutTheme.material.colorScheme.onSurface,
//    )
////    Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
//}