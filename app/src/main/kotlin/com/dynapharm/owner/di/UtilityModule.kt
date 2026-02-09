package com.dynapharm.owner.di

import android.content.Context
import com.dynapharm.owner.data.local.prefs.FranchiseManager
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
 * Includes coroutine dispatchers and network monitoring.
 * Note: Json configuration is provided by NetworkModule.
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {

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

    /**
     * Provides FranchiseManager for managing active franchise selection.
     * Uses EncryptedSharedPreferences for secure storage.
     */
    @Provides
    @Singleton
    fun provideFranchiseManager(
        @ApplicationContext context: Context,
        json: Json
    ): FranchiseManager = FranchiseManager(context, json)
}
