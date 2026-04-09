package com.humayapp.scout.core.data.di

import android.content.Context
import com.humayapp.scout.core.data.notification.NotificationRepository
import com.humayapp.scout.core.data.settings.SettingsDataSource
import com.humayapp.scout.core.data.settings.SettingsDataSourceImpl
import com.humayapp.scout.core.database.dao.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideSettingsDataSource(@ApplicationContext context: Context): SettingsDataSource {
        return SettingsDataSourceImpl(context)
    }

    @Provides
    @Singleton
    fun providesNotificationRepository(
        supabase: SupabaseClient,
        dao: NotificationDao,
    ): NotificationRepository {
        return NotificationRepository(supabase, dao)
    }
}