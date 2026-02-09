package com.dynapharm.owner.domain.usecase.auth

import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user logout.
 * Clears tokens and session data.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Executes the logout operation.
     * @return Result indicating success or error
     */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
