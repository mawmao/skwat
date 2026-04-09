package com.humayapp.scout.feature.auth.model

import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.time.Instant

@Serializable
data class ScoutUser(
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


fun UserInfo?.toScoutUser(): ScoutUser {
    val metadata = this?.userMetadata
    val firstName = (metadata?.get("first_name") as? JsonPrimitive)?.content
    val lastName = (metadata?.get("last_name") as? JsonPrimitive)?.content
    val role = (metadata?.get("role") as? JsonPrimitive)?.content
    val dateOfBirthString = (metadata?.get("date_of_birth") as? JsonPrimitive)?.content
    val dateOfBirth = dateOfBirthString?.let { LocalDate.parse(it) }
    val isActive = (metadata?.get("is_active") as? JsonPrimitive)?.booleanOrNull
    return ScoutUser(
        id = this?.id!!,
        email = this.email,
        firstName = firstName,
        lastName = lastName,
        role = role,
        dateOfBirth = dateOfBirth,
        isActive = isActive
    )
}


