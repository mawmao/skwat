package com.humayapp.scout.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.humayapp.scout.core.database.converters.InstantConverter
import com.humayapp.scout.core.database.converters.ListConverters
import com.humayapp.scout.core.database.dao.BarangayDao
import com.humayapp.scout.core.database.dao.CityMunicipalityDao
import com.humayapp.scout.core.database.dao.FormEntryDao
import com.humayapp.scout.core.database.dao.ProvinceDao
import com.humayapp.scout.core.database.model.BarangayEntity
import com.humayapp.scout.core.database.model.CityMunicipalityEntity
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.core.database.model.ProvinceEntity

@Database(
    entities = [
        FormEntryEntity::class,
        FormImageEntity::class,
        ProvinceEntity::class,
        CityMunicipalityEntity::class,
        BarangayEntity::class
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ListConverters::class, InstantConverter::class)
abstract class ScoutDatabase : RoomDatabase() {
    abstract fun formEntryDao(): FormEntryDao
    abstract fun provinceDao(): ProvinceDao
    abstract fun cityMunicipalityDao(): CityMunicipalityDao
    abstract fun barangayDao(): BarangayDao
}
