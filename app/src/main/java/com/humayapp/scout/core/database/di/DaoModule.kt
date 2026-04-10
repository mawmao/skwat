package com.humayapp.scout.core.database.di

import com.humayapp.scout.core.database.ScoutDatabase
import com.humayapp.scout.core.database.dao.BarangayDao
import com.humayapp.scout.core.database.dao.CityMunicipalityDao
import com.humayapp.scout.core.database.dao.CollectionFormDao
import com.humayapp.scout.core.database.dao.CollectionTaskDao
import com.humayapp.scout.core.database.dao.FormEntryDao
import com.humayapp.scout.core.database.dao.ImagesDao
import com.humayapp.scout.core.database.dao.NotificationDao
import com.humayapp.scout.core.database.dao.ProvinceDao
import com.humayapp.scout.core.database.dao.SyncQueueDao
import com.humayapp.scout.core.database.dao.SyncStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    fun providesFormEntryDao(database: ScoutDatabase): FormEntryDao = database.formEntryDao()

    @Provides
    fun providesNotificationDao(database: ScoutDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun providesProvinceDao(database: ScoutDatabase): ProvinceDao = database.provinceDao()

    @Provides
    fun providesCityMunicipalityDao(database: ScoutDatabase): CityMunicipalityDao = database.cityMunicipalityDao()

    @Provides
    fun providesBarangayDao(database: ScoutDatabase): BarangayDao = database.barangayDao()

    @Provides
    fun providesCollectionTaskDao(database: ScoutDatabase): CollectionTaskDao = database.collectionTaskDao()

    @Provides
    fun providesCollectionFormDao(database: ScoutDatabase): CollectionFormDao = database.collectionFormDao()

    @Provides
    fun providesImagesDao(database: ScoutDatabase): ImagesDao = database.imagesDao()

    @Provides
    fun providesSyncStateDao(database: ScoutDatabase): SyncStateDao = database.syncStateDao()

    @Provides
    fun providesSyncQueueDao(database: ScoutDatabase): SyncQueueDao = database.syncQueueDao()
}
