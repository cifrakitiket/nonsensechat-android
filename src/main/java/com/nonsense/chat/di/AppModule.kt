package com.nonsense.chat.di

import com.nonsense.chat.data.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @AppScope
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideSupabase(): SupabaseClient = createSupabaseClient(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY,
    ) {
        install(Auth) {
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}
