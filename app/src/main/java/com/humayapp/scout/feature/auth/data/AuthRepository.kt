package com.humayapp.scout.feature.auth.data

import android.util.Log
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.model.AuthResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class AuthRepository(
    private val supabase: SupabaseClient,
    private val store: SessionDataStore,
    private val offlineStore: OfflineAuthDataStore,
    private val networkMonitor: NetworkMonitor,
) {
    private val _isLoggingOut = MutableStateFlow(false)

    val authState: Flow<ScoutAuthState> = combine(
        store.sessionFlow,
        offlineStore.offlineUserFlow,
        networkMonitor.isOnline,
        offlineStore.hasOfflineCredentials
    ) { onlineSession, offlineUser, isOnline, hasOffline ->

        if (onlineSession != null) {
            return@combine resolveAuthState(onlineSession, isOnline, hasOffline)
        }

        if (!isOnline && offlineUser != null) {
            val dummySession = UserSession(
                accessToken = "",
                refreshToken = "",
                expiresIn = 0,
                tokenType = "offline",
                user = UserInfo(
                    id = offlineUser.id,
                    email = offlineUser.email,
                    userMetadata = offlineUser.userMetadata,
                    aud = "authenticated"
                ),
                expiresAt = Clock.System.now() + 365.days
            )
            return@combine ScoutAuthState.AuthenticatedOffline(dummySession)
        }

        ScoutAuthState.Unauthenticated
    }
        .distinctUntilChanged()
        .onEach {
            logAuthStateTransition(it)
        }

    suspend fun getCurrentUserId(): String? {
        return when (val currentState = authState.first()) {
            is ScoutAuthState.AuthenticatedOnline -> currentState.session.user?.id
            is ScoutAuthState.AuthenticatedOffline -> currentState.session?.user?.id
            is ScoutAuthState.SessionExpired -> currentState.session?.user?.id
            else -> null
        }
    }


    suspend fun login(email: String, password: String): AuthResult {
        val isOnline = networkMonitor.isOnline.first()

        Log.i(LOG_TAG, "[Auth] Login attempt detected.")

        val result = if (isOnline) {
            Log.i(LOG_TAG, "    Network is online. Attempting online login for $email.")
            loginOnline(email, password)
        } else {
            Log.i(LOG_TAG, "    Network is offline. Attempting offline login.")
            loginOffline(password)
        }
        return result
    }

    private suspend fun loginOnline(email: String, password: String): AuthResult = try {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        Log.v(LOG_TAG, "    Trying to sign in with email $email.")

        val session = supabase.auth.currentSessionOrNull() ?: error("Session not established")

        Log.v(LOG_TAG, "    Session acquired for user: ${session.user?.id}.")

        session.user?.let { user ->
            offlineStore.saveOfflineCredentials(user.id, password)
            offlineStore.saveOfflineUser(
                OfflineUser(
                    id = user.id,
                    email = user.email.orEmpty(),
                    firstName = user.userMetadata?.get("first_name")?.jsonPrimitive?.contentOrNull,
                    lastName = user.userMetadata?.get("last_name")?.jsonPrimitive?.contentOrNull,
                    userMetadata = user.userMetadata
                )
            )

            Log.v(LOG_TAG, "    Setting offline session.")
            Log.v(LOG_TAG, "    Offline session set.")
            Log.i(LOG_TAG, "    Online login success for user: ${user.id}.")
        }

        store.saveSession(session)
        AuthResult.Success

    } catch (e: Exception) {
        Log.w(LOG_TAG, "[Auth] Online login failed: ${e.message}")
        handleLoginError(e, password)
    }


    private suspend fun loginOffline(password: String): AuthResult {

        val lastUserId = offlineStore.getLastUserId()
        if (lastUserId == null) {
            Log.w(LOG_TAG, "[Auth] No stored user for offline login.")
            return AuthResult.InvalidCredentials()
        }

        return if (offlineStore.verifyPassword(lastUserId, password)) {
            Log.i(LOG_TAG, "[Auth] Offline login success.")
            AuthResult.Success
        } else {
            Log.w(LOG_TAG, "[Auth] Offline login failed.")
            AuthResult.InvalidCredentials()
        }
    }

    suspend fun logout() = withContext(NonCancellable) {
        Log.i(LOG_TAG, "[Auth] Logout attempt detected.")

        _isLoggingOut.value = true
//        _offlineSessionFlow.value = null

        runCatching {
            val isOnline = networkMonitor.isOnline.first()

            if (isOnline) {
                try {
                    supabase.auth.signOut()
                } catch (e: Exception) {
                    Log.w(LOG_TAG, "[Auth] Online logout failed: ${e.message}")
                }
            } else {
                Log.i(LOG_TAG, "[Auth] Offline logout: skipping online sign-out")
            }

            Log.v(LOG_TAG, "    Clearing online and offline sessions.")
            supabase.auth.clearSession()
            Log.v(LOG_TAG, "    Offline and online sessions cleared.")
            store.clearSession()
        }

        _isLoggingOut.value = false
        Log.i(LOG_TAG, "    User logged out successfully.")
    }

    suspend fun restoreSession() {
        if (_isLoggingOut.value) return

        val session = store.sessionFlow.firstOrNull() ?: return

        if (Clock.System.now() >= session.expiresAt) {
            store.clearSession()
            return
        }

        supabase.auth.importSession(session)
        Log.d(LOG_TAG, "[Auth] Session restored: ${session.user?.email}")
    }

    private suspend fun handleLoginError(e: Exception, password: String): AuthResult {
        return when (e) {
            is AuthRestException -> AuthResult.InvalidCredentials()
            is HttpRequestTimeoutException -> AuthResult.Timeout()
            is HttpRequestException -> {
                val lastUserId = offlineStore.getLastUserId() ?: return AuthResult.InvalidCredentials()
                if (offlineStore.verifyPassword(lastUserId, password)) {
                    Log.i(LOG_TAG, "[Auth] Network failed, but offline hash matched.")
                    AuthResult.Success
                } else {
                    AuthResult.NoConnection()
                }
            }

            else -> AuthResult.Unknown()
        }
    }


    private fun logAuthStateTransition(state: ScoutAuthState) {
        val details = when (state) {
            is ScoutAuthState.AuthenticatedOnline -> "Online (User: ${state.session.user?.email})"
            is ScoutAuthState.AuthenticatedOffline -> "Offline (User: ${state.session?.user?.email})"
            is ScoutAuthState.SessionExpired -> "Expired (User: ${state.session?.user?.email})"
            else -> "Unauthenticated"
        }
        Log.i(LOG_TAG, "[Auth] Authentication state changed to $details.")
    }

    companion object {
        private const val LOG_TAG = "Scout: NewAuthRepository"
    }

}

sealed class ScoutAuthState {
    object Initializing : ScoutAuthState()
    object Unauthenticated : ScoutAuthState()
    data class AuthenticatedOnline(val session: UserSession) : ScoutAuthState()
    data class AuthenticatedOffline(val session: UserSession?) : ScoutAuthState()
    data class SessionExpired(val session: UserSession?) : ScoutAuthState()
}


fun resolveAuthState(
    session: UserSession?,
    isOnline: Boolean,
    hasOffline: Boolean
): ScoutAuthState {
    val now = Clock.System.now()
    val isExpired = session?.let { now >= it.expiresAt } ?: true
    return when {
        session == null -> ScoutAuthState.Unauthenticated
        isExpired -> ScoutAuthState.SessionExpired(session)
        isOnline -> ScoutAuthState.AuthenticatedOnline(session)
        hasOffline -> ScoutAuthState.AuthenticatedOffline(session)
        else -> ScoutAuthState.Unauthenticated
    }
}

