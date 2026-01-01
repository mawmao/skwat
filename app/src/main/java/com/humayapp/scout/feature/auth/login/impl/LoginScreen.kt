package com.humayapp.scout.feature.auth.login.impl

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
import com.humayapp.scout.core.ui.component.ScoutButton
import com.humayapp.scout.core.ui.component.ScoutLogo
import com.humayapp.scout.core.ui.component.ScoutSecureTextField
import com.humayapp.scout.core.ui.component.ScoutTextButton
import com.humayapp.scout.core.ui.component.ScoutTextField
import com.humayapp.scout.core.ui.rememberFocusRequester
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.auth.recovery.api.navigation.navigateToRecoveryOtp
import com.humayapp.scout.navigation.navigateToMain

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imeVisible = WindowInsets.isImeVisible

    val rootNavigator = LocalRootStackNavigator.current
    val authNavigator = LocalStackNavigator.current

    // NOTE: only do navigation after keyboard is completely hidden
    LaunchedEffect(imeVisible, uiState.isLoggingIn) {
        if (!imeVisible && uiState.isLoggingIn) {
            viewModel.updateLoggingIn(false)
            rootNavigator.navigateToMain()
        }
    }

    LoginScreenContent(
        modifier = modifier,
        emailState = viewModel.emailState,
        passwordState = viewModel.passwordState,
        uiState = uiState,
        onLogin = {
            viewModel.updateLoggingIn(true)
            viewModel.clearFields()
        },
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
        ScoutButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Login",
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
                style = ScoutTheme.material.typography.labelLarge.copy(fontSize = 14.sp, color = ScoutTheme.extras.colors.mutedOnBackground),
                isLoading = uiState.isRecoveringPassword,
                onClick = onForgotPassword
            )
        }
    }
}