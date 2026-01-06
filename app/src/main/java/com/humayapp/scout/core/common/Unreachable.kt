package com.humayapp.scout.core.common

fun unreachable(reason: String = ""): Nothing =
    error(if (reason.isEmpty()) "unreachable" else "unreachable: $reason")
