package com.humayapp.scout.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.data.notification.Notification
import com.humayapp.scout.core.data.notification.NotificationRepository
import com.humayapp.scout.core.database.model.CollectionTaskUiModel
import com.humayapp.scout.core.sync.FormSyncWorker
import com.humayapp.scout.core.sync.SyncManager
import com.humayapp.scout.core.sync.SyncOrchestrator
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.core.system.SnackbarManager
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.ScoutAuthState
import com.humayapp.scout.feature.auth.data.ensureSession
import com.humayapp.scout.feature.auth.model.ScoutUser
import com.humayapp.scout.feature.auth.model.toScoutUser
import com.humayapp.scout.feature.main.data.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.exceptions.HttpRequestException
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@HiltViewModel
class MainSectionViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val notificationRepository: NotificationRepository,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val pollingInterval = 5.seconds

    private val _uiState = MutableStateFlow(MainSectionUiState())
    val uiState: StateFlow<MainSectionUiState> = _uiState.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError = _uiError.asStateFlow()

    private val _uiEvent = Channel<MainSectionEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _currentUser = MutableStateFlow<ScoutUser?>(null)
    val currentUser: StateFlow<ScoutUser?> = _currentUser.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _authState = MutableStateFlow<ScoutAuthState>(ScoutAuthState.Initializing)
    val authState = _authState.asStateFlow()

    init {
        observeNetworkState()
        observeTasks()
        observeAuthState()
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: unreachable("can never be null. if null, then bug wahaha.")
            notificationRepository.getLocalNotifications(userId).collect { local ->
                _notifications.value = local
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState
                .onEach { state ->
                    _authState.value = state
                    val user = when (state) {
                        is ScoutAuthState.AuthenticatedOnline -> state.session.user
                        is ScoutAuthState.AuthenticatedOffline -> state.session?.user
                        else -> null
                    }
                    _currentUser.value = user?.toScoutUser()
                }
                .flatMapLatest { state ->
                    if (state is ScoutAuthState.AuthenticatedOnline) {
                        pollWhenOnline()
                    } else {
                        emptyFlow()
                    }
                }
                .debounce(100.milliseconds)
                .collect()
        }
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            val initialOnline = withTimeoutOrNull(2000L) {
                networkMonitor.isOnline.first { it }
            } ?: run {
                false
            }

            _isOnline.value = initialOnline

            networkMonitor.isOnline.collect { online ->
                _isOnline.value = online
            }
        }
    }

    private fun observeTasks() {
        collectionRepository.observeTasks()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { tasks ->
                _uiState.update {
                    it.copy(tasks = tasks, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }


    fun refreshTasks() {
        viewModelScope.launch {
            if (!_isOnline.value) {
                _uiError.update { "No internet connection. Cannot refresh tasks." }
                return@launch
            }

            if (_authState.value == ScoutAuthState.Unauthenticated) {
                _uiError.update { "You are not logged in. Please sign in again." }
                return@launch
            }

            _uiState.update { it.copy(isRefreshing = true) }

            try {
                syncManager.syncNow()
                FormSyncWorker.startUpSyncWork()
                delay(300)
                snackbarManager.show("Refresh successful")
            } catch (e: Exception) {
                Log.e("Scout: MainSectionViewModel", "Refresh error:", e)
                _uiError.update { e.message ?: "Failed to refresh tasks" }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onAction(action: MainSectionAction) {
        when (action) {
            is MainSectionAction.LogoutRequest -> viewModelScope.launch {
                authRepository.logout()
                _uiEvent.send(MainSectionEvent.LogoutSuccess)
            }

            is MainSectionAction.ClearUiError -> _uiError.update { null }
            is MainSectionAction.ToggleProfile -> _uiState.update { it.copy(isProfileShown = action.isVisible) }
        }
    }

    private fun pollWhenOnline(): Flow<Unit> = flow {
        pullTaskAndNotifications()
        while (true) {
            delay(pollingInterval)
            if (networkMonitor.isOnline.first()) {
                pullTaskAndNotifications()
            }
        }
    }

    private suspend fun pullTaskAndNotifications() {
        try {
            ensureSession(onSessionExpired = ::handleSessionExpired) {
                syncManager.syncNow()
            }
        } catch (e: CancellationException) {
            Log.w(LOG_TAG, "[Poll] Polling cancelled because user left or app backgrounded.")
            throw e
        } catch (e: HttpRequestException) {
            Log.w(LOG_TAG, "[Poll] Network error during polling: ${e.message}")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Poll] Unexpected polling failure", e)
        }
    }

    private suspend fun handleSessionExpired() {
        Log.d(LOG_TAG, "    Handling session expiration. Logging out user. Emitting event to UI.")

        authRepository.logout()
        withContext(NonCancellable) {
            _uiEvent.send(MainSectionEvent.SessionExpired)
        }
    }

    companion object {
        private const val LOG_TAG = "Scout: MainSectionViewModel"
    }
}

data class MainSectionUiState(
    val isProfileShown: Boolean = false,
    val tasks: List<CollectionTaskUiModel> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = true
)

sealed interface MainSectionAction {
    object LogoutRequest : MainSectionAction
    data class ToggleProfile(val isVisible: Boolean) : MainSectionAction
    object ClearUiError : MainSectionAction
}

sealed class MainSectionEvent {
    object LogoutSuccess : MainSectionEvent()
    object SessionExpired : MainSectionEvent()
}
