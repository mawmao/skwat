package com.humayapp.scout.feature.auth.data.util

import androidx.compose.runtime.Composable
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun SessionStatus?.onAvailability(block: @Composable (SessionStatus) -> Unit) = this?.let { block(this) }

//fun SessionStatus?.onAvailable(block: (SessionStatus) -> Unit) = this?.let { block(this) }
