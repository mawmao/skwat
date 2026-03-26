package com.humayapp.scout.feature.auth.di

import android.content.Context
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.SecureCredentialsRepository
import com.humayapp.scout.feature.auth.data.SupabaseAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun providesAuthRepository(
        supabaseClient: SupabaseClient,
        secureCredentialsRepository: SecureCredentialsRepository,
        networkMonitor: NetworkMonitor
    ): AuthRepository =
        SupabaseAuthRepository(
            supabaseClient = supabaseClient,
            secureCredentialsRepo = secureCredentialsRepository,
            networkMonitor = networkMonitor
        )

    @Provides
    @Singleton
    fun providesSecureCredentialsRepository(@ApplicationContext context: Context): SecureCredentialsRepository =
        SecureCredentialsRepository(context)
}
