package com.dynapharm.owner.domain.repository

import com.dynapharm.owner.domain.model.Franchise
import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.model.User

/**
 * Repository interface for authentication operations.
 * Defines contract for login, logout, and token management.
 */
interface AuthRepository {

    /**
     * Attempts to log in a user with email and password.
     * @param email User's email address
     * @param password User's password
     * @return Result containing login response or error
     */
    suspend fun login(email: String, password: String): Result<LoginResponse>

    /**
     * Logs out the current user.
     * Clears all stored tokens and session data.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Checks if the user has a valid session.
     * @return true if user is logged in with valid tokens
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * Refreshes the access token using the refresh token.
     * @return Result containing new access token or error
     */
    suspend fun refreshToken(): Result<String>

    /**
     * Gets the current logged-in user.
     * @return User object or null if not logged in
     */
    suspend fun getCurrentUser(): User?

    /**
     * Gets the franchises associated with the current user.
     * @return List of franchises
     */
    suspend fun getUserFranchises(): List<Franchise>
}

/**
 * Response data from successful login.
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val userName: String,
    val userEmail: String
)
