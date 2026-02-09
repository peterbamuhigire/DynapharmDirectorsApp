package com.dynapharm.owner.domain.model

import com.dynapharm.owner.data.local.db.entity.DashboardStatsEntity
import com.dynapharm.owner.data.remote.dto.DashboardStatsDto

/**
 * Domain model for dashboard statistics.
 * Contains all key performance indicators and their trends.
 */
data class DashboardStats(
    val salesMtd: Double,
    val cashBalance: Double,
    val inventoryValue: Double,
    val totalBv: Double,
    val pendingApprovals: Int,
    val salesTrend: Trend,
    val cashTrend: Trend,
    val inventoryTrend: Trend,
    val bvTrend: Trend,
    val approvalsTrend: Trend,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Trend direction for KPI values
 */
enum class Trend {
    UP,
    DOWN,
    NEUTRAL;

    companion object {
        /**
         * Converts string representation to Trend enum.
         * @param value String value ("up", "down", "neutral")
         * @return Corresponding Trend enum value
         */
        fun fromString(value: String): Trend {
            return when (value.lowercase()) {
                "up" -> UP
                "down" -> DOWN
                "neutral" -> NEUTRAL
                else -> NEUTRAL
            }
        }
    }

    /**
     * Converts Trend enum to string representation.
     * @return String value ("up", "down", "neutral")
     */
    fun toApiString(): String {
        return when (this) {
            UP -> "up"
            DOWN -> "down"
            NEUTRAL -> "neutral"
        }
    }
}

/**
 * Converts DashboardStatsDto to domain model.
 */
fun DashboardStatsDto.toDomainModel(): DashboardStats {
    return DashboardStats(
        salesMtd = salesMtd,
        cashBalance = cashBalance,
        inventoryValue = inventoryValue,
        totalBv = totalBv,
        pendingApprovals = pendingApprovals,
        salesTrend = Trend.fromString(salesTrend),
        cashTrend = Trend.fromString(cashTrend),
        inventoryTrend = Trend.fromString(inventoryTrend),
        bvTrend = Trend.fromString(bvTrend),
        approvalsTrend = Trend.fromString(approvalsTrend)
    )
}

/**
 * Converts DashboardStatsDto to database entity.
 */
fun DashboardStatsDto.toEntity(): DashboardStatsEntity {
    return DashboardStatsEntity(
        salesMtd = salesMtd,
        cashBalance = cashBalance,
        inventoryValue = inventoryValue,
        totalBv = totalBv,
        pendingApprovals = pendingApprovals,
        salesTrend = salesTrend,
        cashTrend = cashTrend,
        inventoryTrend = inventoryTrend,
        bvTrend = bvTrend,
        approvalsTrend = approvalsTrend
    )
}

/**
 * Converts database entity to domain model.
 */
fun DashboardStatsEntity.toDomainModel(): DashboardStats {
    return DashboardStats(
        salesMtd = salesMtd,
        cashBalance = cashBalance,
        inventoryValue = inventoryValue,
        totalBv = totalBv,
        pendingApprovals = pendingApprovals,
        salesTrend = Trend.fromString(salesTrend),
        cashTrend = Trend.fromString(cashTrend),
        inventoryTrend = Trend.fromString(inventoryTrend),
        bvTrend = Trend.fromString(bvTrend),
        approvalsTrend = Trend.fromString(approvalsTrend),
        lastUpdated = cachedAt
    )
}
