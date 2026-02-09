package com.dynapharm.owner.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dynapharm.owner.data.local.db.dao.DashboardDao
import com.dynapharm.owner.data.local.db.entity.DashboardStatsEntity

/**
 * Room database for the Dynapharm Owner Hub app.
 * Contains all database tables and provides DAOs for data access.
 */
@Database(
    entities = [
        DashboardStatsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters()
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to dashboard data.
     */
    abstract fun dashboardDao(): DashboardDao
}
