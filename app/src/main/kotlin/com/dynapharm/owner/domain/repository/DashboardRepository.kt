package com.dynapharm.owner.domain.repository

import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for dashboard data operations.
 * Defines contract for fetching and caching dashboard metrics.
 */
interface DashboardRepository {

    /**
     * Observes dashboard statistics.
     * Emits cached data first if available, then fetches fresh data from API.
     * @return Flow emitting Result states with DashboardStats
     */
    fun observeDashboardStats(): Flow<Result<DashboardStats>>

    /**
     * Fetches dashboard statistics.
     * Uses cache-first strategy with background refresh.
     * @param forceRefresh If true, bypasses cache and fetches from API
     * @return Result with DashboardStats
     */
    suspend fun getDashboardStats(forceRefresh: Boolean = false): Result<DashboardStats>

    /**
     * Refreshes dashboard statistics from API.
     * Forces a fresh fetch regardless of cache state.
     * @return Result with updated DashboardStats
     */
    suspend fun refreshDashboardStats(): Result<DashboardStats>

    /**
     * Clears cached dashboard data.
     */
    suspend fun clearCache()
}
