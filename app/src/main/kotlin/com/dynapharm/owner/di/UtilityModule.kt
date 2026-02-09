package com.dynapharm.owner.di

import android.content.Context
import com.dynapharm.owner.util.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt module providing utility dependencies.
 * Includes JSON configuration, coroutine dispatchers, and network monitoring.
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {

    /**
     * Provides JSON configuration for kotlinx.serialization.
     * Configured to be lenient and ignore unknown keys.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Provides IO Dispatcher for IO-bound operations.
     * Use this for network calls, database access, and file operations.
     */
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides Main Dispatcher for UI operations.
     * Use this for updating UI on the main thread.
     */
    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Provides NetworkMonitor for monitoring network connectivity.
     */
    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitor(context)
}
