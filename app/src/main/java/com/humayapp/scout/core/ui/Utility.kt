package com.humayapp.scout.core.ui

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.debug() = this.border(width = 1.dp, color = Color.Red)



// Convenience

@Composable
fun rememberFocusRequester() = remember { FocusRequester() }
@Composable
fun rememberBooleanState(initialValue: Boolean) = remember { mutableStateOf(initialValue) }