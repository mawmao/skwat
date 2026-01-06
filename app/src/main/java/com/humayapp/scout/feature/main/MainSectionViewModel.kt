package com.humayapp.scout.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class MainSectionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainSectionUiState())
    val uiState: StateFlow<MainSectionUiState> = _uiState.asStateFlow()

    private val _events = Channel<MainSectionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun toggleSettingsDialog(value: Boolean) = _uiState.update { it.copy(isSettingsShown = value) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun onLogout() {
        viewModelScope.launch {
            val result = authRepository.signOut()
            result.onSuccess {
                _events.send(MainSectionEvent.SignOutSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = (getSignOutError(error))) }
            }
        }
    }

    private fun getSignOutError(error: Throwable): String {
        return when (error) {
            is AuthRestException -> "Authentication failed. Please check your credentials or try again."
            is HttpRequestTimeoutException -> "Network timed out. Please try again."
            is HttpRequestException -> "Unable to connect. Check your internet connection."
            else -> "Something went wrong. Please try again."
        }
    }
}

sealed class MainSectionEvent {
    object SignOutSuccess : MainSectionEvent()
}

data class MainSectionUiState(
    val errorMessage: String? = null,
    val isSettingsShown: Boolean = false
)

