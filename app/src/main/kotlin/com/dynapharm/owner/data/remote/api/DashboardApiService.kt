package com.dynapharm.owner.data.remote.api

import com.dynapharm.owner.data.remote.dto.ApiResponse
import com.dynapharm.owner.data.remote.dto.DashboardStatsDto
import retrofit2.http.GET

/**
 * Retrofit API service for dashboard endpoints.
 * Handles fetching dashboard statistics and KPIs.
 */
interface DashboardApiService {

    /**
     * Fetches current dashboard statistics.
     * Includes sales, cash balance, inventory, BV, and pending approvals.
     *
     * @return ApiResponse with DashboardStatsDto containing all KPIs and trends
     */
    @GET("api/owners/dashboard-stats.php")
    suspend fun getDashboardStats(): ApiResponse<DashboardStatsDto>
}
