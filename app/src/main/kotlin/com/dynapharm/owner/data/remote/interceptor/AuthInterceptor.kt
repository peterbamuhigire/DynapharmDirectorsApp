package com.dynapharm.owner.data.remote.interceptor

import com.dynapharm.owner.data.local.prefs.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp Interceptor that adds the Authorization header with JWT token to requests.
 * Skips adding the header for login and refresh token endpoints.
 *
 * Usage: Add this interceptor to OkHttpClient in the network module.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Skip adding Authorization header for public endpoints
        // Use contains() to match paths like /api/auth/mobile-login.php
        if (path.contains("mobile-login") ||
            path.contains("mobile-refresh") ||
            path.contains("register") ||
            path.contains("forgot-password") ||
            path.contains("reset-password")) {
            return chain.proceed(request)
        }

        // Get the access token
        val token = tokenManager.getAccessToken()

        // If no token available, proceed without auth
        return if (token != null && token.isNotBlank()) {
            chain.proceed(
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/json")
                    .build()
            )
        } else {
            chain.proceed(request)
        }
    }
}
