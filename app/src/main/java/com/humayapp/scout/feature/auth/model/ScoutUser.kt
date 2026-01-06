package com.humayapp.scout.feature.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScoutUser(
    val role: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("date_of_birth")
    val dateOfBirth: String,

    @SerialName("email_verified")
    val emailVerified: Boolean
)
