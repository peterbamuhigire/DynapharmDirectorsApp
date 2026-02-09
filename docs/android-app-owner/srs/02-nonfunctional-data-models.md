# SRS 02 -- Non-Functional Requirements & Data Models

**Parent:** [02_SRS.md](../02_SRS.md) | [All Docs](../README.md)

---

## 1. Performance Requirements

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-PERF-001 | Cold start time (app launch to interactive dashboard) | < 3 seconds | P0 |
| NFR-PERF-002 | Dashboard KPI load time (API call to rendered cards) | < 2 seconds | P0 |
| NFR-PERF-003 | Report data load time (p95 latency) | < 3 seconds | P0 |
| NFR-PERF-004 | Approval action response time (tap to confirmation) | < 1 second | P0 |
| NFR-PERF-005 | Search and filter response time | < 500 ms | P0 |
| NFR-PERF-006 | List/table scroll frame rate | 60 fps | P0 |
| NFR-PERF-007 | APK download size | < 40 MB | P1 |
| NFR-PERF-008 | Peak memory usage during report rendering | < 200 MB | P1 |
| NFR-PERF-009 | Franchise switch total reload time | < 2 seconds | P0 |
| NFR-PERF-010 | Date picker open and interaction latency | < 200 ms | P0 |
| NFR-PERF-011 | Pull-to-refresh full data reload time | < 3 seconds | P0 |
| NFR-PERF-012 | Report cache hit response time (Room read) | < 500 ms | P0 |

## 2. Security Requirements

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-SEC-001 | All API communication over TLS | TLS 1.2+ minimum | P0 |
| NFR-SEC-002 | Certificate pinning for API domain | 2 active pins + 1 backup | P0 |
| NFR-SEC-003 | JWT tokens stored in EncryptedSharedPreferences | AES-256 encryption | P0 |
| NFR-SEC-004 | Biometric authentication for app re-entry | AndroidX Biometric API | P1 |
| NFR-SEC-005 | Code obfuscation in release builds | ProGuard/R8 enabled | P0 |
| NFR-SEC-006 | No sensitive data in logcat output | Strip all PII from logs | P0 |
| NFR-SEC-007 | Session duration before forced re-auth | 30 days max | P0 |
| NFR-SEC-008 | Auto-logout on refresh token failure | Immediate redirect to login | P0 |
| NFR-SEC-009 | Root/jailbreak detection | Warning dialog, allow continue | P2 |
| NFR-SEC-010 | Prevent clipboard copy of sensitive fields (tokens, passwords) | Disabled via XML flag | P1 |
| NFR-SEC-011 | Franchise data isolation in app | Owner can only view owned franchises | P0 |
| NFR-SEC-012 | Audit log for all approval actions | Log action, user, timestamp | P0 |

## 3. Offline & Cache Requirements

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-CACHE-001 | Cache dashboard KPI data locally | TTL: 10 minutes | P0 |
| NFR-CACHE-002 | Cache report data per franchise + date range | TTL: 30 minutes | P0 |
| NFR-CACHE-003 | Manual refresh (pull-to-refresh) bypasses cache | Force network fetch | P0 |
| NFR-CACHE-004 | Display cached data with staleness indicator | Show "Last updated: X min ago" | P0 |
| NFR-CACHE-005 | Graceful degradation on network loss | Show cached data + offline banner | P0 |
| NFR-CACHE-006 | Queue approval actions when offline | Sync on reconnect via SyncQueueEntity | P1 |
| NFR-CACHE-007 | Clear franchise-specific cache on franchise switch | Immediate invalidation | P0 |

## 4. Reliability Requirements

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-REL-001 | Crash-free session rate | >= 99.5% | P0 |
| NFR-REL-002 | Exponential backoff on API failure | 3 retries: 1s, 2s, 4s | P0 |
| NFR-REL-003 | Auto-recover from token expiry | Transparent refresh + retry | P0 |
| NFR-REL-004 | User-facing error messages | No raw HTTP errors or stack traces | P0 |
| NFR-REL-005 | Room database corruption recovery | Fallback destructive migration | P1 |
| NFR-REL-006 | Background sync for queued approvals | WorkManager periodic sync | P1 |
| NFR-REL-007 | Conflict resolution for simultaneous approvals | Server state wins, refresh local | P0 |

## 5. Accessibility Requirements

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-ACC-001 | Minimum touch target size | 48dp x 48dp | P0 |
| NFR-ACC-002 | Color contrast ratios | WCAG AA (4.5:1 text, 3:1 large) | P0 |
| NFR-ACC-003 | Screen reader support | TalkBack compatible | P1 |
| NFR-ACC-004 | RTL layout for Arabic locale | Full mirroring | P0 |
| NFR-ACC-005 | Respect system font size preference | Scalable sp units | P0 |
| NFR-ACC-006 | Semantic content descriptions for KPI cards | Meaningful labels for each value | P1 |

## 6. Localization Requirements

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-L10N-001 | Supported languages | en, fr, ar, sw, es | P0 |
| NFR-L10N-002 | Currency formatting per franchise | Symbol + locale grouping | P0 |
| NFR-L10N-003 | Date display formatting per locale | d MMM yyyy (e.g., 25 Jan 2025) | P0 |
| NFR-L10N-004 | Number formatting per locale | Locale-aware thousands separator | P0 |
| NFR-L10N-005 | RTL layout direction for Arabic | CompositionLocalLayoutDirection | P0 |
| NFR-L10N-006 | In-app language switcher (independent of device locale) | Immediate UI refresh | P1 |

---

## 7. Room Entity Definitions

### 7.1 OwnerEntity

```kotlin
@Entity(tableName = "owner")
data class OwnerEntity(
    @PrimaryKey
    val id: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val city: String?,
    val country: String?,
    val photoUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
```

### 7.2 FranchiseEntity

```kotlin
@Entity(
    tableName = "franchise",
    indices = [Index(value = ["ownerId"])]
)
data class FranchiseEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val country: String,
    val currency: String,
    val timezone: String,
    val isActive: Boolean,
    val isPrimary: Boolean,
    val ownerId: Long
)
```

### 7.3 DashboardKpiEntity

```kotlin
@Entity(
    tableName = "dashboard_kpi",
    indices = [Index(value = ["franchiseId"], unique = true)]
)
data class DashboardKpiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val franchiseId: Long,
    val salesMtd: Double,
    val cashBalance: Double,
    val inventoryValue: Double,
    val totalBv: Double,
    val pendingApprovals: Int,
    val trendSalesPct: Double?,
    val trendCashPct: Double?,
    val trendInventoryPct: Double?,
    val trendBvPct: Double?,
    val fetchedAt: Long  // epoch millis
)
```

### 7.4 ReportCacheEntity

```kotlin
@Entity(
    tableName = "report_cache",
    indices = [
        Index(value = ["franchiseId", "reportType", "dateFrom", "dateTo"], unique = true)
    ]
)
data class ReportCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val franchiseId: Long,
    val reportType: String,   // e.g., "daily_sales", "profit_loss", "inventory_valuation"
    val dateFrom: String,     // ISO date yyyy-MM-dd
    val dateTo: String,       // ISO date yyyy-MM-dd
    val jsonData: String,     // Raw JSON response stored as text
    val fetchedAt: Long,      // epoch millis
    val expiresAt: Long       // epoch millis (fetchedAt + 30 min)
)
```

### 7.5 ApprovalEntity

```kotlin
@Entity(
    tableName = "approval",
    indices = [
        Index(value = ["franchiseId", "approvalType", "status"]),
        Index(value = ["syncStatus"])
    ]
)
data class ApprovalEntity(
    @PrimaryKey
    val id: Long,
    val franchiseId: Long,
    val approvalType: String,  // expense, po, stock_transfer, stock_adjustment, payroll, leave, asset_depreciation
    val referenceId: Long,
    val title: String,
    val description: String?,
    val amount: Double?,
    val currency: String?,
    val requestedBy: String,
    val requestedDate: String,
    val status: String,        // pending, approved, rejected
    val approverNotes: String?,
    val approvedAt: String?,
    val syncStatus: String     // synced, pending_sync, failed
)
```

### 7.6 SyncQueueEntity

```kotlin
@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["status"])]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operation: String,     // approve, reject
    val endpoint: String,      // Full API endpoint path
    val method: String,        // POST, PUT
    val payload: String,       // JSON payload
    val status: String,        // pending, processing, completed, failed
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val createdAt: Long,       // epoch millis
    val processedAt: Long?     // epoch millis, null until processed
)
```

### 7.7 ProfileEntity

```kotlin
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val id: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val city: String?,
    val country: String?,
    val photoUrl: String?,
    val updatedAt: String
)
```

---

## 8. Android Version Support Matrix

| Android Version | API Level | Support Level | Notes |
|----------------|-----------|---------------|-------|
| Android 8.0 Oreo | API 26 | Minimum SDK | Broadest reach with modern APIs |
| Android 9.0 Pie | API 28 | Supported | Biometric API available |
| Android 10 | API 29 | Supported | Scoped storage |
| Android 11 | API 30 | Supported | Enhanced permissions |
| Android 12 | API 31 | Target SDK | Material You, splash screen API |
| Android 13 | API 33 | Supported | Per-app language preferences |
| Android 14 | API 34 | Compile SDK | Latest APIs |
| Android 15 | API 35 | Forward compatible | Tested in CI |

**Build Configuration:**

```
minSdk = 26
targetSdk = 31
compileSdk = 34
```

---

## 9. Third-Party Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.0+ | Language |
| Jetpack Compose BOM | 2024.06+ | UI toolkit |
| Material 3 | 1.3+ | Design system |
| Hilt | 2.51+ | Dependency injection |
| Retrofit | 2.11+ | HTTP client |
| OkHttp | 4.12+ | HTTP engine + interceptors |
| Moshi | 1.15+ | JSON serialization |
| Room | 2.6+ | Local database |
| DataStore | 1.1+ | Preferences storage |
| Navigation Compose | 2.8+ | Screen navigation |
| Lifecycle ViewModel | 2.8+ | MVVM architecture |
| Coroutines | 1.9+ | Async operations |
| WorkManager | 2.9+ | Background sync |
| Coil | 2.7+ | Image loading |
| AndroidX Biometric | 1.2+ | Biometric authentication |
| AndroidX Security Crypto | 1.1+ | EncryptedSharedPreferences |
| Timber | 5.0+ | Logging (debug builds only) |
| LeakCanary | 2.14+ | Memory leak detection (debug) |
| JUnit 5 | 5.10+ | Unit testing |
| Mockk | 1.13+ | Mocking in tests |
| Turbine | 1.1+ | Flow testing |
| Compose UI Test | BOM version | UI testing |
| Firebase Crashlytics | 19+ | Crash reporting |
| Firebase Analytics | 22+ | Usage analytics |

---

## 10. Build Variants

| Variant | API Base URL | Logging | Obfuscation | Certificate Pinning |
|---------|-------------|---------|-------------|---------------------|
| debug | `https://dev.dynapharm.app/` | Verbose (Timber) | Disabled | Disabled |
| staging | `https://staging.dynapharm.app/` | Warn only | Enabled | Enabled |
| release | `https://app.dynapharm.app/` | None | Enabled (R8 full) | Enabled |

---

*Revision History*

| Date | Version | Author | Changes |
|------|---------|--------|---------|
| 2026-02-08 | 1.0 | Claude | Initial non-functional requirements and data models |
