package com.humayapp.scout.feature.auth.data

import android.util.Log
import com.humayapp.scout.core.system.NetworkMonitor
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
import kotlinx.coroutines.flow.first

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>

    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun isOnline(): Boolean
    suspend fun getCurrentUserId(): String?
}

class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val networkMonitor: NetworkMonitor,
    private val secureCredentialsRepo: SecureCredentialsRepository
) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    override suspend fun isOnline(): Boolean {
        return networkMonitor.isOnline.first()
    }

    override suspend fun signIn(email: String, password: String): AuthResult = try {
        if (!isOnline()) {
            val (storedEmail, storedPassword) = secureCredentialsRepo.getStoredCredentials()
            return if (email == storedEmail && password == storedPassword) {
                AuthResult.SuccessOffline
            } else {
                AuthResult.InvalidCredentials(message = "No internet connection and stored credentials do not match.")
            }
        }

        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = when (val status = sessionStatus.first()) {
            is SessionStatus.Authenticated -> status.session.user?.id
            else -> throw IllegalStateException("User not authenticated")
        }

        secureCredentialsRepo.saveCredentials(email, password, userId)
        AuthResult.Success
    } catch (e: Throwable) {
        Log.d("Scout: AuthRepository", "Error =", e)

        when (e) {
            is AuthRestException -> AuthResult.InvalidCredentials()
            is HttpRequestTimeoutException -> AuthResult.Timeout()
            is HttpRequestException -> AuthResult.NoConnection()
            else -> AuthResult.Unknown()
        }
    }
    override suspend fun getCurrentUserId(): String? {
        val status = sessionStatus.first()
        if (status is SessionStatus.Authenticated) {
            return status.session.user?.id
        }
        val (_, _, userId) = secureCredentialsRepo.getStoredCredentials()
        return userId
    }

    override suspend fun signOut(): AuthResult = try {
        if (isOnline()) {
            try {
                supabaseClient.auth.signOut()
            } catch (e: Exception) {
                Log.w("Scout: AuthRepository", "Remote signOut failed", e)
            }
        }
        supabaseClient.auth.clearSession()
        secureCredentialsRepo.clearCredentials()
        AuthResult.Success
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

    object SuccessOffline : AuthResult()

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


