package com.humayapp.scout.core.database.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserDetailsDto(

    @SerialName("id")
    val id: String, // uuid

    @SerialName("role")
    val role: String, // or enum later

    @SerialName("email")
    val email: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("date_of_birth")
    val dateOfBirth: String,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("updated_at")
    val updatedAt: Instant,

    @SerialName("last_sign_in_at")
    val lastSignInAt: Instant?
)