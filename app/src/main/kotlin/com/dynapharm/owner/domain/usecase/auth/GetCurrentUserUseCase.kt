package com.dynapharm.owner.domain.usecase.auth

import com.dynapharm.owner.domain.model.User
import com.dynapharm.owner.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for getting the current logged-in user.
 * Returns the user from memory/cache.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Gets the current user.
     * @return User object or null if not logged in
     */
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
