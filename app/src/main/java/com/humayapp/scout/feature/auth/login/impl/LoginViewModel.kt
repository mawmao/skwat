package com.humayapp.scout.feature.auth.login.impl

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Log.d(LOG_TAG,  "LoginViewModel cleared")
    }

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

data class LoginUiState(
    val isLoggingIn: Boolean = false,
    val isRecoveringPassword: Boolean = false,
)

private const val LOG_TAG = "Scout: LoginViewModel"
