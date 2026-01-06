package com.humayapp.scout.core.ui.util

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.debug() = this.border(width = 1.dp, color = Color.Red)

@Composable
fun rememberFocusRequester() = remember { FocusRequester() }
