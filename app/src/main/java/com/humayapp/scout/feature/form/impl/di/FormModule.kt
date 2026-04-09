package com.humayapp.scout.feature.form.impl.di

import com.humayapp.scout.core.database.dao.CachedFormDetailsDao
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.humayapp.scout.core.common.dispatcher.Dispatcher
import com.humayapp.scout.core.common.dispatcher.ScoutDispatchers.IO
import com.humayapp.scout.core.database.dao.BarangayDao
import com.humayapp.scout.core.database.dao.CityMunicipalityDao
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.dao.FormEntryDao
import com.humayapp.scout.core.database.dao.ProvinceDao
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepository
import com.humayapp.scout.feature.form.impl.data.repository.CollectionRepositoryImpl
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesRepository
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesRepositoryImpl
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesService
import com.humayapp.scout.feature.form.impl.data.repository.CoordinatesServiceGms
import com.humayapp.scout.feature.form.impl.data.repository.FormRepository
import com.humayapp.scout.feature.form.impl.data.repository.FormRepositoryImpl
import com.humayapp.scout.feature.form.impl.data.repository.LocationRepository
import com.humayapp.scout.feature.form.impl.data.repository.LocationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

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
        @ApplicationContext context: Context
    ): CoordinatesService = CoordinatesServiceGms(fusedLocationClient, context)


    @Provides
    @Singleton
    fun provideCollectionsRepository(
        supabaseClient: SupabaseClient,
        collectionTaskDao: CollectionTaskDao,
        cacheDao: CachedFormDetailsDao,
        @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
    ): CollectionRepository = CollectionRepositoryImpl(supabaseClient, collectionTaskDao, cacheDao, ioDispatcher)


    @Provides
    @Singleton
    fun provideCoordinatesRepository(
        coordinatesService: CoordinatesService,
    ): CoordinatesRepository = CoordinatesRepositoryImpl(coordinatesService)

    @Provides
    @Singleton
    fun providesFormRepository(
        formEntryDao: FormEntryDao,
        @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
    ): FormRepository = FormRepositoryImpl(
        formEntryDao = formEntryDao,
        ioDispatcher = ioDispatcher
    )

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
