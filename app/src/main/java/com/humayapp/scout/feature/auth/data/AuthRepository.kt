package com.humayapp.scout.feature.auth.data

import android.util.Log
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.model.AuthResult
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.time.Instant

@Serializable
data class User(
    val id: String,
    val email: String?,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val role: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("date_of_birth") val dateOfBirth: LocalDate? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("updated_at") val updatedAt: Instant? = null,
    @SerialName("last_sign_in_at") val lastSignInAt: Instant? = null
) {
    val name: String? get() = listOfNotNull(firstName, lastName).joinToString(" ").ifEmpty { null }
}

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>
    val currentUser: Flow<User?>

    suspend fun getCurrentUser(): User?

    suspend fun isOnline(): Boolean
    suspend fun isOffline(): Boolean

    suspend fun isAuthenticated(): Boolean
    suspend fun getRequiresReauth(): Boolean

    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signOut(): AuthResult

    suspend fun getCurrentUserId(): String?

    suspend fun clearSessionOnly()

    suspend fun tryRestoreSession(): Boolean

    suspend fun restoreSessionIfNeeded()
}

class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val networkMonitor: NetworkMonitor,
    private val secureCredentialsRepo: SecureCredentialsRepository
) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    override val currentUser: Flow<User?> = flow {
        val storedUser = secureCredentialsRepo.getUser()
        if (storedUser != null) {
            emit(storedUser)
        } else {
            val (storedEmail, _, storedUserId) = secureCredentialsRepo.getStoredCredentials()
            if (storedUserId != null && storedEmail != null) {
                emit(User(id = storedUserId, email = storedEmail))
            }
        }

        sessionStatus.collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val userInfo = status.session.user
                    val metadata = userInfo?.userMetadata
                    val firstName = (metadata?.get("first_name") as? JsonPrimitive)?.content
                    val lastName = (metadata?.get("last_name") as? JsonPrimitive)?.content
                    val role = (metadata?.get("role") as? JsonPrimitive)?.content
                    val dateOfBirthString = (metadata?.get("date_of_birth") as? JsonPrimitive)?.content
                    val dateOfBirth = dateOfBirthString?.let { LocalDate.parse(it) }
                    val isActive = (metadata?.get("is_active") as? JsonPrimitive)?.booleanOrNull
                    val user = User(
                        id = userInfo?.id!!,
                        email = userInfo.email,
                        firstName = firstName,
                        lastName = lastName,
                        role = role,
                        dateOfBirth = dateOfBirth,
                        isActive = isActive
                    )
                    secureCredentialsRepo.saveUser(user)
                    emit(user)
                }

                else -> {
                    // No session – keep previously emitted stored user
                }
            }
        }
    }.distinctUntilChanged()

    override suspend fun getCurrentUser(): User? = currentUser.first()

    override suspend fun tryRestoreSession(): Boolean {
        return try {
            val (email, password, _) = secureCredentialsRepo.getStoredCredentials()

            if (email != null && password != null) {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                secureCredentialsRepo.setRequiresReauth(false)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("Auth", "Session restore failed", e)
            false
        }
    }

    override suspend fun isOnline() = networkMonitor.isOnline.first()
    override suspend fun isOffline() = !networkMonitor.isOnline.first()

    override suspend fun clearSessionOnly() {
        supabaseClient.auth.clearSession()
        Log.d(LOG_TAG, "Session cleared (offline mode)")
    }

    override suspend fun getRequiresReauth(): Boolean = secureCredentialsRepo.getRequiresReauth()

    override suspend fun isAuthenticated(): Boolean {
        val status = sessionStatus.first()
        if (status is SessionStatus.Authenticated) return true
        val (_, _, userId) = secureCredentialsRepo.getStoredCredentials()
        return userId != null
    }

    override suspend fun restoreSessionIfNeeded() {
        if (!isAuthenticated() && getRequiresReauth().not()) {
            val (email, password, userId) = secureCredentialsRepo.getStoredCredentials()
            if (email != null && password != null) {
                handleOnlineSignIn(email, password)
            }
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult = try {
        if (isOffline()) {
            return handleOfflineSignIn(email, password)
        }

        handleOnlineSignIn(email, password)
    } catch (e: Throwable) {
        Log.d(LOG_TAG, "[Auth] Sign in error =", e)
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
                Log.w(LOG_TAG, "[Auth] Remote signOut failed", e)
            }
        }
        supabaseClient.auth.clearSession()
        secureCredentialsRepo.setRequiresReauth(true)

        Log.d(LOG_TAG, "[Auth] Sign out success.")

        AuthResult.Success
    } catch (e: Throwable) {
        Log.d(LOG_TAG, "[Auth] Sign out error =", e)
        when (e) {
            is AuthRestException -> AuthResult.InvalidCredentials()
            is HttpRequestTimeoutException -> AuthResult.Timeout()
            is HttpRequestException -> AuthResult.NoConnection()
            else -> AuthResult.Unknown()
        }
    }


    private suspend fun handleOfflineSignIn(email: String, password: String): AuthResult {
        val (storedEmail, storedPassword) = secureCredentialsRepo.getStoredCredentials()
        return if (email.equals(storedEmail, ignoreCase = true) && password == storedPassword) {
            supabaseClient.auth.clearSession()
            secureCredentialsRepo.setRequiresReauth(true)
            AuthResult.SuccessOffline
        } else {
            AuthResult.InvalidCredentials(message = "No internet connection and stored credentials do not match.")
        }
    }

    private suspend fun handleOnlineSignIn(email: String, password: String): AuthResult {
        supabaseClient.auth.signInWith(Email) { this.email = email; this.password = password }
        val session =
            supabaseClient.auth.currentSessionOrNull() ?: throw IllegalStateException("Session not established")
        val userId = session.user?.id ?: throw IllegalStateException("User ID missing")
        val metadata = session.user?.userMetadata
        val firstName = (metadata?.get("first_name") as? JsonPrimitive)?.content
        val lastName = (metadata?.get("last_name") as? JsonPrimitive)?.content
        val role = (metadata?.get("role") as? JsonPrimitive)?.content
        val dateOfBirthString = (metadata?.get("date_of_birth") as? JsonPrimitive)?.content
        val dateOfBirth = dateOfBirthString?.let { LocalDate.parse(it) }
        val isActive = (metadata?.get("is_active") as? JsonPrimitive)?.booleanOrNull
        val user = User(
            id = userId,
            email = email,
            firstName = firstName,
            lastName = lastName,
            role = role,
            dateOfBirth = dateOfBirth,
            isActive = isActive
        )
        secureCredentialsRepo.saveCredentials(email, password, userId)
        secureCredentialsRepo.saveUser(user)
        secureCredentialsRepo.setRequiresReauth(false)
        return AuthResult.Success
    }

    companion object {
        private const val LOG_TAG = "Scout: AuthRepository"
    }
}


