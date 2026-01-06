package com.humayapp.scout.feature.auth.login.impl

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humayapp.scout.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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

    val sessionStatus = authRepository.sessionStatus

    val emailState = TextFieldState()
    val passwordState = TextFieldState()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateLoggingIn(value: Boolean) = _uiState.update { it.copy(isLoggingIn = value) }
    fun updateRecoveringPassword(value: Boolean) = _uiState.update { it.copy(isRecoveringPassword = value) }

    fun onAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.LoginClick -> onLoginClick()
            is LoginUiAction.ClearError -> clearError()
        }
    }

    fun clearFields() {
        emailState.clearText()
        passwordState.clearText()
    }

    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun onLoginClick() {
        viewModelScope.launch {
            updateLoggingIn(true)

            val email = emailState.text.toString()
            val password = passwordState.text.toString()
            val result = authRepository.signIn(email, password)

            updateLoggingIn(false)

            result.onSuccess {
                clearFields()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = getSignInError(err)) }
            }
        }
    }

    private fun getSignInError(error: Throwable): String {
        return when (error) {
            is AuthRestException -> "Invalid email or password."
            is HttpRequestTimeoutException -> "Network timed out. Try again."
            is HttpRequestException -> "Check your internet connection."
            else -> "Something went wrong. Please try again."
        }
    }
}

data class LoginUiState(
    val isLoggingIn: Boolean = false,
    val isRecoveringPassword: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginUiAction {
    object LoginClick : LoginUiAction()
    object ClearError : LoginUiAction()
}
