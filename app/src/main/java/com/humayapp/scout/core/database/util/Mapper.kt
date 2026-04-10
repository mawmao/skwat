package com.humayapp.scout.core.database.util

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter
import kotlin.time.Instant

private val isoInstant = DateTimeFormatter.ISO_INSTANT
private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE

val json = Json { ignoreUnknownKeys = true }

fun String.toInstantSafe(): Instant = Instant.parse(this)

fun String.toInstantSafeOrNull(): Instant? = Instant.parseOrNull(this)

fun String.toLocalDateSafe(): LocalDate = LocalDate.parse(this)