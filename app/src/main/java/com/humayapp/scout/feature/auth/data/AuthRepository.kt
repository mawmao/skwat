package com.humayapp.scout.feature.auth.data

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>

    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signOut(): AuthResult
}

class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    override suspend fun signIn(email: String, password: String): AuthResult = try {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        AuthResult.Success // return
    } catch (e: Throwable) {
        Log.d("Scout: AuthRepository", "Error =", e)

        when (e) {
            is AuthRestException -> AuthResult.InvalidCredentials()
            is HttpRequestTimeoutException -> AuthResult.Timeout()
            is HttpRequestException -> AuthResult.NoConnection()
            else -> AuthResult.Unknown()
        }
    }

    override suspend fun signOut(): AuthResult = try {
        supabaseClient.auth.signOut()
        supabaseClient.auth.clearSession()

        AuthResult.Success // return
    } catch (e: Throwable) {
        when (e) {
            is AuthRestException -> AuthResult.InvalidCredentials()
            is HttpRequestTimeoutException -> AuthResult.Timeout()
            is HttpRequestException -> AuthResult.NoConnection()
            else -> AuthResult.Unknown()
        }
    }
}

sealed class AuthResult(open val message: String = "") {

    object Success : AuthResult()

    data class InvalidCredentials(
        override val message: String = "Authentication failed. Please try again."
    ) : AuthResult(message)

    data class Timeout(
        override val message: String = "Network timed out. Try again."
    ) : AuthResult(message)

    data class NoConnection(
        override val message: String = "Check your internet connection."
    ) : AuthResult(message)

    data class Unknown(
        override val message: String = "Something went wrong. Please try again."
    ) : AuthResult(message)
}


