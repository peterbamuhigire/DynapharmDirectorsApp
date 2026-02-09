package com.dynapharm.owner.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing cached dashboard statistics.
 * Stores the latest dashboard metrics for offline access.
 */
@Entity(tableName = "dashboard_stats")
data class DashboardStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "sales_mtd")
    val salesMtd: Double,

    @ColumnInfo(name = "cash_balance")
    val cashBalance: Double,

    @ColumnInfo(name = "inventory_value")
    val inventoryValue: Double,

    @ColumnInfo(name = "total_bv")
    val totalBv: Double,

    @ColumnInfo(name = "pending_approvals")
    val pendingApprovals: Int,

    @ColumnInfo(name = "sales_trend")
    val salesTrend: String,

    @ColumnInfo(name = "cash_trend")
    val cashTrend: String,

    @ColumnInfo(name = "inventory_trend")
    val inventoryTrend: String,

    @ColumnInfo(name = "bv_trend")
    val bvTrend: String,

    @ColumnInfo(name = "approvals_trend")
    val approvalsTrend: String,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)
