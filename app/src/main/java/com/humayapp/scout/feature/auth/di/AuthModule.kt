package com.humayapp.scout.feature.auth.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.humayapp.scout.core.system.NetworkMonitor
import com.humayapp.scout.feature.auth.data.AuthRepository
import com.humayapp.scout.feature.auth.data.NewAuthRepository
import com.humayapp.scout.feature.auth.data.OfflineAuthDataStore
import com.humayapp.scout.feature.auth.data.SecureCredentialsRepository
import com.humayapp.scout.feature.auth.data.SessionDataStore
import com.humayapp.scout.feature.auth.data.SupabaseAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.java

private val Context.userPrefsStore by preferencesDataStore(name = "session_store")
private val Context.offlineAuthPrefsStore by preferencesDataStore(name = "offline_auth_store")

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideSessionDataStore(@ApplicationContext context: Context, aead: Aead): SessionDataStore {
        return SessionDataStore(dataStore = context.userPrefsStore, aead = aead)
    }

    @Provides
    @Singleton
    fun provideOfflineAuthDataStore(@ApplicationContext context: Context, aead: Aead): OfflineAuthDataStore {
        return OfflineAuthDataStore(dataStore = context.offlineAuthPrefsStore, aead = aead)
    }

    @Provides
    @Singleton
    fun provideAead(@ApplicationContext context: Context): Aead {
        AeadConfig.register()
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, "auth_keyset", "master_key_file")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://auth_master_key")
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

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
    fun providesNewAuthRepository(
        supabase: SupabaseClient,
        store: SessionDataStore,
        offlineAuthDataStore: OfflineAuthDataStore,
        networkMonitor: NetworkMonitor,
    ): NewAuthRepository = NewAuthRepository(supabase, store, offlineAuthDataStore, networkMonitor)

    @Provides
    @Singleton
    fun providesSecureCredentialsRepository(@ApplicationContext context: Context): SecureCredentialsRepository =
        SecureCredentialsRepository(context)
}
