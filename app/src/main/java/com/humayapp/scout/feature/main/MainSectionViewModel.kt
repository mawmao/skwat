package com.humayapp.scout.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humayapp.scout.core.data.settings.SettingsRepository
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainSectionUiState())
    val uiState: StateFlow<MainSectionUiState> = _uiState.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError = _uiError.asStateFlow()

    private val _uiEvent = Channel<MainSectionEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: MainSectionAction) {
        Log.d("Scout: MainSectionViewModel", "onAction($action)")
        when (action) {
            is MainSectionAction.LogoutRequest -> onLogout()
            is MainSectionAction.ClearUiError -> _uiError.update { null }
            is MainSectionAction.ToggleSettings -> _uiState.update { it.copy(isSettingsShown = action.isVisible) }
        }
    }

    private fun onLogout() {
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is AuthResult.Success -> _uiEvent.send(MainSectionEvent.LogoutSuccess)
                else -> _uiError.update { result.message }
            }
        }
    }
}

data class MainSectionUiState(
    val isSettingsShown: Boolean = false
)

sealed interface MainSectionAction {
    object LogoutRequest : MainSectionAction
    data class ToggleSettings(val isVisible: Boolean) : MainSectionAction
    object ClearUiError : MainSectionAction
}

sealed class MainSectionEvent {
    object LogoutSuccess : MainSectionEvent()
}
