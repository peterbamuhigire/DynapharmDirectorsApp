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

    private companion object {
        // Endpoints that don't require authentication
        val SKIP_AUTH_ENDPOINTS = setOf(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath

        // Skip adding Authorization header for public endpoints
        if (shouldSkipAuth(requestPath)) {
            return chain.proceed(originalRequest)
        }

        // Get the access token
        val accessToken = tokenManager.getAccessToken()

        // If no token is available, proceed without Authorization header
        // (API will return 401, which will be handled by TokenRefreshAuthenticator)
        if (accessToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // Add Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    /**
     * Checks if the request path matches any of the endpoints that don't need auth.
     */
    private fun shouldSkipAuth(path: String): Boolean {
        return SKIP_AUTH_ENDPOINTS.any { endpoint ->
            path.endsWith(endpoint)
        }
    }
}
