package com.dynapharm.owner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for dashboard statistics from the API.
 * Contains key performance indicators and their trends.
 */
@Serializable
data class DashboardStatsDto(
    @SerialName("sales_mtd")
    val salesMtd: Double,

    @SerialName("cash_balance")
    val cashBalance: Double,

    @SerialName("inventory_value")
    val inventoryValue: Double,

    @SerialName("total_bv")
    val totalBv: Double,

    @SerialName("pending_approvals")
    val pendingApprovals: Int,

    @SerialName("sales_trend")
    val salesTrend: String, // "up", "down", "neutral"

    @SerialName("cash_trend")
    val cashTrend: String,

    @SerialName("inventory_trend")
    val inventoryTrend: String,

    @SerialName("bv_trend")
    val bvTrend: String,

    @SerialName("approvals_trend")
    val approvalsTrend: String
)
