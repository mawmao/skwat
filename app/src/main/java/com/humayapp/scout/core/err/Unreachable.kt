package com.humayapp.scout.core.err

fun unreachable(reason: String = ""): Nothing =
    error(if (reason.isEmpty()) "unreachable" else "unreachable: $reason")
