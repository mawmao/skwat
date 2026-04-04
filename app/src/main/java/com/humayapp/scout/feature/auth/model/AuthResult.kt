package com.humayapp.scout.feature.auth.model

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