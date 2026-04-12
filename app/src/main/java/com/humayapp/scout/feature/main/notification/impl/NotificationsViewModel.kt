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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: unreachable("can never be null. if null, then bug wahaha.")
            notificationRepository.getLocalNotifications(userId).collect { local ->
                _notifications.value = local
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: unreachable("can never be null. if null, then bug wahaha.")
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
