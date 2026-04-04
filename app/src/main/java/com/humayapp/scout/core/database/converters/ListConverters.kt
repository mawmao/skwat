package com.humayapp.scout.core.database.converters

import androidx.room.TypeConverter
import com.humayapp.scout.core.database.model.SyncStatus
import kotlinx.datetime.LocalDate
import kotlin.time.Instant


class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}

class ListConverters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        return if (data.isNullOrEmpty()) emptyList()
        else data.split(",")
    }
}

class InstantConverter {
    @TypeConverter
    fun fromStringInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

    @TypeConverter
    fun instantToLong(instant: Instant?): String? = instant?.toString()
}

class SyncStatusConverters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(name: String): SyncStatus = enumValueOf(name)
}