package com.humayapp.scout.feature.main.notification.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.data.notification.Notification
import com.humayapp.scout.core.data.notification.NotificationRepository
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.ScoutAuthState
import com.humayapp.scout.feature.auth.data.ensureSession
import com.humayapp.scout.feature.auth.model.ScoutUser
import com.humayapp.scout.feature.auth.model.toScoutUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.exceptions.HttpRequestException
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val pollingInterval = 15.seconds

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    private val _authState = MutableStateFlow<ScoutAuthState>(ScoutAuthState.Initializing)
    private val _currentUser = MutableStateFlow<ScoutUser?>(null)

    init {
        observeNetworkState()
        observeAuthState()

        viewModelScope.launch {
            val userId = _currentUser.value?.id
            if (userId != null) {
                notificationRepository.getLocalNotifications(userId).collect { local ->
                    _notifications.value = local
                }
            }
        }
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.onEach { state ->
                _authState.value = state
                val user = when (state) {
                    is ScoutAuthState.AuthenticatedOffline -> state.session?.user
                    is ScoutAuthState.SessionExpired -> state.session?.user
                    else -> null
                }
                _currentUser.value = user?.toScoutUser()
            }.flatMapLatest { state ->
                if (state is ScoutAuthState.AuthenticatedOnline) {
                    pollWhenOnline(state.session.user?.id ?: unreachable("should always have an id"))
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

    private fun pollWhenOnline(userId: String): Flow<Unit> = flow {
        pullNotifications(userId)
        while (true) {
            delay(pollingInterval)
            if (networkMonitor.isOnline.first()) {
                pullNotifications(userId)
            }
        }
    }

    private suspend fun pullNotifications(userId: String) {
        try {
            ensureSession(onSessionExpired = authRepository::logout) {
                notificationRepository.pullNotifications(userId)
            }
            Log.d(LOG_TAG, "Polling: tasks pulled successfully")
        } catch (e: CancellationException) {
            Log.w(LOG_TAG, "[Poll] Polling cancelled because user left or app backgrounded.")
            throw e
        } catch (e: HttpRequestException) {
            Log.w(LOG_TAG, "[Poll] Network error during polling: ${e.message}")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "[Poll] Unexpected polling failure", e)
        }
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    companion object {
        private const val LOG_TAG = "Scout: NotificationsViewModel"
    }
}
