package com.dynapharm.owner.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Login screen.
 * Manages login state and handles user authentication.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Attempts to log in with the provided credentials.
     * @param username User's email or username
     * @param password User's password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = loginUseCase(username, password)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(
                        result.message ?: result.exception.message ?: "Login failed"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    /**
     * Clears any error state and returns to idle.
     */
    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    /**
     * Resets the UI state to idle.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
