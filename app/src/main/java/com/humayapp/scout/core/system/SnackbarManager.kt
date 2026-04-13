package com.humayapp.scout.core.system

import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class SnackbarManager @Inject constructor() {
    private val _messages = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val messages: SharedFlow<String> = _messages

    fun show(message: String) {
        _messages.tryEmit(message)
    }
}