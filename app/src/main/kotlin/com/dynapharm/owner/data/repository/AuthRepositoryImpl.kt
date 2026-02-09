package com.dynapharm.owner.data.repository

import com.dynapharm.owner.data.local.prefs.TokenManager
import com.dynapharm.owner.data.remote.api.AuthApiService
import com.dynapharm.owner.data.remote.dto.LoginRequestDto
import com.dynapharm.owner.di.IoDispatcher
import com.dynapharm.owner.domain.model.Franchise
import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.model.User
import com.dynapharm.owner.domain.model.toDomain
import com.dynapharm.owner.domain.repository.AuthRepository
import com.dynapharm.owner.domain.repository.LoginResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of AuthRepository.
 * Handles authentication logic, token management, and API communication.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    private var currentUser: User? = null
    private var userFranchises: List<Franchise> = emptyList()

    override suspend fun login(email: String, password: String): Result<LoginResponse> =
        withContext(ioDispatcher) {
            try {
                val request = LoginRequestDto(username = email, password = password)
                val response = authApiService.login(request)

                if (response.success && response.data != null) {
                    val data = response.data

                    // Save tokens
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)

                    // Convert to domain models and cache
                    currentUser = data.user.toDomain()
                    userFranchises = data.franchises.map { it.toDomain() }

                    Result.Success(
                        LoginResponse(
                            accessToken = data.accessToken,
                            refreshToken = data.refreshToken,
                            userId = data.user.id.toString(),
                            userName = data.user.name,
                            userEmail = data.user.email
                        )
                    )
                } else {
                    Result.Error(
                        Exception(response.error?.message ?: "Login failed"),
                        response.error?.message
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Login failed: ${e.message}")
            }
        }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val refreshToken = tokenManager.getRefreshToken()

            // Call API logout if we have a refresh token
            if (refreshToken != null) {
                try {
                    authApiService.logout(mapOf("refresh_token" to refreshToken))
                } catch (e: Exception) {
                    // Continue with logout even if API call fails
                }
            }

            // Clear local data
            tokenManager.clearTokens()
            currentUser = null
            userFranchises = emptyList()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Logout failed: ${e.message}")
        }
    }

    override suspend fun isLoggedIn(): Boolean = withContext(ioDispatcher) {
        tokenManager.hasValidSession()
    }

    override suspend fun refreshToken(): Result<String> = withContext(ioDispatcher) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                return@withContext Result.Error(
                    Exception("No refresh token available"),
                    "Please login again"
                )
            }

            val response = authApiService.refreshToken(
                mapOf("refresh_token" to refreshToken)
            )

            if (response.success && response.data != null) {
                tokenManager.saveAccessToken(response.data.accessToken)
                Result.Success(response.data.accessToken)
            } else {
                Result.Error(
                    Exception(response.error?.message ?: "Token refresh failed"),
                    response.error?.message
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Token refresh failed: ${e.message}")
        }
    }

    override suspend fun getCurrentUser(): User? = withContext(ioDispatcher) {
        currentUser
    }

    override suspend fun getUserFranchises(): List<Franchise> = withContext(ioDispatcher) {
        userFranchises
    }
}
