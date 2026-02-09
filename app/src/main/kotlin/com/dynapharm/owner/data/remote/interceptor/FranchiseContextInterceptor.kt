package com.dynapharm.owner.data.remote.interceptor

import com.dynapharm.owner.BuildConfig
import com.dynapharm.owner.data.local.prefs.FranchiseManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp Interceptor that adds context headers to all requests.
 * Includes franchise ID, app version, and platform information.
 *
 * Headers added:
 * - X-Franchise-ID: Current franchise context (if selected)
 * - X-App-Version: App version from BuildConfig
 * - X-Platform: Always "android"
 *
 * Dynamically retrieves the franchise ID from FranchiseManager.
 * If no franchise is selected (e.g., during login), only version/platform headers are added.
 *
 * Usage: Add this interceptor to OkHttpClient in the network module.
 */
class FranchiseContextInterceptor @Inject constructor(
    private val franchiseManager: FranchiseManager
) : Interceptor {

    private companion object {
        const val HEADER_FRANCHISE_ID = "X-Franchise-ID"
        const val HEADER_APP_VERSION = "X-App-Version"
        const val HEADER_PLATFORM = "X-Platform"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Always add app version and platform
        requestBuilder
            .header(HEADER_APP_VERSION, BuildConfig.VERSION_NAME)
            .header(HEADER_PLATFORM, "android")

        // Add franchise ID if available (allows login endpoint to work without it)
        val franchiseId = franchiseManager.getActiveFranchiseIdString()
        if (franchiseId != null) {
            requestBuilder.header(HEADER_FRANCHISE_ID, franchiseId)
        }

        return chain.proceed(requestBuilder.build())
    }
}
