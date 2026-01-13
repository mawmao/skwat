package com.humayapp.scout.core.network.di

import com.humayapp.scout.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun providesSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            defaultLogLevel = LogLevel.DEBUG

            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }

//    @Provides
//    @Singleton
//    fun providesSupabaseService(client: SupabaseClient): SupabaseService =
//        SupabaseService(client = client)

}
