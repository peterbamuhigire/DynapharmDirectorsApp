# Room Database Documentation - Dynapharm Owner Hub

**Version:** 1.0
**Last Updated:** 2026-02-09
**Database Name:** `dynapharm_owner.db`
**App:** Dynapharm Owner Hub (Android)

---

## Purpose

This document provides a comprehensive guide to the Room database implementation for the Dynapharm Owner Hub. It covers the schema, entity definitions, DAO interfaces, caching strategies, migration patterns, and query examples.

For detailed implementation specs, see `docs/android-app-owner/phase-1/07-room-database.md`.

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Database Configuration](#database-configuration)
3. [Entity Definitions](#entity-definitions)
4. [DAO Interfaces](#dao-interfaces)
5. [Caching Strategy](#caching-strategy)
6. [Migration Strategy](#migration-strategy)
7. [Query Examples](#query-examples)
8. [Type Converters](#type-converters)
9. [Testing DAOs](#testing-daos)

---

## Database Overview

### Schema Summary

| Table | Purpose | Cache TTL | Phase |
|-------|---------|-----------|-------|
| `dashboard_stats` | Dashboard KPI cache | 10 minutes | Phase 1 |
| `report_cache` | Generic report cache | 30 minutes | Phase 2 |
| `approvals` | Approval queue cache | 5 minutes | Phase 2 |
| `franchises` | Franchise list cache | 24 hours | Phase 2 |
| `sync_queue` | Offline approval action queue | N/A (persistent) | Phase 3 |

### Database Characteristics

- **Database Name:** `dynapharm_owner.db`
- **Version:** 1 (Phase 1 baseline, increments with migrations)
- **Schema Export:** Enabled (JSON to `app/schemas/`)
- **Caching Pattern:** Stale-while-revalidate
- **Offline Support:** Read-heavy with graceful degradation

---

## Database Configuration

### AppDatabase.kt

```kotlin
package com.dynapharm.owner.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dynapharm.owner.data.db.dao.DashboardDao
import com.dynapharm.owner.data.db.entity.DashboardStatsEntity

@Database(
    entities = [DashboardStatsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao

    companion object {
        const val DATABASE_NAME = "dynapharm_owner.db"
    }
}
```

### Hilt Module (DatabaseModule.kt)

```kotlin
package com.dynapharm.owner.di

import android.content.Context
import androidx.room.Room
import com.dynapharm.owner.data.db.AppDatabase
import com.dynapharm.owner.data.db.dao.DashboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()  // Dev-time safety net
        .build()

    @Provides
    fun provideDashboardDao(db: AppDatabase): DashboardDao = db.dashboardDao()

    // Phase 2+ DAO providers (uncomment as entities are added):
    // @Provides fun provideReportCacheDao(db: AppDatabase) = db.reportCacheDao()
    // @Provides fun provideApprovalDao(db: AppDatabase) = db.approvalDao()
    // @Provides fun provideFranchiseDao(db: AppDatabase) = db.franchiseDao()
    // @Provides fun provideSyncQueueDao(db: AppDatabase) = db.syncQueueDao()
}
```

### Gradle Configuration

```kotlin
// build.gradle.kts (app module)
plugins {
    id("com.google.devtools.ksp") version "2.0.10-1.0.24"
}

android {
    defaultConfig {
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}
```

---

## Entity Definitions

### DashboardStatsEntity (Phase 1)

**Table:** `dashboard_stats`
**Primary Key:** `franchise_id` (one row per franchise)

```kotlin
package com.dynapharm.owner.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_stats")
data class DashboardStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "franchise_id")
    val franchiseId: Long,

    // Sales KPIs
    @ColumnInfo(name = "sales_mtd")
    val salesMtd: Double,

    @ColumnInfo(name = "sales_target")
    val salesTarget: Double,

    @ColumnInfo(name = "sales_today")
    val salesToday: Double,

    // Financial KPIs
    @ColumnInfo(name = "cash_balance")
    val cashBalance: Double,

    @ColumnInfo(name = "inventory_value")
    val inventoryValue: Double,

    @ColumnInfo(name = "receivables")
    val receivables: Double,

    // Distributor KPIs
    @ColumnInfo(name = "total_bv")
    val totalBv: Double,

    @ColumnInfo(name = "active_distributors")
    val activeDistributors: Int,

    @ColumnInfo(name = "new_distributors_mtd")
    val newDistributorsMtd: Int,

    // Operational
    @ColumnInfo(name = "pending_approvals")
    val pendingApprovals: Int,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    // Cache metadata
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,         // Unix timestamp (milliseconds)

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long          // Unix timestamp (milliseconds)
) {
    companion object {
        /** 10 minutes in milliseconds. */
        const val CACHE_TTL_MS: Long = 10 * 60 * 1000L
    }
}
```

### ReportCacheEntity (Phase 2)

**Table:** `report_cache`
**Primary Key:** `id` (auto-generated)

```kotlin
@Entity(
    tableName = "report_cache",
    indices = [Index(value = ["franchise_id", "report_type", "date_from", "date_to"], unique = true)]
)
data class ReportCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "franchise_id")
    val franchiseId: Long,

    @ColumnInfo(name = "report_type")
    val reportType: String,         // "sales_summary", "cash_flow", etc.

    @ColumnInfo(name = "date_from")
    val dateFrom: String,           // YYYY-MM-DD

    @ColumnInfo(name = "date_to")
    val dateTo: String,             // YYYY-MM-DD

    @ColumnInfo(name = "report_json")
    val reportJson: String,         // Serialized JSON of report data

    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long
) {
    companion object {
        const val CACHE_TTL_MS: Long = 30 * 60 * 1000L
    }
}
```

### ApprovalEntity (Phase 2)

**Table:** `approvals`
**Primary Key:** `id` (approval ID from server)

```kotlin
@Entity(
    tableName = "approvals",
    indices = [Index(value = ["franchise_id", "approval_type"])]
)
data class ApprovalEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "franchise_id")
    val franchiseId: Long,

    @ColumnInfo(name = "approval_type")
    val approvalType: String,       // "expense", "purchase", "discount", etc.

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "requested_by")
    val requestedBy: String,

    @ColumnInfo(name = "requested_at")
    val requestedAt: Long,

    @ColumnInfo(name = "status")
    val status: String,             // "pending", "approved", "rejected"

    @ColumnInfo(name = "details_json")
    val detailsJson: String,        // Full approval details as JSON

    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long
) {
    companion object {
        const val CACHE_TTL_MS: Long = 5 * 60 * 1000L
    }
}
```

### FranchiseEntity (Phase 2)

**Table:** `franchises`
**Primary Key:** `id` (franchise ID)

```kotlin
@Entity(tableName = "franchises")
data class FranchiseEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "country")
    val country: String,

    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long
) {
    companion object {
        const val CACHE_TTL_MS: Long = 24 * 60 * 60 * 1000L  // 24 hours
    }
}
```

### SyncQueueEntity (Phase 3 - Offline Sync)

**Table:** `sync_queue`
**Primary Key:** `id` (auto-generated)

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "operation")
    val operation: String,          // "approve", "reject"

    @ColumnInfo(name = "endpoint")
    val endpoint: String,           // Full API endpoint path

    @ColumnInfo(name = "method")
    val method: String,             // "POST"

    @ColumnInfo(name = "payload")
    val payload: String,            // JSON body

    @ColumnInfo(name = "status")
    val status: String,             // "pending", "processing", "completed", "failed"

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = 3,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "processed_at")
    val processedAt: Long? = null,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)
```

---

## DAO Interfaces

### DashboardDao

```kotlin
package com.dynapharm.owner.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dynapharm.owner.data.db.entity.DashboardStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    /**
     * Get fresh cache only (expires_at still in the future).
     * Returns null if cache is expired.
     */
    @Query("""
        SELECT * FROM dashboard_stats
        WHERE franchise_id = :franchiseId AND expires_at > :now
        LIMIT 1
    """)
    suspend fun getFreshStats(franchiseId: Long, now: Long): DashboardStatsEntity?

    /**
     * Get stale cache (ignores expiry).
     * Used for stale-while-revalidate pattern.
     */
    @Query("SELECT * FROM dashboard_stats WHERE franchise_id = :franchiseId LIMIT 1")
    suspend fun getStaleStats(franchiseId: Long): DashboardStatsEntity?

    /**
     * Reactive stream for the active franchise.
     * Emits whenever data changes.
     */
    @Query("SELECT * FROM dashboard_stats WHERE franchise_id = :franchiseId LIMIT 1")
    fun observeStats(franchiseId: Long): Flow<DashboardStatsEntity?>

    /**
     * Upsert (REPLACE strategy).
     * One row per franchise_id at any time.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: DashboardStatsEntity)

    /**
     * Clear stats for one franchise (on franchise switch).
     */
    @Query("DELETE FROM dashboard_stats WHERE franchise_id = :franchiseId")
    suspend fun deleteByFranchise(franchiseId: Long)

    /**
     * Remove expired rows (periodic cleanup).
     */
    @Query("DELETE FROM dashboard_stats WHERE expires_at < :now")
    suspend fun deleteExpired(now: Long)

    /**
     * Wipe all rows (on logout).
     */
    @Query("DELETE FROM dashboard_stats")
    suspend fun deleteAll()
}
```

### ReportCacheDao (Phase 2)

```kotlin
@Dao
interface ReportCacheDao {

    @Query("""
        SELECT * FROM report_cache
        WHERE franchise_id = :franchiseId
          AND report_type = :reportType
          AND date_from = :dateFrom
          AND date_to = :dateTo
          AND expires_at > :now
        LIMIT 1
    """)
    suspend fun getFreshReport(
        franchiseId: Long,
        reportType: String,
        dateFrom: String,
        dateTo: String,
        now: Long
    ): ReportCacheEntity?

    @Query("""
        SELECT * FROM report_cache
        WHERE franchise_id = :franchiseId
          AND report_type = :reportType
          AND date_from = :dateFrom
          AND date_to = :dateTo
        LIMIT 1
    """)
    suspend fun getStaleReport(
        franchiseId: Long,
        reportType: String,
        dateFrom: String,
        dateTo: String
    ): ReportCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheReport(report: ReportCacheEntity)

    @Query("DELETE FROM report_cache WHERE franchise_id = :franchiseId")
    suspend fun clearFranchiseCache(franchiseId: Long)

    @Query("DELETE FROM report_cache WHERE expires_at < :now")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM report_cache")
    suspend fun deleteAll()
}
```

### ApprovalDao (Phase 2)

```kotlin
@Dao
interface ApprovalDao {

    @Query("""
        SELECT * FROM approvals
        WHERE franchise_id = :franchiseId
          AND status = 'pending'
          AND expires_at > :now
        ORDER BY requested_at DESC
    """)
    suspend fun getFreshPendingApprovals(franchiseId: Long, now: Long): List<ApprovalEntity>

    @Query("""
        SELECT * FROM approvals
        WHERE franchise_id = :franchiseId AND status = 'pending'
        ORDER BY requested_at DESC
    """)
    suspend fun getStalePendingApprovals(franchiseId: Long): List<ApprovalEntity>

    @Query("SELECT * FROM approvals WHERE id = :approvalId LIMIT 1")
    suspend fun getApprovalById(approvalId: Long): ApprovalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheApprovals(approvals: List<ApprovalEntity>)

    @Query("DELETE FROM approvals WHERE franchise_id = :franchiseId")
    suspend fun clearFranchiseApprovals(franchiseId: Long)

    @Query("DELETE FROM approvals WHERE expires_at < :now")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM approvals")
    suspend fun deleteAll()
}
```

### SyncQueueDao (Phase 3)

```kotlin
@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY created_at ASC")
    suspend fun getPendingItems(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity): Long

    @Query("""
        UPDATE sync_queue
        SET status = :status, processed_at = :processedAt, error_message = :errorMessage
        WHERE id = :id
    """)
    suspend fun updateStatus(id: Long, status: String, processedAt: Long, errorMessage: String?)

    @Query("UPDATE sync_queue SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)

    @Query("DELETE FROM sync_queue WHERE status = 'completed' AND processed_at < :olderThan")
    suspend fun deleteCompleted(olderThan: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun deleteAll()
}
```

---

## Caching Strategy

### Cache TTL Policies

| Data Type | Entity | TTL | Rationale |
|-----------|--------|-----|-----------|
| Dashboard KPIs | `DashboardStatsEntity` | 10 minutes | Time-sensitive, changes frequently |
| Report data | `ReportCacheEntity` | 30 minutes | Historical, rarely changes for date range |
| Franchise list | `FranchiseEntity` | 24 hours | Rarely changes |
| Approval list | `ApprovalEntity` | 5 minutes | Time-sensitive, frequently updated |

### Stale-While-Revalidate Flow

```
┌─────────────────────────────────────────────┐
│ 1. Repository requests data from DAO        │
└──────┬──────────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────────┐
│ 2. DAO.getFreshData(franchiseId, now)       │
│    (checks expires_at > now)                │
└──────┬──────────────────────────────────────┘
       │
       ├─── Fresh cache exists ──> Emit Success, DONE
       │
       └─── No fresh cache
              │
              v
       ┌─────────────────────────────────────┐
       │ 3. DAO.getStaleData(franchiseId)    │
       │    (ignores expires_at)             │
       └──────┬──────────────────────────────┘
              │
              ├─── Stale cache exists ──> Emit Loading(staleData)
              │                              Fetch from API in background
              │
              └─── No cache ──> Emit Loading(null)
                                  Fetch from API
```

### Repository Caching Pattern

```kotlin
override fun getDashboardKpis(
    franchiseId: Long,
    forceRefresh: Boolean
): Flow<Resource<DashboardKpi>> = flow {
    val now = System.currentTimeMillis()

    // 1. Fast path: Return fresh cache if available
    if (!forceRefresh) {
        dao.getFreshStats(franchiseId, now)?.let { entity ->
            emit(Resource.Success(mapper.entityToDomain(entity)))
            return@flow
        }
    }

    // 2. Stale-while-revalidate: Return stale cache while fetching fresh
    val stale = dao.getStaleStats(franchiseId)
    emit(Resource.Loading(stale?.let { mapper.entityToDomain(it) }))

    // 3. Fetch from API
    try {
        val response = apiService.getDashboardStats()
        if (response.success && response.data != null) {
            // Map DTO to Entity with expiry timestamp
            val entity = mapper.dtoToEntity(
                dto = response.data,
                franchiseId = franchiseId,
                now = now
            )
            dao.upsertStats(entity)
            emit(Resource.Success(mapper.entityToDomain(entity)))
        } else {
            emit(Resource.Error(
                message = response.message ?: "Failed to load data",
                data = stale?.let { mapper.entityToDomain(it) }
            ))
        }
    } catch (e: Exception) {
        emit(Resource.Error(
            message = e.message ?: "Network error",
            data = stale?.let { mapper.entityToDomain(it) }
        ))
    }
}
```

### Cache Eviction Rules

| Event | Action | DAO Method |
|-------|--------|------------|
| Pull-to-refresh | Bypass cache, fetch fresh, upsert | `upsertStats()` |
| Franchise switch | Delete old franchise's data | `deleteByFranchise()` |
| App resume > TTL | `getFreshStats()` returns null, triggers re-fetch | `getFreshStats()` |
| Logout | Wipe entire database | `deleteAll()` |
| Periodic cleanup | Remove expired rows | `deleteExpired()` |
| After approval action | Invalidate approval cache | `clearFranchiseApprovals()` |

---

## Migration Strategy

### Phase 1: Initial Schema (Version 1)

No migrations needed. This is the baseline schema.

```kotlin
@Database(
    entities = [DashboardStatsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase()
```

### Phase 2: Adding Entities (Version 2)

Use Room auto-migration for adding new tables:

```kotlin
@Database(
    entities = [
        DashboardStatsEntity::class,
        ReportCacheEntity::class,      // NEW
        ApprovalEntity::class,         // NEW
        FranchiseEntity::class         // NEW
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase()
```

### Manual Migration Example

For complex schema changes (column renames, deletes):

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new column with default value
        database.execSQL("""
            ALTER TABLE dashboard_stats
            ADD COLUMN sales_ytd REAL NOT NULL DEFAULT 0.0
        """)
    }
}

// Register in DatabaseModule
Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
    .addMigrations(MIGRATION_2_3)
    .build()
```

### Schema Export

Room automatically exports schema JSON to `app/schemas/` directory:

```
app/schemas/
└── com.dynapharm.owner.data.db.AppDatabase/
    ├── 1.json   (version 1 schema)
    ├── 2.json   (version 2 schema)
    └── 3.json   (version 3 schema)
```

**CRITICAL:** Commit `app/schemas/` to version control for migration testing.

### Pre-Release Migration Policy

Before production release:

1. Remove `fallbackToDestructiveMigration()` from DatabaseModule
2. Add explicit migration paths for all schema changes
3. Test migrations thoroughly with real data
4. Never ship destructive migrations to production

---

## Query Examples

### Get Fresh Dashboard Stats

```kotlin
val now = System.currentTimeMillis()
val stats = dashboardDao.getFreshStats(franchiseId = 1L, now = now)

if (stats != null) {
    // Fresh cache available
    displayDashboard(stats)
} else {
    // Cache expired or missing, fetch from API
    fetchFromApi()
}
```

### Observe Dashboard Changes (Reactive)

```kotlin
dashboardDao.observeStats(franchiseId = 1L)
    .collectLatest { stats ->
        if (stats != null) {
            updateUI(stats)
        }
    }
```

### Cache Report Data

```kotlin
val now = System.currentTimeMillis()
val expiresAt = now + ReportCacheEntity.CACHE_TTL_MS

val entity = ReportCacheEntity(
    franchiseId = 1L,
    reportType = "sales_summary",
    dateFrom = "2026-02-01",
    dateTo = "2026-02-09",
    reportJson = json.encodeToString(reportDto),
    fetchedAt = now,
    expiresAt = expiresAt
)

reportCacheDao.cacheReport(entity)
```

### Clear Franchise Data on Switch

```kotlin
suspend fun onFranchiseSwitch(newFranchiseId: Long) {
    dashboardDao.deleteByFranchise(newFranchiseId)
    reportCacheDao.clearFranchiseCache(newFranchiseId)
    approvalDao.clearFranchiseApprovals(newFranchiseId)

    // Franchise list is preserved (shared across franchises)
}
```

### Periodic Cleanup (Background Worker)

```kotlin
class CacheCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()

        dashboardDao.deleteExpired(now)
        reportCacheDao.deleteExpired(now)
        approvalDao.deleteExpired(now)

        return Result.success()
    }
}
```

---

## Type Converters

### Converters.kt

```kotlin
package com.dynapharm.owner.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    // Phase 2: Add converters for complex types
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    @TypeConverter
    fun stringListToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}
```

### Usage in Database

```kotlin
@Database(
    entities = [DashboardStatsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)  // Register converters
abstract class AppDatabase : RoomDatabase()
```

---

## Testing DAOs

### Setup In-Memory Database

```kotlin
@RunWith(AndroidJUnit4::class)
class DashboardDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DashboardDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.dashboardDao()
    }

    @After
    fun teardown() {
        database.close()
    }
}
```

### Test Upsert and Retrieve

```kotlin
@Test
fun upsertAndRetrieveFreshStats() = runTest {
    val now = System.currentTimeMillis()
    val entity = createTestEntity(franchiseId = 1L, fetchedAt = now)

    dao.upsertStats(entity)

    val result = dao.getFreshStats(franchiseId = 1L, now = now)

    assertThat(result).isNotNull()
    assertThat(result!!.franchiseId).isEqualTo(1L)
    assertThat(result.salesMtd).isEqualTo(150000.0)
}
```

### Test Cache Expiry

```kotlin
@Test
fun expiredStatsNotReturnedByGetFresh() = runTest {
    val pastTime = System.currentTimeMillis() - (60 * 60 * 1000)  // 1 hour ago
    val entity = createTestEntity(franchiseId = 1L, fetchedAt = pastTime)

    dao.upsertStats(entity)

    val result = dao.getFreshStats(franchiseId = 1L, now = System.currentTimeMillis())

    assertThat(result).isNull()
}
```

### Test Stale Cache Retrieval

```kotlin
@Test
fun staleStatsIgnoreExpiry() = runTest {
    val pastTime = System.currentTimeMillis() - (60 * 60 * 1000)
    val entity = createTestEntity(franchiseId = 1L, fetchedAt = pastTime)

    dao.upsertStats(entity)

    val result = dao.getStaleStats(franchiseId = 1L)

    assertThat(result).isNotNull()
    assertThat(result!!.franchiseId).isEqualTo(1L)
}
```

### Test Franchise Isolation

```kotlin
@Test
fun deleteByFranchiseIsScoped() = runTest {
    val now = System.currentTimeMillis()

    dao.upsertStats(createTestEntity(franchiseId = 1L, fetchedAt = now))
    dao.upsertStats(createTestEntity(franchiseId = 2L, fetchedAt = now))

    dao.deleteByFranchise(franchiseId = 1L)

    assertThat(dao.getStaleStats(franchiseId = 1L)).isNull()
    assertThat(dao.getStaleStats(franchiseId = 2L)).isNotNull()
}
```

### Test Helper

```kotlin
private fun createTestEntity(franchiseId: Long, fetchedAt: Long) = DashboardStatsEntity(
    franchiseId = franchiseId,
    salesMtd = 150000.0,
    salesTarget = 200000.0,
    salesToday = 12500.0,
    cashBalance = 345000.0,
    inventoryValue = 890000.0,
    receivables = 45000.0,
    totalBv = 78000.0,
    activeDistributors = 142,
    newDistributorsMtd = 8,
    pendingApprovals = 3,
    currencyCode = "KES",
    fetchedAt = fetchedAt,
    expiresAt = fetchedAt + DashboardStatsEntity.CACHE_TTL_MS
)
```

---

## Additional Resources

### Detailed Room Implementation
- `docs/android-app-owner/phase-1/07-room-database.md` - Complete Room implementation
- `docs/android-app-owner/sds/04-offline-sync.md` - Offline caching strategy

### Related Docs
- `CLAUDE.md` - AI development patterns
- `docs/API.md` - API integration guide
- `ARCHITECTURE.md` - Architecture overview

---

**Last Updated:** 2026-02-09
**Maintainer:** Development Team
