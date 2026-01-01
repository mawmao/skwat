package com.humayapp.scout.feature.auth.login.impl

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class LoginUiState(
    val isLoggingIn: Boolean = false,
    val isRecoveringPassword: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    val emailState = TextFieldState()
    val passwordState = TextFieldState()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateLoggingIn(value: Boolean) = _uiState.update { it.copy(isLoggingIn = value) }
    fun updateRecoveringPassword(value: Boolean) = _uiState.update { it.copy(isRecoveringPassword = value) }

    fun clearFields() {
        emailState.clearText()
        passwordState.clearText()
    }
}