package com.humayapp.scout.feature.auth.login.impl

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // separated email and password state since they are frequently changing
    val emailState = TextFieldState()
    val passwordState = TextFieldState()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError = _uiError.asStateFlow()

    private val _uiEvent = Channel<LoginUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun updateLoggingIn(value: Boolean) = _uiState.update { it.copy(isLoggingIn = value) }

    fun onAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.LoginRequest -> onLogin()
            is LoginUiAction.ClearUiError -> _uiError.update { null }
        }
    }

    private fun onLogin() {
        viewModelScope.launch {
            updateLoggingIn(true)

            val email = emailState.text.toString()
            val password = passwordState.text.toString()

//            emailState.clearText()
//            passwordState.clearText()
//            _uiEvent.send(LoginUiEvent.LoginSuccess)

            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    emailState.clearText()
                    passwordState.clearText()
                    _uiEvent.send(LoginUiEvent.LoginSuccess)
                }
                else -> _uiError.update { result.message }
            }

            updateLoggingIn(false)

        }
    }
}



data class LoginUiState(
    val isLoggingIn: Boolean = false,
    val isRecoveringPassword: Boolean = false,
)

sealed interface LoginUiAction {
    object LoginRequest : LoginUiAction
    object ClearUiError : LoginUiAction
}

sealed interface LoginUiEvent {
    object LoginSuccess : LoginUiEvent
}
