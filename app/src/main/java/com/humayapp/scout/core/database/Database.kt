package com.humayapp.scout.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.humayapp.scout.core.database.dao.BarangayDao
import com.humayapp.scout.core.database.dao.CityMunicipalityDao
import com.humayapp.scout.core.database.dao.FormEntryDao
import com.humayapp.scout.core.database.dao.ProvinceDao
import com.humayapp.scout.core.database.model.BarangayEntity
import com.humayapp.scout.core.database.model.CityMunicipalityEntity
import com.humayapp.scout.core.database.model.FormEntryEntity
import com.humayapp.scout.core.database.model.ProvinceEntity

@Database(
    entities = [
        FormEntryEntity::class,
        ProvinceEntity::class,
        CityMunicipalityEntity::class,
        BarangayEntity::class
    ],
    version = 1,
    exportSchema = true,
)
abstract class ScoutDatabase : RoomDatabase() {
    abstract fun formEntryDao(): FormEntryDao
    abstract fun provinceDao(): ProvinceDao
    abstract fun cityMunicipalityDao(): CityMunicipalityDao
    abstract fun barangayDao(): BarangayDao
}
