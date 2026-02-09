# Phase 1 -- Section 07: Room Database

**Parent:** [Phase 1 README](./README.md) | [All Docs](../README.md)

---

## 1. Overview

Phase 1 provisions Room with a single entity (`DashboardStatsEntity`) and DAO.
Phase 2+ adds `ReportCacheEntity`, `ApprovalEntity`, `FranchiseEntity`, and
`SyncQueueEntity` via auto-migrations.

| Concern | Decision |
|---------|----------|
| Database name | `dynapharm_owner.db` |
| Version | 1 (Phase 1 baseline) |
| Schema export | Enabled -- JSON to `app/schemas/` for migration testing |
| Cache TTL | 30 minutes for dashboard stats |
| Stale-while-revalidate | Return expired cache while background fetch runs |

---

## 2. AppDatabase.kt

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

---

## 3. DashboardStatsEntity

One row per franchise (PK = `franchiseId`). Upserted on every successful fetch.

```kotlin
package com.dynapharm.owner.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_stats")
data class DashboardStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "franchise_id")   val franchiseId: Long,

    // Sales KPIs
    @ColumnInfo(name = "sales_mtd")      val salesMtd: Double,
    @ColumnInfo(name = "sales_target")   val salesTarget: Double,
    @ColumnInfo(name = "sales_today")    val salesToday: Double,

    // Financial KPIs
    @ColumnInfo(name = "cash_balance")   val cashBalance: Double,
    @ColumnInfo(name = "inventory_value") val inventoryValue: Double,
    @ColumnInfo(name = "receivables")    val receivables: Double,

    // Distributor KPIs
    @ColumnInfo(name = "total_bv")       val totalBv: Double,
    @ColumnInfo(name = "active_distributors")  val activeDistributors: Int,
    @ColumnInfo(name = "new_distributors_mtd") val newDistributorsMtd: Int,

    // Operational
    @ColumnInfo(name = "pending_approvals") val pendingApprovals: Int,
    @ColumnInfo(name = "currency_code")    val currencyCode: String,

    // Cache metadata
    @ColumnInfo(name = "fetched_at")  val fetchedAt: Long,
    @ColumnInfo(name = "expires_at")  val expiresAt: Long
) {
    companion object {
        /** 30 minutes in milliseconds. */
        const val CACHE_TTL_MS: Long = 30 * 60 * 1000L
    }
}
```

---

## 4. DashboardDao

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

    /** Fresh cache only (expires_at still in the future). */
    @Query("""
        SELECT * FROM dashboard_stats
        WHERE franchise_id = :franchiseId AND expires_at > :now LIMIT 1
    """)
    suspend fun getFreshStats(franchiseId: Long, now: Long): DashboardStatsEntity?

    /** Stale-while-revalidate -- ignores expiry. */
    @Query("SELECT * FROM dashboard_stats WHERE franchise_id = :franchiseId LIMIT 1")
    suspend fun getStaleStats(franchiseId: Long): DashboardStatsEntity?

    /** Reactive stream for the active franchise. */
    @Query("SELECT * FROM dashboard_stats WHERE franchise_id = :franchiseId LIMIT 1")
    fun observeStats(franchiseId: Long): Flow<DashboardStatsEntity?>

    /** Upsert (REPLACE). One row per franchise_id at any time. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: DashboardStatsEntity)

    /** Clear stats for one franchise (on franchise switch). */
    @Query("DELETE FROM dashboard_stats WHERE franchise_id = :franchiseId")
    suspend fun deleteByFranchise(franchiseId: Long)

    /** Remove expired rows (periodic cleanup). */
    @Query("DELETE FROM dashboard_stats WHERE expires_at < :now")
    suspend fun deleteExpired(now: Long)

    /** Wipe all rows (on logout). */
    @Query("DELETE FROM dashboard_stats")
    suspend fun deleteAll()
}
```

---

## 5. Converters.kt

```kotlin
package com.dynapharm.owner.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
```

Phase 2 will add converters for `List<String>`, `BigDecimal`, etc.

---

## 6. DatabaseModule.kt (Hilt)

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
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
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

---

## 7. Schema Export Configuration

Already configured in Gradle (`sds/02-gradle-config.md`):

```kotlin
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
```

Produces `app/schemas/com.dynapharm.owner.data.db.AppDatabase/1.json`.
The `app/schemas/` directory MUST be committed to version control for
auto-migration diffing.

---

## 8. Migration Strategy

**Phase 1:** No migrations -- initial schema (version 1).
`fallbackToDestructiveMigration()` is a dev-time safety net.

**Phase 2+ auto-migration pattern:**

```kotlin
@Database(
    entities = [
        DashboardStatsEntity::class,
        ReportCacheEntity::class,   // Phase 2
        ApprovalEntity::class,      // Phase 2
        FranchiseEntity::class      // Phase 2
    ],
    version = 2, exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class AppDatabase : RoomDatabase() { /* ... */ }
```

For column renames or deletes, supply an `AutoMigrationSpec`:

```kotlin
@RenameColumn(tableName = "dashboard_stats", fromColumnName = "old", toColumnName = "new")
class Migration2To3 : AutoMigrationSpec
```

**Pre-release policy:** Replace `fallbackToDestructiveMigration()` with
explicit migration paths before any production release.

---

## 9. Cache Eviction Rules

| Event | Action | DAO Method |
|-------|--------|------------|
| Pull-to-refresh | Bypass cache, fetch fresh, upsert | `upsertStats()` |
| Franchise switch | Delete old franchise's stats | `deleteByFranchise()` |
| App resume > 30 min | `getFreshStats()` returns null, triggers re-fetch | `getFreshStats()` |
| Logout | Wipe entire table | `deleteAll()` |
| Periodic cleanup | Remove expired rows | `deleteExpired()` |

### Stale-While-Revalidate Flow

```
1. Repository calls dao.getFreshStats(franchiseId, now)
   - Non-null -> emit success, done
   - Null -> call dao.getStaleStats(franchiseId)
     - Stale exists -> emit stale + isLoading=true
     - No cache -> emit loading(null)
2. Fetch from API
3. Success -> map DTO to Entity (expiresAt = now + 30min) -> upsert -> emit
4. Failure + stale -> keep stale visible, show error snackbar
5. Failure + no cache -> emit error state with retry
```

### Repository Sketch

```kotlin
package com.dynapharm.owner.data.repository

import com.dynapharm.owner.data.api.DashboardApiService
import com.dynapharm.owner.data.db.dao.DashboardDao
import com.dynapharm.owner.data.db.entity.DashboardStatsEntity
import com.dynapharm.owner.data.mapper.DashboardMapper
import com.dynapharm.owner.domain.repository.DashboardRepository
import com.dynapharm.owner.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApiService,
    private val dao: DashboardDao,
    private val mapper: DashboardMapper
) : DashboardRepository {

    override fun getDashboardStats(
        franchiseId: Long, forceRefresh: Boolean
    ): Flow<Resource<com.dynapharm.owner.domain.model.DashboardStats>> = flow {
        val now = System.currentTimeMillis()

        if (!forceRefresh) {
            dao.getFreshStats(franchiseId, now)?.let {
                emit(Resource.Success(mapper.entityToDomain(it))); return@flow
            }
        }

        val stale = dao.getStaleStats(franchiseId)
        emit(Resource.Loading(stale?.let { mapper.entityToDomain(it) }))

        try {
            val resp = api.getDashboardStats(franchiseId)
            if (resp.success && resp.data != null) {
                val entity = mapper.dtoToEntity(resp.data, franchiseId, now)
                dao.upsertStats(entity)
                emit(Resource.Success(mapper.entityToDomain(entity)))
            } else {
                emit(Resource.Error(resp.message ?: "Load failed",
                    stale?.let { mapper.entityToDomain(it) }))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error",
                stale?.let { mapper.entityToDomain(it) }))
        }
    }
}
```

---

## 10. Placeholder Entities (Phase 2+)

| Entity | Phase | Table | Purpose |
|--------|-------|-------|---------|
| `ReportCacheEntity` | 2 | `report_cache` | Serialized report JSON by type + date range |
| `ApprovalEntity` | 2 | `approvals` | Offline approval browsing |
| `FranchiseEntity` | 2 | `franchises` | Franchise list (24-hour TTL) |
| `SyncQueueEntity` | 3 | `sync_queue` | Offline approval action queue |

See `sds/04-offline-sync.md` for full entity definitions.

---

## 11. DAO Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class DashboardDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: DashboardDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.dashboardDao()
    }

    @After fun teardown() { db.close() }

    @Test fun upsertAndRetrieveFreshStats() = runTest {
        val now = System.currentTimeMillis()
        dao.upsertStats(testEntity(1L, now))
        val result = dao.getFreshStats(1L, now)
        assertThat(result).isNotNull()
        assertThat(result!!.franchiseId).isEqualTo(1L)
    }

    @Test fun expiredStatsNotReturnedByGetFresh() = runTest {
        dao.upsertStats(testEntity(1L, System.currentTimeMillis() - 3_600_000))
        assertThat(dao.getFreshStats(1L, System.currentTimeMillis())).isNull()
    }

    @Test fun staleStatsIgnoreExpiry() = runTest {
        dao.upsertStats(testEntity(1L, System.currentTimeMillis() - 3_600_000))
        assertThat(dao.getStaleStats(1L)).isNotNull()
    }

    @Test fun deleteByFranchiseIsScoped() = runTest {
        val now = System.currentTimeMillis()
        dao.upsertStats(testEntity(1L, now))
        dao.upsertStats(testEntity(2L, now))
        dao.deleteByFranchise(1L)
        assertThat(dao.getStaleStats(1L)).isNull()
        assertThat(dao.getStaleStats(2L)).isNotNull()
    }

    private fun testEntity(fid: Long, at: Long) = DashboardStatsEntity(
        franchiseId = fid, salesMtd = 150000.0, salesTarget = 200000.0,
        salesToday = 12500.0, cashBalance = 345000.0, inventoryValue = 890000.0,
        receivables = 45000.0, totalBv = 78000.0, activeDistributors = 142,
        newDistributorsMtd = 8, pendingApprovals = 3, currencyCode = "KES",
        fetchedAt = at, expiresAt = at + DashboardStatsEntity.CACHE_TTL_MS
    )
}
```

---

## 12. Cross-References

| Topic | Document |
|-------|----------|
| Full offline caching strategy | [../sds/04-offline-sync.md](../sds/04-offline-sync.md) |
| Hilt DI modules | [../sds/03-hilt-modules.md](../sds/03-hilt-modules.md) |
| Gradle Room/KSP config | [../sds/02-gradle-config.md](../sds/02-gradle-config.md) |
| Architecture layers | [../sds/01-architecture.md](../sds/01-architecture.md) |
| Phase 1 README | [./README.md](./README.md) |
