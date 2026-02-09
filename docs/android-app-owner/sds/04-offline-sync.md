# SDS 04 -- Offline Caching and Sync Strategy

**Parent:** [03_SDS.md](../03_SDS.md)

---

## 1. Design Philosophy

The Owner Portal app is **read-heavy, write-light** (95% reads, 4% approvals, 1% edits). This shapes the caching strategy:

- **Aggressive read caching** with short TTLs
- **Minimal write queues** (only approval actions)
- **Stale-while-revalidate** pattern for most data
- **NOT full offline-first** -- the app degrades gracefully when offline

---

## 2. Cache Architecture

```
API Response --> Room Cache (with TTL) --> UI Display
                      ^
              Manual Refresh bypasses cache
```

### Data Flow with Caching

```
1. ViewModel requests data via UseCase
2. Repository checks Room cache
   a. Cache HIT + not expired  --> return cached data
   b. Cache HIT + expired      --> return cached (stale), fetch fresh in background
   c. Cache MISS               --> show loading, fetch from API
3. Fresh API data --> mapper --> save to Room --> emit to UI
4. On error + cache available  --> return stale cache + show warning
5. On error + no cache         --> show error state with retry
```

---

## 3. Staleness Budgets

| Data Type | Cache TTL | Refresh Trigger |
|-----------|-----------|-----------------|
| Dashboard KPIs | 10 minutes | Pull-to-refresh, franchise switch, app resume |
| Report data | 30 minutes | Manual refresh, date range change |
| Franchise list | 24 hours | App launch, manual refresh |
| Approval list | 5 minutes | Pull-to-refresh, after approve/reject |
| Profile data | 24 hours | After profile edit |

**Rationale:** Dashboard and approvals have short TTLs because they are time-sensitive. Reports are inherently historical data (rarely changes for a given date range). Franchise list and profile change infrequently.

---

## 4. Room Database Schema

```kotlin
package com.dynapharm.ownerhub.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dynapharm.ownerhub.data.db.dao.*
import com.dynapharm.ownerhub.data.db.entity.*

@Database(
    entities = [
        OwnerEntity::class, FranchiseEntity::class, DashboardKpiEntity::class,
        ReportCacheEntity::class, ApprovalEntity::class, SyncQueueEntity::class
    ],
    version = 1, exportSchema = true
)
abstract class OwnerHubDatabase : RoomDatabase() {
    abstract fun franchiseDao(): FranchiseDao
    abstract fun dashboardKpiDao(): DashboardKpiDao
    abstract fun reportCacheDao(): ReportCacheDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun syncQueueDao(): SyncQueueDao
}
```

---

## 5. Cache Entities

### DashboardKpiEntity

```kotlin
@Entity(tableName = "dashboard_kpi")
data class DashboardKpiEntity(
    @PrimaryKey val franchiseId: Long,
    val salesMtd: Double, val salesTarget: Double,
    val cashBalance: Double, val inventoryValue: Double,
    val totalBv: Double, val pendingApprovals: Int,
    val cachedAt: Long, val expiresAt: Long
)
```

### ReportCacheEntity

```kotlin
@Entity(
    tableName = "report_cache",
    indices = [Index(value = ["franchiseId", "reportType", "dateFrom", "dateTo"])]
)
data class ReportCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val franchiseId: Long, val reportType: String,
    val dateFrom: String, val dateTo: String,
    val responseJson: String,   // Full API response as JSON
    val cachedAt: Long, val expiresAt: Long
)
```

### ApprovalEntity

```kotlin
@Entity(tableName = "approvals")
data class ApprovalEntity(
    @PrimaryKey val id: Long,
    val franchiseId: Long, val approvalType: String,
    val title: String, val description: String,
    val amount: Double?, val requestedBy: String,
    val requestedAt: String, val status: String,
    val cachedAt: Long, val expiresAt: Long
)
```

### SyncQueueEntity

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operation: String,     // "approve" | "reject"
    val endpoint: String,      // Full API endpoint path
    val method: String,        // "POST"
    val payload: String,       // JSON body
    val status: String,        // "pending" | "processing" | "completed" | "failed"
    val retryCount: Int = 0, val maxRetries: Int = 3,
    val createdAt: Long, val processedAt: Long? = null,
    val errorMessage: String? = null
)
```

---

## 6. DAOs

### ReportCacheDao

```kotlin
@Dao
interface ReportCacheDao {
    @Query("""SELECT * FROM report_cache
        WHERE franchiseId = :franchiseId AND reportType = :type
          AND dateFrom = :from AND dateTo = :to AND expiresAt > :now LIMIT 1""")
    suspend fun getCachedReport(franchiseId: Long, type: String, from: String, to: String, now: Long): ReportCacheEntity?

    @Query("""SELECT * FROM report_cache
        WHERE franchiseId = :franchiseId AND reportType = :type
          AND dateFrom = :from AND dateTo = :to ORDER BY cachedAt DESC LIMIT 1""")
    suspend fun getStaleReport(franchiseId: Long, type: String, from: String, to: String): ReportCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheReport(report: ReportCacheEntity)

    @Query("DELETE FROM report_cache WHERE franchiseId = :franchiseId")
    suspend fun clearFranchiseCache(franchiseId: Long)

    @Query("DELETE FROM report_cache WHERE expiresAt < :now")
    suspend fun clearExpiredCache(now: Long)

    @Query("DELETE FROM report_cache")
    suspend fun clearAll()
}
```

### DashboardKpiDao

```kotlin
@Dao
interface DashboardKpiDao {
    @Query("SELECT * FROM dashboard_kpi WHERE franchiseId = :franchiseId AND expiresAt > :now")
    suspend fun getCachedKpi(franchiseId: Long, now: Long): DashboardKpiEntity?

    @Query("SELECT * FROM dashboard_kpi WHERE franchiseId = :franchiseId")
    suspend fun getStaleKpi(franchiseId: Long): DashboardKpiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheKpi(kpi: DashboardKpiEntity)

    @Query("DELETE FROM dashboard_kpi WHERE franchiseId = :franchiseId")
    suspend fun clearFranchiseKpi(franchiseId: Long)

    @Query("DELETE FROM dashboard_kpi")
    suspend fun clearAll()
}
```

### SyncQueueDao

```kotlin
@Dao
interface SyncQueueDao {
    @Insert
    suspend fun enqueue(item: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY createdAt ASC")
    suspend fun getPendingItems(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'pending'")
    suspend fun getPendingCount(): Int

    @Query("UPDATE sync_queue SET status = :status, processedAt = :processedAt, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, processedAt: Long?, errorMessage: String?)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)

    @Query("DELETE FROM sync_queue WHERE status = 'completed'")
    suspend fun clearCompleted()

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()
}
```

---

## 7. Approval Sync Worker (WorkManager)

```kotlin
@HiltWorker
class ApprovalSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val approvalApiService: ApprovalApiService,
    private val json: Json
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendingItems = syncQueueDao.getPendingItems()
        if (pendingItems.isEmpty()) return Result.success()

        var allSuccess = true
        for (item in pendingItems) {
            if (item.retryCount >= item.maxRetries) {
                syncQueueDao.updateStatus(item.id, "failed", System.currentTimeMillis(), "Max retries exceeded")
                continue
            }
            try {
                syncQueueDao.updateStatus(item.id, "processing", null, null)
                val request = json.decodeFromString<ApprovalActionRequest>(item.payload)
                val type = item.endpoint.substringAfterLast("/").substringBefore(".php")
                val response = approvalApiService.processApproval(type, request)
                if (response.success) {
                    syncQueueDao.updateStatus(item.id, "completed", System.currentTimeMillis(), null)
                } else {
                    throw Exception(response.message ?: "API returned failure")
                }
            } catch (e: Exception) {
                syncQueueDao.incrementRetry(item.id)
                syncQueueDao.updateStatus(item.id, "pending", null, e.message)
                allSuccess = false
            }
        }
        syncQueueDao.clearCompleted()
        return if (allSuccess) Result.success() else Result.retry()
    }
}
```

---

## 8. SyncManager

```kotlin
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleSyncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<ApprovalSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("approval_sync", ExistingWorkPolicy.REPLACE, request)
    }
}
```

---

## 9. Cache Invalidation Rules

| Trigger | Action |
|---------|--------|
| Franchise switch | Clear all cached data EXCEPT franchise list |
| Logout | Clear ALL cached data (Room + EncryptedPrefs) |
| TTL expiry | Fetch fresh data on next access, return stale while loading |
| Manual pull-to-refresh | Bypass cache, fetch fresh from API |
| After approve/reject | Invalidate approval list cache, re-fetch |
| After profile edit | Invalidate profile cache |
| App launch | Check franchise list freshness, refresh dashboard KPIs |

```kotlin
// On franchise switch
suspend fun onFranchiseSwitch(newFranchiseId: Long) {
    dashboardKpiDao.clearFranchiseKpi(newFranchiseId)
    reportCacheDao.clearFranchiseCache(newFranchiseId)
    approvalDao.clearFranchiseApprovals(newFranchiseId)
}

// On logout
suspend fun onLogout() {
    dashboardKpiDao.clearAll(); reportCacheDao.clearAll()
    approvalDao.clearAll(); syncQueueDao.clearAll()
    franchiseDao.clearAll(); tokenManager.clearTokens()
}
```

---

## 10. Conflict Resolution

**Server wins** for all approval actions:

1. User approves an expense while offline -- queued in `sync_queue`
2. Meanwhile, another owner approves the same expense from web
3. When device comes online, sync worker sends the approve request
4. Server responds with "already processed" (success or error code)
5. App accepts server state, shows notification: "This item was already approved"

This prevents double-approvals and maintains server as the single source of truth.

---

## 11. Cross-References

| Topic | Document |
|-------|----------|
| Room database Hilt setup | [03-hilt-modules.md](03-hilt-modules.md) |
| API service definitions | [06-networking.md](06-networking.md) |
| Security (token clearing) | [05-security.md](05-security.md) |
| Architecture layers | [01-architecture.md](01-architecture.md) |
| API contract details | [../api-contract/](../api-contract/) |
