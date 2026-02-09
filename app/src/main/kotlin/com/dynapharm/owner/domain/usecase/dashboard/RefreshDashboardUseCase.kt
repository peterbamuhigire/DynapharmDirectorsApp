package com.dynapharm.owner.domain.usecase.dashboard

import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Use case for forcing a refresh of dashboard statistics.
 * Always fetches fresh data from the API, bypassing cache.
 */
class RefreshDashboardUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    /**
     * Forces a refresh of dashboard statistics from the API.
     * Bypasses cache and always fetches fresh data.
     *
     * @return Result with updated DashboardStats or error
     */
    suspend operator fun invoke(): Result<DashboardStats> {
        return dashboardRepository.refreshDashboardStats()
    }
}
