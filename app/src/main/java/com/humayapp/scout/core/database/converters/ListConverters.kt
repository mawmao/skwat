package com.humayapp.scout.core.database.converters

import androidx.room.TypeConverter
import com.humayapp.scout.core.database.model.SyncStatus
import kotlin.time.Instant


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