package com.dynapharm.owner.util

import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.model.Franchise
import com.dynapharm.owner.domain.model.Trend
import com.dynapharm.owner.domain.model.User

/**
 * Mock data for UI testing without backend.
 * Only used in development builds for preview and testing.
 */
object MockData {

    /**
     * Sample user for testing
     */
    val sampleUser = User(
        id = 1,
        name = "John Doe",
        email = "john.doe@dynapharm.com",
        role = "owner",
        phone = "+256 700 123 456"
    )

    /**
     * Sample franchises for testing
     */
    val sampleFranchises = listOf(
        Franchise(id = 1, name = "Kampala Main Branch", branchCount = 5),
        Franchise(id = 2, name = "Nairobi Branch", branchCount = 3),
        Franchise(id = 3, name = "Dar es Salaam Branch", branchCount = 2)
    )

    /**
     * Sample dashboard stats for testing
     */
    val sampleDashboardStats = DashboardStats(
        salesMtd = 1_250_000.50,
        cashBalance = 450_000.00,
        inventoryValue = 2_300_000.00,
        totalBv = 15_000.0,
        pendingApprovals = 7,
        salesTrend = Trend.UP,
        cashTrend = Trend.DOWN,
        inventoryTrend = Trend.UP,
        bvTrend = Trend.NEUTRAL,
        approvalsTrend = Trend.UP
    )

    /**
     * Sample dashboard stats with different values for testing various states
     */
    val lowValuesDashboardStats = DashboardStats(
        salesMtd = 50_000.00,
        cashBalance = 10_000.00,
        inventoryValue = 100_000.00,
        totalBv = 2_500.0,
        pendingApprovals = 0,
        salesTrend = Trend.DOWN,
        cashTrend = Trend.DOWN,
        inventoryTrend = Trend.NEUTRAL,
        bvTrend = Trend.DOWN,
        approvalsTrend = Trend.NEUTRAL
    )

    /**
     * Sample dashboard stats with high values
     */
    val highValuesDashboardStats = DashboardStats(
        salesMtd = 5_500_000.75,
        cashBalance = 1_200_000.00,
        inventoryValue = 8_750_000.00,
        totalBv = 45_000.0,
        pendingApprovals = 25,
        salesTrend = Trend.UP,
        cashTrend = Trend.UP,
        inventoryTrend = Trend.UP,
        bvTrend = Trend.UP,
        approvalsTrend = Trend.UP
    )
}
