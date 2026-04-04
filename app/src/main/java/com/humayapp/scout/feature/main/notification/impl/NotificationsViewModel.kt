package com.humayapp.scout.feature.main.notification.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.data.notification.Notification
import com.humayapp.scout.core.data.notification.NotificationRepository
import com.humayapp.scout.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            notificationRepository.startPolling()
            notificationRepository.getLocalNotifications(userId).collect { local ->
                _notifications.value = local
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationRepository.stopPolling()
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }
}
