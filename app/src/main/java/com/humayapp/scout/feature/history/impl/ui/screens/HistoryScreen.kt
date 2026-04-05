package com.humayapp.scout.feature.history.impl.ui.screens

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.SyncStatus
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.util.toRelativeString
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val formRepository: FormRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = formRepository.getAllEntries()
        .map { entries -> HistoryUiState.Ready(formEntries = entries) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_500),
            initialValue = HistoryUiState.Loading
        )

    companion object {
        private const val LOG_TAG = "Scout: HistoryViewModel"
    }
}


sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Ready(val formEntries: List<FormEntryEntity>) : HistoryUiState
}


@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun HistoryScreen(
    modifier: Modifier = Modifier,
    vm: HistoryViewModel = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val rootNavigator = LocalRootStackNavigator.current

    when (val s = uiState) {
        is HistoryUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                )
            }
        }

        is HistoryUiState.Ready -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.smallMedium),
                contentPadding = PaddingValues(ScoutTheme.margin)
            ) {
                items(s.formEntries) { entry ->

                    val vm = hiltViewModel<HistoryDetailViewModel, HistoryDetailViewModel.Factory>(
                        key = "${Uuid.random()}"
                    ) {
                        it.create(entry = entry)
                    }

                    HistoryCardDefault(
                        entry = entry,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
//                            rootNavigator.navigateToDetail {
//                                HistoryDetailScreen(vm)
//                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCardDefault(
    entry: FormEntryEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clip(ScoutTheme.shapes.cornerMedium)
            .clickable(onClick = onClick)
            .border(1.dp, ScoutTheme.material.colorScheme.onSurfaceVariant, ScoutTheme.shapes.cornerMedium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(72.dp),
        ) {
            Icon(
                painter = painterResource(ScoutIcons.DataArray),
                contentDescription = null
            )
            Spacer(Modifier.width(ScoutTheme.spacing.smallMedium))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val label = FormType.fromActivityType(entry.activityType).label
                Text(text = label, style = ScoutTheme.material.typography.titleSmall)
                Text(
                    entry.mfid,
                    style = ScoutTheme.material.typography.bodyMedium,
                    color = ScoutTheme.material.colorScheme.onSurface
                )
            }
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val statusText = when (entry.syncStatus) {
                    SyncStatus.PENDING -> "Not synced"
                    SyncStatus.SYNCED -> "Synced"
                    SyncStatus.DUPLICATE -> "Duplicate"
                }
                val collectedAt = "Collected ${entry.collectedAt.toRelativeString()}"
                Text(text = statusText, style = ScoutTheme.material.typography.bodyMedium)
                Text(
                    text = collectedAt,
                    style = ScoutTheme.material.typography.bodySmall,
                    color = ScoutTheme.material.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
