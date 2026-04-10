package com.humayapp.scout.core.database.di

import android.content.Context
import androidx.room.Room
import com.humayapp.scout.core.database.ScoutDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesScoutDatabase(
        @ApplicationContext context: Context,
    ): ScoutDatabase = Room.databaseBuilder(
        context = context,
        klass = ScoutDatabase::class.java,
        name = "scout-database",
    ).createFromAsset("seed.db")
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
