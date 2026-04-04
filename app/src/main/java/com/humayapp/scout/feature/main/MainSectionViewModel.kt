package com.humayapp.scout.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.network.CollectionTask
import com.humayapp.scout.core.sync.FormSyncWorker
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.User
import com.humayapp.scout.feature.auth.model.AuthResult
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds


@HiltViewModel
class MainSectionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val collectionRepository: CollectionRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private var pollingJob: Job? = null
    private val pollingInterval = 15.seconds

    private val _uiState = MutableStateFlow(MainSectionUiState())
    val uiState: StateFlow<MainSectionUiState> = _uiState.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError = _uiError.asStateFlow()

    private val _uiEvent = Channel<MainSectionEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        observeNetworkState()
        observeAuthState()
        observeTasks()
        startPolling()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _currentUser.value = user
                Log.d("MainSectionVM", "Authenticated user: ${user?.email ?: "none"}")
            }
        }
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            val initialOnline = withTimeoutOrNull(2000L) {
                networkMonitor.isOnline.first { it }
            } ?: run {
                // fallback: use ConnectivityManager directly
                // val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                // cm.activeNetwork != null
                false
            }
            _isOnline.value = initialOnline
            Log.d(LOG_TAG, "Initial network status: $initialOnline")
            // then collect further updates
            networkMonitor.isOnline.collect { online ->
                _isOnline.value = online
                Log.d(LOG_TAG, "Network status: ${if (online) "online" else "offline"}")
            }
        }
    }

    private fun observeTasks() {
        viewModelScope.launch {
            var firstEmission = true
            collectionRepository.getAllCollectionTasks()
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks) }
                    if (firstEmission) {
                        firstEmission = false
                        if (tasks.isNotEmpty()) {
                            _uiState.update { state -> state.copy(isLoading = false) }
                        } else {
                            launch {
                                delay(2000L)
                                _uiState.update { state -> state.copy(isLoading = false) }
                            }
                        }
                    }
                }
        }
    }

    fun refreshTasks() {
        viewModelScope.launch {
            if (!_isOnline.value) {
                _uiError.update { "No internet connection. Cannot refresh tasks." }
                return@launch
            }

            if (!authRepository.isAuthenticated()) {
                _uiError.update { "You are not logged in. Please sign in again." }
                return@launch
            }

            _uiState.update { it.copy(isRefreshing = true) }
            try {
                FormSyncWorker.startUpSyncWork()
                collectionRepository.pullTasksFromSupabaseForCurrentUser()
                delay(300)
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
            is MainSectionAction.LogoutRequest -> onLogout()
            is MainSectionAction.ClearUiError -> _uiError.update { null }
            is MainSectionAction.ToggleProfile -> _uiState.update { it.copy(isProfileShown = action.isVisible) }
        }
    }

    private fun onLogout() {
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is AuthResult.Success, is AuthResult.SuccessOffline -> _uiEvent.send(MainSectionEvent.LogoutSuccess)
                else -> _uiError.update { result.message }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            // listen for network becoming online
            launch {
                _isOnline.collect { isOnline ->
                    if (isOnline && authRepository.isAuthenticated()) {
                        pullTasks()
                    }
                }
            }
            // periodic polling fallback
            while (isActive) {
                delay(pollingInterval)
                if (_isOnline.value && authRepository.isAuthenticated()) {
                    pullTasks()
                }
            }
        }
    }

    private suspend fun pullTasks() {
        try {
            collectionRepository.pullTasksFromSupabaseForCurrentUser()
            Log.d(LOG_TAG, "Polling: tasks pulled successfully")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Polling failed", e)
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    companion object {
        private const val LOG_TAG = "Scout: MainSectionViewModel"
    }
}

data class MainSectionUiState(
    val isProfileShown: Boolean = false,
    val tasks: List<CollectionTask> = emptyList(),
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
}
