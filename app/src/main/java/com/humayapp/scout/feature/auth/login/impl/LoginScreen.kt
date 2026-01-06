package com.humayapp.scout.feature.auth.login.impl

import android.R.id.message
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.navigation.LocalStackNavigator
import com.humayapp.scout.core.ui.common.ConnectivityIndicator
import com.humayapp.scout.core.ui.common.ScoutRegion
import com.humayapp.scout.core.ui.component.ScoutErrorDialog
import com.humayapp.scout.core.ui.component.ScoutLoadingButton
import com.humayapp.scout.core.ui.component.ScoutLogo
import com.humayapp.scout.core.ui.component.ScoutSecureTextField
import com.humayapp.scout.core.ui.component.ScoutTextButton
import com.humayapp.scout.core.ui.component.ScoutTextField
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.rememberFocusRequester
import com.humayapp.scout.feature.auth.recovery.api.navigation.navigateToRecoveryOtp
import com.humayapp.scout.navigation.navigateToMain
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vm: LoginViewModel = hiltViewModel(),
) {

    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val imeVisible = WindowInsets.isImeVisible
    var authSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val rootNavigator = LocalRootStackNavigator.current
    val authNavigator = LocalStackNavigator.current

    if (showError) {
        ScoutErrorDialog(
            title = "Error!",
            message = uiState.errorMessage!!,
            onDismissRequest = {
                vm.onAction(LoginUiAction.ClearError)
                showError = false
            }
        )
    }

    LaunchedEffect(Unit) {
        vm.sessionStatus.collectLatest { sessionStatus ->
            if (sessionStatus is SessionStatus.Authenticated && sessionStatus.source is SessionSource.SignIn) {
                authSuccess = true
            } else if (sessionStatus is SessionStatus.NotAuthenticated && sessionStatus.isSignOut) {
                // show snackbar to show logged out successfully
            }
        }
    }

    LaunchedEffect(imeVisible, authSuccess, uiState.errorMessage) {
        if (!imeVisible && authSuccess) {
            rootNavigator.navigateToMain()
        }
        if (!imeVisible && uiState.errorMessage != null) {
            showError = true
        }
    }

    LoginScreenContent(
        modifier = modifier,
        emailState = vm.emailState,
        passwordState = vm.passwordState,
        uiState = uiState,
        onLogin = { vm.onAction(LoginUiAction.LoginClick) },
        onForgotPassword = authNavigator::navigateToRecoveryOtp
    )
}


@Composable
private fun LoginScreenContent(
    modifier: Modifier,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    uiState: LoginUiState,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = ScoutTheme.margin),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScoutRegion(modifier = Modifier.padding(top = 24.dp))
        ScoutLogo(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        )
        LoginForm(
            emailState = emailState,
            passwordState = passwordState,
            uiState = uiState,
            onLogin = onLogin,
            onForgotPassword = onForgotPassword
        )
        ConnectivityIndicator(
            isOffline = false,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .weight(1F),
        )
    }
}


@Composable
private fun LoginForm(
    emailState: TextFieldState,
    passwordState: TextFieldState,
    uiState: LoginUiState,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = rememberFocusRequester()
    val passwordFocusRequester = rememberFocusRequester()

    Column {
        ScoutTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocusRequester),
            state = emailState,
            label = "Email",
            enabled = !uiState.isLoggingIn,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Spacer(modifier = Modifier.height(ScoutTheme.spacing.small))
        ScoutSecureTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            state = passwordState,
            label = "Password",
            enabled = !uiState.isLoggingIn,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {}
        )
        Spacer(modifier = Modifier.height(ScoutTheme.spacing.large))
        ScoutLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Login",
            isLoading = uiState.isLoggingIn,
            onClick = {
                when {
                    emailState.text.isEmpty() -> emailFocusRequester.requestFocus()
                    passwordState.text.isEmpty() -> passwordFocusRequester.requestFocus()
                    else -> {
                        onLogin()
                        focusManager.clearFocus()
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(ScoutTheme.spacing.large))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            // TODO: could also disable when logging in
            ScoutTextButton(
                text = "Forgot Password?",
                style = ScoutTheme.material.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    color = ScoutTheme.extras.colors.mutedOnBackground
                ),
                isLoading = uiState.isRecoveringPassword,
                onClick = onForgotPassword
            )
        }
    }
}