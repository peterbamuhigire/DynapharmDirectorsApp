package com.dynapharm.owner.domain.usecase.dashboard

import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing dashboard statistics.
 * Emits cached data immediately if available, then fetches fresh data from API.
 */
class GetDashboardStatsUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    /**
     * Observes dashboard statistics.
     * Returns a Flow that:
     * 1. Emits Loading state
     * 2. Emits cached data if available (even if stale)
     * 3. Fetches fresh data from API and emits it
     * 4. Handles errors gracefully, preferring to show stale data over errors
     *
     * @return Flow emitting Result states with DashboardStats
     */
    operator fun invoke(): Flow<Result<DashboardStats>> {
        return dashboardRepository.observeDashboardStats()
    }
}
