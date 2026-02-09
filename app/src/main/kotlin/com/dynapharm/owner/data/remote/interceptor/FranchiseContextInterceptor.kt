package com.dynapharm.owner.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp Interceptor that adds the X-Franchise-ID header to all requests.
 * This header identifies which franchise the request is being made for.
 *
 * Currently uses a hardcoded franchise ID of 1.
 * TODO: Make this dynamic based on user's selected franchise from preferences/database.
 *
 * Usage: Add this interceptor to OkHttpClient in the network module.
 */
class FranchiseContextInterceptor @Inject constructor() : Interceptor {

    private companion object {
        const val HEADER_FRANCHISE_ID = "X-Franchise-ID"

        // TODO: Replace with dynamic franchise ID from user preferences/session
        // This will be updated once we have:
        // 1. User authentication flow complete
        // 2. Franchise selection mechanism
        // 3. Franchise data stored in local database/preferences
        const val DEFAULT_FRANCHISE_ID = "1"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Add the franchise ID header to the request
        val requestWithFranchiseId = originalRequest.newBuilder()
            .header(HEADER_FRANCHISE_ID, DEFAULT_FRANCHISE_ID)
            .build()

        return chain.proceed(requestWithFranchiseId)
    }

    /**
     * Updates the franchise ID for subsequent requests.
     * This method will be called when the user switches franchises.
     *
     * Note: Currently not implemented as we're using a constant.
     * Will be implemented in a future update when we add multi-franchise support.
     */
    // TODO: Implement dynamic franchise switching
    // fun setFranchiseId(franchiseId: String) {
    //     // Store in preferences or in-memory cache
    // }
}
