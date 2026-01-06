package com.humayapp.scout.feature.auth.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}

class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            supabaseClient.auth.signOut()
            supabaseClient.auth.clearSession()
        }.recoverCatching { error ->
            // still accept sign out even there is no internet connection
            if (error is HttpRequestException || error is HttpRequestTimeoutException) {
                supabaseClient.auth.clearSession()
            } else {
                throw error
            }
        }
    }
}

