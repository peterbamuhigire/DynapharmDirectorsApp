package com.dynapharm.owner.di

import android.content.Context
import androidx.room.Room
import com.dynapharm.owner.data.local.db.AppDatabase
import com.dynapharm.owner.data.local.db.dao.DashboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     * Database name: owner_hub.db
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "owner_hub.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    /**
     * Provides DashboardDao for dashboard data access.
     */
    @Provides
    fun provideDashboardDao(database: AppDatabase): DashboardDao =
        database.dashboardDao()
}
