package com.humayapp.scout.core.database.di

import com.humayapp.scout.core.database.ScoutDatabase
import com.humayapp.scout.core.database.dao.BarangayDao
import com.humayapp.scout.core.database.dao.CityMunicipalityDao
import com.humayapp.scout.core.database.dao.FormEntryDao
import com.humayapp.scout.core.database.dao.ProvinceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    fun providesFormEntryDao(
        database: ScoutDatabase,
    ): FormEntryDao = database.formEntryDao()

    @Provides
    fun providesProvinceDao(
        database: ScoutDatabase,
    ): ProvinceDao = database.provinceDao()

    @Provides
    fun providesCityMunicipalityDao(
        database: ScoutDatabase,
    ): CityMunicipalityDao = database.cityMunicipalityDao()

    @Provides
    fun providesBarangayDao(
        database: ScoutDatabase,
    ): BarangayDao = database.barangayDao()
}
