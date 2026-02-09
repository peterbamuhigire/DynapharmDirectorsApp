package com.dynapharm.owner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Dynapharm Owner Hub.
 * Entry point for Hilt dependency injection and global initialization.
 */
@HiltAndroidApp
class OwnerHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging (only in debug/staging builds)
        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(Timber.DebugTree())
            Timber.d("OwnerHubApplication initialized")
            Timber.d("API Base URL: ${BuildConfig.API_BASE_URL}")
            Timber.d("App Name: ${BuildConfig.APP_NAME}")
            Timber.d("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        }
    }
}
