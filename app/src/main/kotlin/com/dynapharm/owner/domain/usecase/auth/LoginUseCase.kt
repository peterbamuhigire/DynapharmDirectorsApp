package com.dynapharm.owner.domain.usecase.auth

import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.model.User
import com.dynapharm.owner.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user login.
 * Handles authentication and returns the logged-in user.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Executes the login operation.
     * @param username User's email or username
     * @param password User's password
     * @return Result containing User on success, or error
     */
    suspend operator fun invoke(username: String, password: String): Result<User> {
        // Validate input
        if (username.isBlank()) {
            return Result.Error(
                Exception("Username cannot be empty"),
                "Please enter your username or email"
            )
        }

        if (password.isBlank()) {
            return Result.Error(
                Exception("Password cannot be empty"),
                "Please enter your password"
            )
        }

        // Perform login
        return when (val loginResult = authRepository.login(username, password)) {
            is Result.Success -> {
                // Get current user from repository (already cached during login)
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    Result.Success(user)
                } else {
                    Result.Error(
                        Exception("User data not available after login"),
                        "Login succeeded but user information is missing"
                    )
                }
            }
            is Result.Error -> loginResult
            is Result.Loading -> Result.Loading
        }
    }
}
