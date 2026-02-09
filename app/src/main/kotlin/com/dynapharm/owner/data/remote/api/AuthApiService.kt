package com.dynapharm.owner.data.remote.api

import com.dynapharm.owner.data.remote.dto.ApiResponse
import com.dynapharm.owner.data.remote.dto.LoginRequestDto
import com.dynapharm.owner.data.remote.dto.LoginResponseDto
import com.dynapharm.owner.data.remote.dto.TokenResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

/**
 * Retrofit API service for authentication endpoints.
 * Handles login, token refresh, and logout operations.
 */
interface AuthApiService {

    /**
     * Authenticates an owner user with username and password.
     * @param request LoginRequestDto containing username and password
     * @return ApiResponse with LoginResponseDto containing tokens and user info
     */
    @POST("api/auth/owner-mobile-login.php")
    suspend fun login(@Body request: LoginRequestDto): ApiResponse<LoginResponseDto>

    /**
     * Refreshes the access token using the refresh token.
     * @param request Map containing "refresh_token" key
     * @return ApiResponse with TokenResponseDto containing new access token
     */
    @POST("api/auth/mobile-refresh.php")
    suspend fun refreshToken(@Body request: Map<String, String>): ApiResponse<TokenResponseDto>

    /**
     * Logs out the current user and invalidates tokens.
     * @param request Map containing "refresh_token" key
     * @return ApiResponse with Unit (no data expected)
     */
    @DELETE("api/auth/mobile-logout.php")
    suspend fun logout(@Body request: Map<String, String>): ApiResponse<Unit>
}
