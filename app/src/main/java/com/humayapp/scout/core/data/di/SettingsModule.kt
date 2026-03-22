package com.humayapp.scout.core.data.di

import android.content.Context
import com.humayapp.scout.core.data.settings.SettingsDataSource
import com.humayapp.scout.core.data.settings.SettingsDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideSettingsDataSource(@ApplicationContext context: Context): SettingsDataSource {
        return SettingsDataSourceImpl(context)
    }
}