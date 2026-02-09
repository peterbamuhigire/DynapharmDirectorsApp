package com.dynapharm.owner.presentation.screens.auth

import com.dynapharm.owner.domain.model.User

/**
 * UI state for the Login screen.
 * Represents all possible states during the login flow.
 */
sealed class LoginUiState {
    /**
     * Initial state before any login attempt.
     */
    data object Idle : LoginUiState()

    /**
     * Loading state while login request is in progress.
     */
    data object Loading : LoginUiState()

    /**
     * Success state after successful login.
     * @param user The logged-in user
     */
    data class Success(val user: User) : LoginUiState()

    /**
     * Error state when login fails.
     * @param message Human-readable error message
     */
    data class Error(val message: String) : LoginUiState()
}
