package com.dynapharm.owner.data.local.db.dao

import androidx.room.*
import com.dynapharm.owner.data.local.db.entity.DashboardStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for dashboard statistics data.
 * Provides methods to interact with cached dashboard metrics.
 */
@Dao
interface DashboardDao {

    /**
     * Observes the latest dashboard statistics.
     * Emits updates whenever the data changes.
     */
    @Query("SELECT * FROM dashboard_stats ORDER BY cached_at DESC LIMIT 1")
    fun observeDashboardStats(): Flow<DashboardStatsEntity?>

    /**
     * Gets the latest dashboard statistics.
     */
    @Query("SELECT * FROM dashboard_stats ORDER BY cached_at DESC LIMIT 1")
    suspend fun getDashboardStats(): DashboardStatsEntity?

    /**
     * Inserts or updates dashboard statistics.
     * Replaces existing data if present.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashboardStats(stats: DashboardStatsEntity)

    /**
     * Deletes all dashboard statistics.
     * Used for cache invalidation.
     */
    @Query("DELETE FROM dashboard_stats")
    suspend fun clearDashboardStats()

    /**
     * Checks if cached dashboard data exists and is recent.
     * @param timestamp Minimum timestamp for fresh data
     */
    @Query("SELECT COUNT(*) > 0 FROM dashboard_stats WHERE cached_at > :timestamp")
    suspend fun hasFreshData(timestamp: Long): Boolean
}
