package com.humayapp.scout.feature.form.impl.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.humayapp.scout.core.database.dao.BarangayDao
import com.humayapp.scout.core.database.dao.CityMunicipalityDao
import com.humayapp.scout.core.database.dao.ProvinceDao
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesRepository
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesRepositoryImpl
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesService
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesServiceGms
import com.humayapp.scout.feature.form.impl.data.repository.LocationRepository
import com.humayapp.scout.feature.form.impl.data.repository.LocationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FormModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)


    @Provides
    @Singleton
    fun provideCoordinatesService(
        fusedLocationClient: FusedLocationProviderClient,
    ): CoordinatesService = CoordinatesServiceGms(fusedLocationClient)

    @Provides
    @Singleton
    fun provideCoordinatesRepository(
        coordinatesService: CoordinatesService,
    ): CoordinatesRepository = CoordinatesRepositoryImpl(coordinatesService)

//    @Provides
//    @Singleton
//    fun providesFormRepository(
//        formEntryDao: FormEntryDao
//    ): FormRepository = FormRepositoryImpl(formEntryDao = formEntryDao)

    @Provides
    @Singleton
    fun providesLocationRepository(
        provinceDao: ProvinceDao,
        cityMunicipalityDao: CityMunicipalityDao,
        barangayDao: BarangayDao
    ): LocationRepository = LocationRepositoryImpl(
        provinceDao = provinceDao,
        cityMunicipalityDao = cityMunicipalityDao,
        barangayDao = barangayDao,
    )
}
