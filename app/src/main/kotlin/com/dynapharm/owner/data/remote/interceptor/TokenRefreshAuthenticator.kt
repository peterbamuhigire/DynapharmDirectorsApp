package com.dynapharm.owner.data.remote.interceptor

import com.dynapharm.owner.data.local.prefs.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject

/**
 * OkHttp Authenticator that handles 401 Unauthorized responses by attempting
 * to refresh the access token using the refresh token.
 *
 * Flow:
 * 1. API call returns 401
 * 2. This authenticator is triggered
 * 3. Attempts to refresh access token using refresh token
 * 4. If successful, saves new access token and retries original request
 * 5. If failed, returns null (forces user to re-login)
 *
 * Usage: Add this authenticator to OkHttpClient in the network module.
 */
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val json: Json
) : Authenticator {

    private companion object {
        const val MAX_RETRY_COUNT = 3
        const val REFRESH_TOKEN_ENDPOINT = "/api/v1/auth/refresh"
    }

    // Track request retry count to prevent infinite loops
    private val Response.retryCount: Int
        get() {
            var count = 0
            var response: Response? = this.priorResponse
            while (response != null) {
                count++
                response = response.priorResponse
            }
            return count
        }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops
        if (response.retryCount >= MAX_RETRY_COUNT) {
            Timber.w("Max retry count reached for token refresh")
            return null
        }

        // Get the refresh token
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            Timber.w("No refresh token available, forcing re-login")
            tokenManager.clearTokens()
            return null
        }

        // Synchronously refresh the token (Authenticator doesn't support suspend functions)
        val newAccessToken = runBlocking {
            refreshAccessToken(response.request.url.scheme + "://" + response.request.url.host, refreshToken)
        }

        if (newAccessToken == null) {
            Timber.w("Token refresh failed, forcing re-login")
            tokenManager.clearTokens()
            return null
        }

        // Save the new access token
        tokenManager.saveAccessToken(newAccessToken)

        // Retry the original request with the new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    /**
     * Attempts to refresh the access token using the refresh token.
     * Makes a direct HTTP call to avoid circular dependency with Retrofit.
     *
     * @param baseUrl The base URL of the API
     * @param refreshToken The refresh token
     * @return The new access token, or null if refresh failed
     */
    private suspend fun refreshAccessToken(baseUrl: String, refreshToken: String): String? {
        return try {
            // Create a simple OkHttpClient without interceptors to avoid circular calls
            val client = OkHttpClient.Builder().build()

            val requestBody = RefreshTokenRequest(refreshToken)
            val jsonBody = json.encodeToString(RefreshTokenRequest.serializer(), requestBody)

            val request = Request.Builder()
                .url("$baseUrl$REFRESH_TOKEN_ENDPOINT")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return null
                val refreshResponse = json.decodeFromString<RefreshTokenResponse>(responseBody)

                if (refreshResponse.success && refreshResponse.data?.accessToken != null) {
                    Timber.d("Token refreshed successfully")
                    refreshResponse.data.accessToken
                } else {
                    Timber.e("Token refresh response unsuccessful: ${refreshResponse.error?.message}")
                    null
                }
            } else {
                Timber.e("Token refresh failed with code: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during token refresh")
            null
        }
    }
}

/**
 * Request body for refresh token endpoint
 */
@Serializable
private data class RefreshTokenRequest(
    val refresh_token: String
)

/**
 * Response from refresh token endpoint
 */
@Serializable
private data class RefreshTokenResponse(
    val success: Boolean,
    val data: RefreshTokenData? = null,
    val error: ErrorData? = null
)

@Serializable
private data class RefreshTokenData(
    val accessToken: String? = null,
    val access_token: String? = null // Support both snake_case and camelCase
) {
    // Convenience property to handle both formats
    val tokenValue: String?
        get() = accessToken ?: access_token
}

@Serializable
private data class ErrorData(
    val code: String? = null,
    val message: String? = null
)
