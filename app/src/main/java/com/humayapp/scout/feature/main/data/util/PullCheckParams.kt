package com.humayapp.scout.feature.main.data.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class PullCheckParams(
    @SerialName("p_user_id")
    val userId: String,

    @SerialName("p_updated_after")
    val updatedAfter: Instant?,
)