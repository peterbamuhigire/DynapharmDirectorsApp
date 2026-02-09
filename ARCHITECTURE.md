# Dynapharm Owner Hub - Architecture Documentation

**Version:** 1.0
**Last Updated:** 2026-02-09
**App Name:** Dynapharm Owner Hub (Android)
**Package:** com.dynapharm.ownerhub

---

## Table of Contents

1. [Overview](#overview)
2. [Architectural Pattern](#architectural-pattern)
3. [Layer Descriptions](#layer-descriptions)
4. [Package Structure](#package-structure)
5. [Data Flow](#data-flow)
6. [Dependency Injection (Hilt)](#dependency-injection-hilt)
7. [Franchise Context Management](#franchise-context-management)
8. [Caching Strategy](#caching-strategy)
9. [Navigation Architecture](#navigation-architecture)
10. [Security Architecture](#security-architecture)
11. [Offline-First Approach](#offline-first-approach)
12. [Technology Stack](#technology-stack)
13. [Build Configuration](#build-configuration)

---

## Overview

The Dynapharm Owner Hub is an Android application designed for franchise owners to monitor business performance, view reports, and manage approvals across multiple franchises. The app follows modern Android development best practices with a focus on:

- **Clean Architecture** with clear separation of concerns
- **MVVM pattern** for presentation layer
- **Offline-first** capability with intelligent caching
- **Multi-franchise support** with seamless context switching
- **Security-first design** with encrypted storage and certificate pinning

### Key Characteristics

| Aspect | Description |
|--------|-------------|
| **Primary Use Case** | Read-heavy (95% data retrieval, 5% writes) |
| **Target Users** | Franchise owners and directors |
| **Offline Support** | Intelligent caching with stale-while-revalidate |
| **Multi-tenancy** | Support for multiple franchises per owner |
| **Data Volume** | High (franchise-wide aggregate data) |
| **UI Framework** | Jetpack Compose with Material 3 |

---

## Architectural Pattern

The app implements **MVVM (Model-View-ViewModel) + Clean Architecture** with three strictly separated layers.

### Layer Diagram

```
┌─────────────────────────────────────────────┐
│         PRESENTATION LAYER                   │
│  Compose Screens ← ViewModels ← UiState     │
│  (UI, Navigation, Theme)                     │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│           DOMAIN LAYER                       │
│  UseCases ← Repository Interfaces            │
│  (Business Logic, Domain Models)             │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│            DATA LAYER                        │
│  Repository Implementations                  │
│       ↙              ↘                       │
│  Remote (Retrofit)   Local (Room Cache)     │
│  (API Services)      (DAOs, Entities)       │
└─────────────────────────────────────────────┘
```

### Dependency Direction

```
Presentation  ───→  Domain  ←───  Data
   (depends on)   (independent)  (implements)
```

**Key Rule:** The Domain layer is pure Kotlin with no Android dependencies. Data and Presentation layers depend on Domain interfaces, never the reverse.

---

## Layer Descriptions

### 1. Presentation Layer

**Location:** `com.dynapharm.ownerhub.presentation`

**Responsibilities:**
- Display UI using Jetpack Compose
- Handle user interactions
- Observe ViewModel state
- Navigate between screens
- Apply Material 3 theming

**Components:**
- **Screens:** Composable functions for each screen
- **ViewModels:** State management and business logic orchestration
- **Navigation:** NavHost, routes, and navigation logic
- **UI Components:** Reusable composables (KpiCard, ReportTable, etc.)
- **Theme:** Material 3 theme, colors, typography, shapes

**Key Technologies:**
- Jetpack Compose
- Navigation Compose
- Material 3
- Lifecycle ViewModel

### 2. Domain Layer

**Location:** `com.dynapharm.ownerhub.domain`

**Responsibilities:**
- Define business logic
- Declare repository interfaces (contracts)
- Define domain models
- Implement use cases (single responsibility)

**Components:**
- **Models:** Pure Kotlin data classes representing business entities
- **Repository Interfaces:** Contracts for data operations
- **UseCases:** Single-purpose business logic operations

**Key Characteristics:**
- Pure Kotlin (no Android dependencies)
- Framework-agnostic
- Highly testable with unit tests
- Business rules and validation

### 3. Data Layer

**Location:** `com.dynapharm.ownerhub.data`

**Responsibilities:**
- Implement repository interfaces
- Manage remote API calls
- Handle local database operations
- Map between DTOs, Entities, and Domain Models
- Implement caching strategy

**Components:**
- **API Services:** Retrofit interface definitions
- **DTOs (Data Transfer Objects):** JSON serialization classes
- **Room Database:** Local cache with DAOs and Entities
- **Repository Implementations:** Orchestrate remote and local data sources
- **Mappers:** Convert between data representations

**Key Technologies:**
- Retrofit + OkHttp
- Kotlin Serialization
- Room Database
- Kotlin Coroutines + Flow

---

## Package Structure

```
com.dynapharm.ownerhub/
│
├── data/
│   ├── api/                          # Retrofit service interfaces
│   │   ├── AuthApiService.kt
│   │   ├── DashboardApiService.kt
│   │   ├── FranchiseApiService.kt
│   │   ├── ReportApiService.kt
│   │   ├── ApprovalApiService.kt
│   │   └── ProfileApiService.kt
│   │
│   ├── db/                           # Room database
│   │   ├── OwnerHubDatabase.kt
│   │   ├── dao/                      # Data Access Objects
│   │   │   ├── FranchiseDao.kt
│   │   │   ├── DashboardKpiDao.kt
│   │   │   ├── ReportCacheDao.kt
│   │   │   ├── ApprovalDao.kt
│   │   │   └── SyncQueueDao.kt
│   │   └── entity/                   # Room entities
│   │       ├── FranchiseEntity.kt
│   │       ├── DashboardKpiEntity.kt
│   │       ├── ReportCacheEntity.kt
│   │       ├── ApprovalEntity.kt
│   │       └── SyncQueueEntity.kt
│   │
│   ├── dto/                          # Data Transfer Objects (API)
│   │   ├── auth/
│   │   │   ├── LoginRequest.kt
│   │   │   ├── LoginResponse.kt
│   │   │   └── RefreshTokenResponse.kt
│   │   ├── dashboard/
│   │   │   └── DashboardStatsDto.kt
│   │   ├── franchise/
│   │   │   └── FranchiseDto.kt
│   │   ├── report/
│   │   │   ├── ReportRequestDto.kt
│   │   │   └── ReportResponseDto.kt
│   │   └── approval/
│   │       ├── ApprovalDto.kt
│   │       └── ApprovalActionRequest.kt
│   │
│   ├── mapper/                       # DTO ↔ Entity ↔ Domain converters
│   │   ├── FranchiseMapper.kt
│   │   ├── DashboardMapper.kt
│   │   ├── ReportMapper.kt
│   │   └── ApprovalMapper.kt
│   │
│   └── repository/                   # Repository implementations
│       ├── AuthRepositoryImpl.kt
│       ├── DashboardRepositoryImpl.kt
│       ├── FranchiseRepositoryImpl.kt
│       ├── ReportRepositoryImpl.kt
│       ├── ApprovalRepositoryImpl.kt
│       └── ProfileRepositoryImpl.kt
│
├── domain/
│   ├── model/                        # Domain models (business entities)
│   │   ├── Owner.kt
│   │   ├── Franchise.kt
│   │   ├── DashboardKpi.kt
│   │   ├── ReportData.kt
│   │   ├── Approval.kt
│   │   └── ApprovalAction.kt
│   │
│   ├── repository/                   # Repository interfaces
│   │   ├── AuthRepository.kt
│   │   ├── DashboardRepository.kt
│   │   ├── FranchiseRepository.kt
│   │   ├── ReportRepository.kt
│   │   ├── ApprovalRepository.kt
│   │   └── ProfileRepository.kt
│   │
│   └── usecase/                      # Business logic use cases
│       ├── auth/
│       │   ├── LoginUseCase.kt
│       │   ├── LogoutUseCase.kt
│       │   └── RefreshTokenUseCase.kt
│       ├── dashboard/
│       │   └── GetDashboardKpiUseCase.kt
│       ├── franchise/
│       │   ├── GetFranchisesUseCase.kt
│       │   └── SwitchFranchiseUseCase.kt
│       ├── report/
│       │   ├── GetReportUseCase.kt
│       │   └── ExportReportUseCase.kt
│       ├── approval/
│       │   ├── GetApprovalsUseCase.kt
│       │   ├── ApproveItemUseCase.kt
│       │   └── RejectItemUseCase.kt
│       └── profile/
│           ├── GetProfileUseCase.kt
│           └── UpdateProfileUseCase.kt
│
├── presentation/
│   ├── auth/                         # Authentication screens
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   │
│   ├── dashboard/                    # Dashboard screen
│   │   ├── DashboardScreen.kt
│   │   └── DashboardViewModel.kt
│   │
│   ├── franchise/                    # Franchise switcher
│   │   ├── FranchiseSwitcherSheet.kt
│   │   └── FranchiseViewModel.kt
│   │
│   ├── reports/                      # Report screens
│   │   ├── ReportListScreen.kt
│   │   ├── ReportDetailScreen.kt
│   │   ├── ReportViewModel.kt
│   │   └── components/
│   │       ├── DateRangeSelector.kt
│   │       ├── ReportTable.kt
│   │       ├── SummaryCard.kt
│   │       └── QuickDatePresets.kt
│   │
│   ├── approvals/                    # Approval screens
│   │   ├── ApprovalListScreen.kt
│   │   ├── ApprovalDetailScreen.kt
│   │   ├── ApprovalViewModel.kt
│   │   └── components/
│   │       ├── ApprovalCard.kt
│   │       └── ApprovalActionDialog.kt
│   │
│   ├── profile/                      # Profile screen
│   │   ├── ProfileScreen.kt
│   │   └── ProfileViewModel.kt
│   │
│   ├── components/                   # Shared UI components
│   │   ├── KpiCard.kt
│   │   ├── LoadingIndicator.kt
│   │   ├── ErrorView.kt
│   │   ├── EmptyStateView.kt
│   │   └── TopBar.kt
│   │
│   ├── navigation/                   # Navigation setup
│   │   ├── OwnerNavGraph.kt
│   │   ├── Screen.kt
│   │   └── BottomNavBar.kt
│   │
│   └── theme/                        # Material 3 theme
│       ├── Color.kt
│       ├── Theme.kt
│       ├── Type.kt
│       └── Shape.kt
│
├── di/                               # Dependency injection modules
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── SecurityModule.kt
│   ├── AuthInterceptor.kt
│   ├── TenantInterceptor.kt
│   └── TokenRefreshAuthenticator.kt
│
├── sync/                             # Background sync
│   ├── SyncWorker.kt
│   └── SyncManager.kt
│
└── util/                             # Utilities and helpers
    ├── CurrencyFormatter.kt
    ├── DateFormatter.kt
    ├── NetworkMonitor.kt
    ├── TokenManager.kt
    ├── BiometricManager.kt
    ├── RootDetector.kt
    ├── Resource.kt
    └── Extensions.kt
```

---

## Data Flow

### Three-Model Separation

Each data entity has three representations to maintain separation of concerns:

```
┌─────────────────────┐
│   API Response       │
│   (JSON from server) │
└──────────┬───────────┘
           ↓
┌─────────────────────┐
│   DTO                │  → Serialization from/to JSON
│   (data/dto/)        │  → Network layer only
└──────────┬───────────┘
           ↓
      ┌────┴────┐
      ↓         ↓
┌─────────┐ ┌─────────────────┐
│ Entity  │ │ Domain Model    │  → Business logic
│ (Room)  │ │ (domain/model/) │  → UI consumption
└─────────┘ └─────────────────┘
      ↑              ↑
      └──────┬───────┘
           Mappers
```

### Request Flow Example: Fetching Dashboard Data

```
1. User opens Dashboard Screen
   ↓
2. DashboardScreen triggers ViewModel.loadDashboard()
   ↓
3. DashboardViewModel calls GetDashboardKpiUseCase
   ↓
4. UseCase calls DashboardRepository.getDashboardKpis()
   ↓
5. Repository checks Room cache via DashboardKpiDao
   ├─ Cache HIT + Fresh → Return cached data (fast path)
   ├─ Cache HIT + Stale → Return stale data, fetch fresh in background
   └─ Cache MISS → Show loading, fetch from API
   ↓
6. If fetch needed: Call DashboardApiService.getDashboardStats()
   ├─ AuthInterceptor adds JWT token
   ├─ TenantInterceptor adds X-Franchise-ID header
   └─ OkHttp sends request
   ↓
7. API Response → DashboardStatsDto
   ↓
8. Mapper converts DTO → DashboardKpiEntity (save to Room)
                     → DashboardKpi (domain model)
   ↓
9. Repository emits Resource.Success(DashboardKpi)
   ↓
10. ViewModel updates UiState
    ↓
11. Screen recomposes with new data
```

### Write Flow Example: Approving an Expense

```
1. User clicks "Approve" on ApprovalDetailScreen
   ↓
2. ApprovalViewModel calls ApproveItemUseCase
   ↓
3. UseCase calls ApprovalRepository.approveItem()
   ↓
4. Repository checks network connectivity
   ├─ Online → Send request immediately
   └─ Offline → Queue in SyncQueueEntity (Room)
   ↓
5. If online: Call ApprovalApiService.processApproval()
   ↓
6. On success:
   ├─ Invalidate approval cache
   ├─ Update ApprovalEntity status in Room
   └─ Emit success to UI
   ↓
7. If offline:
   ├─ Save to sync_queue table
   ├─ Schedule SyncWorker (WorkManager)
   └─ Show "Queued for sync" message
   ↓
8. When network available: SyncWorker processes queue
```

---

## Dependency Injection (Hilt)

### Module Overview

| Module | Scope | Provides |
|--------|-------|----------|
| **AppModule** | Singleton | Application context, formatters, network monitor |
| **NetworkModule** | Singleton | OkHttpClient, Retrofit, all API services |
| **DatabaseModule** | Singleton | Room database, all DAOs |
| **RepositoryModule** | Singleton | Binds repository interfaces to implementations |
| **SecurityModule** | Singleton | EncryptedSharedPreferences, TokenManager, BiometricManager |

### Hilt Application Setup

```kotlin
@HiltAndroidApp
class OwnerHubApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.ENABLE_LOGGING) Timber.plant(Timber.DebugTree())
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

### Key Injected Components

#### 1. NetworkModule - HTTP Client Setup

```kotlin
@Provides @Singleton
fun provideOkHttpClient(
    authInterceptor: AuthInterceptor,
    tenantInterceptor: TenantInterceptor,
    tokenRefreshAuthenticator: TokenRefreshAuthenticator,
    loggingInterceptor: HttpLoggingInterceptor,
    certificatePinner: CertificatePinner
): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)           // Adds JWT token
    .addInterceptor(tenantInterceptor)         // Adds franchise ID
    .addInterceptor(loggingInterceptor)        // Debug logging
    .authenticator(tokenRefreshAuthenticator)  // Auto token refresh
    .certificatePinner(certificatePinner)      // SSL pinning
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

#### 2. AuthInterceptor - JWT Token Injection

```kotlin
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .apply {
                tokenManager.getAccessToken()?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()
        return chain.proceed(request)
    }
}
```

#### 3. TenantInterceptor - Franchise Context Injection

```kotlin
@Singleton
class TenantInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = tokenManager.getActiveFranchiseId()?.let { id ->
            chain.request().newBuilder()
                .addHeader("X-Franchise-ID", id.toString())
                .build()
        } ?: chain.request()
        return chain.proceed(request)
    }
}
```

#### 4. TokenRefreshAuthenticator - Automatic Token Refresh

Handles 401 responses by automatically refreshing the JWT token:

```kotlin
@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: dagger.Lazy<AuthApiService>
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Thread-safe token refresh with mutex
        // Prevents multiple simultaneous refresh attempts
        // Returns updated request with new token or null (redirects to login)
    }
}
```

### ViewModel Injection Example

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardKpiUseCase: GetDashboardKpiUseCase,
    private val tokenManager: TokenManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    // ViewModel implementation
}
```

---

## Franchise Context Management

### Overview

The Owner Hub supports multiple franchises per owner. The active franchise context is managed centrally and affects all data operations.

### Context Flow

```
Owner logs in
  ↓
JWT contains list of accessible franchise_ids
  ↓
App fetches franchise list from /api/owners/franchises.php
  ↓
User selects franchise (or auto-selects if single franchise)
  ↓
FranchiseContextManager stores selected franchise_id in TokenManager
  ↓
All API calls include franchise_id via TenantInterceptor
  ↓
On franchise switch:
  1. Clear all cached report data (Room)
  2. Reset ViewModels
  3. Refresh dashboard for new franchise
  4. Update approval badge count
```

### FranchiseViewModel - Context Manager

```kotlin
@HiltViewModel
class FranchiseViewModel @Inject constructor(
    private val getFranchisesUseCase: GetFranchisesUseCase,
    private val switchFranchiseUseCase: SwitchFranchiseUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _activeFranchise = MutableStateFlow<Franchise?>(null)
    val activeFranchise: StateFlow<Franchise?> = _activeFranchise.asStateFlow()

    private val _franchises = MutableStateFlow<List<Franchise>>(emptyList())
    val franchises: StateFlow<List<Franchise>> = _franchises.asStateFlow()

    fun switchFranchise(franchiseId: Long) {
        viewModelScope.launch {
            switchFranchiseUseCase(franchiseId)
            _activeFranchise.value = _franchises.value.find { it.id == franchiseId }
            // Triggers cache invalidation and data refresh across all ViewModels
        }
    }
}
```

### Cache Invalidation on Franchise Switch

```kotlin
suspend fun onFranchiseSwitch(newFranchiseId: Long) {
    // Clear franchise-specific cached data
    dashboardKpiDao.clearFranchiseKpi(newFranchiseId)
    reportCacheDao.clearFranchiseCache(newFranchiseId)
    approvalDao.clearFranchiseApprovals(newFranchiseId)

    // Franchise list is preserved (shared across franchises)
}
```

---

## Caching Strategy

### Design Philosophy

The Owner Hub follows a **stale-while-revalidate** pattern optimized for read-heavy operations:

- **Aggressive caching** for fast UI response
- **Background refresh** for data freshness
- **Graceful degradation** when offline
- **NOT full offline-first** (degrades gracefully, not source of truth)

### Cache Architecture

```
API Request → Room Cache (with TTL) → UI Display
                   ↑
           Manual Refresh bypasses cache
```

### Staleness Budgets (TTL)

| Data Type | Cache TTL | Refresh Trigger |
|-----------|-----------|-----------------|
| Dashboard KPIs | 10 minutes | Pull-to-refresh, franchise switch, app resume |
| Report data | 30 minutes | Manual refresh, date range change |
| Franchise list | 24 hours | App launch, manual refresh |
| Approval list | 5 minutes | Pull-to-refresh, after approve/reject |
| Profile data | 24 hours | After profile edit |

**Rationale:**
- Dashboard and approvals are time-sensitive (short TTL)
- Reports are historical and rarely change for a given date range (longer TTL)
- Franchise list and profile change infrequently (24-hour TTL)

### Stale-While-Revalidate Flow

```
1. ViewModel requests data via UseCase
   ↓
2. Repository checks Room cache
   ├─ Cache HIT + Not Expired (fresh)
   │  → Return cached data immediately (fast path)
   │
   ├─ Cache HIT + Expired (stale)
   │  → Return stale data immediately
   │  → Fetch fresh data in background
   │  → Update cache and emit new data
   │
   └─ Cache MISS
      → Show loading indicator
      → Fetch from API
      → Save to cache
      → Emit data
   ↓
3. On error + cache available
   → Return stale cache + show warning banner
   ↓
4. On error + no cache
   → Show error state with retry button
```

### Room Database Schema

```kotlin
@Database(
    entities = [
        FranchiseEntity::class,
        DashboardKpiEntity::class,
        ReportCacheEntity::class,
        ApprovalEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class OwnerHubDatabase : RoomDatabase() {
    abstract fun franchiseDao(): FranchiseDao
    abstract fun dashboardKpiDao(): DashboardKpiDao
    abstract fun reportCacheDao(): ReportCacheDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun syncQueueDao(): SyncQueueDao
}
```

### Cache Entity Example: DashboardKpiEntity

```kotlin
@Entity(tableName = "dashboard_kpi")
data class DashboardKpiEntity(
    @PrimaryKey val franchiseId: Long,
    val salesMtd: Double,
    val salesTarget: Double,
    val cashBalance: Double,
    val inventoryValue: Double,
    val totalBv: Double,
    val pendingApprovals: Int,
    val cachedAt: Long,      // Timestamp when cached
    val expiresAt: Long      // Timestamp when expires (cachedAt + TTL)
)
```

### Repository Caching Pattern Example

```kotlin
override fun getDashboardKpis(
    franchiseId: Long,
    forceRefresh: Boolean
): Flow<Resource<DashboardKpi>> = flow {
    val now = System.currentTimeMillis()

    // 1. Return fresh cache if available (and not force refresh)
    if (!forceRefresh) {
        kpiDao.getCachedKpi(franchiseId, now)?.let {
            emit(Resource.Success(mapper.entityToDomain(it)))
            return@flow
        }
    }

    // 2. Emit stale cache as loading state (stale-while-revalidate)
    val stale = kpiDao.getStaleKpi(franchiseId)
    emit(Resource.Loading(stale?.let { mapper.entityToDomain(it) }))

    // 3. Fetch from API
    when (val result = safeApiCall { apiService.getDashboardStats() }) {
        is Resource.Success -> {
            val entity = mapper.dtoToEntity(result.data, franchiseId, TTL_MS)
            kpiDao.cacheKpi(entity)
            emit(Resource.Success(mapper.dtoToDomain(result.data)))
        }
        is Resource.Error -> {
            // Return stale data with error message if available
            val data = stale?.let { mapper.entityToDomain(it) }
            emit(Resource.Error(result.message, data))
        }
        is Resource.Loading -> { /* not expected */ }
    }
}
```

---

## Navigation Architecture

### Bottom Navigation Tabs

The app uses bottom navigation with four main tabs:

| Tab | Icon | Root Screen | Nested Screens |
|-----|------|-------------|----------------|
| **Dashboard** | dashboard | DashboardScreen | None |
| **Reports** | bar_chart | ReportListScreen | ReportDetailScreen |
| **Approvals** | check_circle | ApprovalListScreen | ApprovalDetailScreen |
| **Profile** | person | ProfileScreen | None |

### Navigation Graph Structure

```
OwnerNavGraph
│
├── AuthGraph (not logged in)
│   └── LoginScreen
│
└── MainGraph (logged in, bottom nav)
    │
    ├── DashboardTab
    │   └── DashboardScreen
    │
    ├── ReportsTab
    │   ├── ReportListScreen
    │   └── ReportDetailScreen(reportType, dateFrom?, dateTo?)
    │
    ├── ApprovalsTab
    │   ├── ApprovalListScreen
    │   └── ApprovalDetailScreen(approvalId, approvalType)
    │
    └── ProfileTab
        └── ProfileScreen
```

### Screen Sealed Class

```kotlin
sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")

    // Main tabs
    data object Dashboard : Screen("dashboard")
    data object ReportList : Screen("reports")
    data object ApprovalList : Screen("approvals")
    data object Profile : Screen("profile")

    // Detail screens
    data class ReportDetail(
        val reportType: String = "{reportType}",
        val dateFrom: String? = null,
        val dateTo: String? = null
    ) : Screen("reports/{reportType}?dateFrom={dateFrom}&dateTo={dateTo}")

    data class ApprovalDetail(
        val approvalId: Long = 0L,
        val approvalType: String = "{approvalType}"
    ) : Screen("approvals/{approvalType}/{approvalId}")
}
```

### Report Categories

Reports are organized into six categories:

```
ReportListScreen
│
├── Sales Reports (7)
│   ├── Daily Sales
│   ├── Sales Summary
│   ├── Sales Trends
│   ├── Sales by Product
│   ├── Top Sellers
│   ├── Product Performance
│   └── Commission Report
│
├── Finance Reports (8)
│   ├── Profit & Loss
│   ├── Cash Flow
│   ├── Balance Sheet
│   ├── Expense Report
│   ├── Account Reconciliation
│   ├── Employee Debts
│   ├── Debtors
│   └── Inventory Valuation
│
├── Inventory Reports (3)
│   ├── Stock Transfer Log
│   ├── Stock Adjustment Log
│   └── Inventory Valuation
│
├── HR/Payroll Reports (3)
│   ├── Payroll Summary
│   ├── Leave Report
│   └── User Activity
│
├── Distributor Reports (5)
│   ├── Directory
│   ├── Performance
│   ├── Genealogy
│   ├── Manager Legs
│   └── Rank Report
│
└── Compliance Reports (2)
    ├── Client Map
    └── User Activity
```

### State Management

Every ViewModel exposes a single `StateFlow<UiState>`:

```kotlin
data class DashboardUiState(
    val isLoading: Boolean = true,
    val kpis: List<DashboardKpi> = emptyList(),
    val activeFranchise: Franchise? = null,
    val error: String? = null,
    val lastUpdated: Instant? = null
)
```

One-time events (navigation, snackbar) use `SharedFlow`:

```kotlin
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class NavigateTo(val route: String) : UiEvent()
    data object NavigateBack : UiEvent()
}
```

---

## Security Architecture

### Security Layers

| Layer | Mechanism | Implementation |
|-------|-----------|----------------|
| **Authentication** | JWT (access 15 min + refresh 30 days) | TokenManager |
| **Token Storage** | EncryptedSharedPreferences (AES-256-GCM) | Android Keystore backed |
| **Transport** | HTTPS + Certificate Pinning | OkHttp + Network Security Config |
| **Biometric** | AndroidX BiometricPrompt (optional) | Fingerprint/Face unlock |
| **Data at Rest** | Room DB not encrypted | Cache only, no secrets |
| **Build Security** | ProGuard/R8 obfuscation | Log stripping, code obfuscation |
| **Device Integrity** | Root detection | Warning only, not blocking |

### JWT Token Lifecycle

```
Login (username + password)
  ↓
Server returns: { access_token (15 min), refresh_token (30 days) }
  ↓
Store in EncryptedSharedPreferences
  ↓
Every API Request:
  AuthInterceptor adds "Authorization: Bearer {access_token}"
  ↓
On 401 Response:
  TokenRefreshAuthenticator:
    ├─ Refresh success → Retry request with new token
    └─ Refresh fail → Clear tokens → Navigate to login
```

### Token Expiry Handling

| Scenario | Action |
|----------|--------|
| Access token expired | Automatic refresh via TokenRefreshAuthenticator |
| Refresh token expired | Clear all tokens, redirect to login |
| Refresh token revoked | 401 on refresh attempt, redirect to login |
| 30 days inactivity | Refresh token expired, must re-login |

### EncryptedSharedPreferences Setup

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "ownerhub_secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

**What is stored (encrypted):**
- `access_token` - JWT access token
- `refresh_token` - JWT refresh token
- `active_franchise_id` - Currently selected franchise
- `owner_id` - Authenticated owner's user ID
- `last_activity_timestamp` - For session timeout

### Certificate Pinning

#### OkHttp Configuration

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("app.dynapharm-dms.com", "sha256/PRIMARY_PIN_HASH")
    .add("app.dynapharm-dms.com", "sha256/BACKUP_PIN_HASH")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

#### Pin Rotation Strategy

1. Add new cert pin as backup in app update
2. Deploy new server certificate
3. Promote new pin to primary in subsequent app update

### Biometric Authentication

Optional convenience lock (not a replacement for JWT):

```
App Launch (already logged in)
  ↓
Check: Is biometric enabled in preferences?
  ├─ No → Show full app (already has valid tokens)
  └─ Yes → Show biometric prompt
             ├─ Success → Show full app
             ├─ "Use Password" → Show login screen
             └─ Error → Show login screen
```

### Root Detection

Checks for rooted devices:
- Root binaries (/system/bin/su, etc.)
- Su command availability
- Root management apps (Magisk, SuperSU)

**Policy:** Rooted devices show a warning banner but are NOT blocked.

### Session Timeout

- **Inactivity timeout:** 30 days
- Tracked via `last_activity_timestamp` in EncryptedSharedPreferences
- On app resume, if inactive for 30+ days, tokens are cleared

### Data Classification

| Data Type | Sensitivity | Storage | Encryption |
|-----------|-------------|---------|------------|
| JWT tokens | **High** | EncryptedSharedPreferences | AES-256-GCM |
| Owner ID | Medium | EncryptedSharedPreferences | AES-256-GCM |
| Franchise ID | Medium | EncryptedSharedPreferences | AES-256-GCM |
| Dashboard KPIs | Low (aggregate) | Room DB | None (cache) |
| Report data | Low (aggregate) | Room DB | None (cache) |
| Approval queue | Medium | Room DB + sync_queue | None (cache) |

### Release Build Protections

| Rule | Implementation |
|------|---------------|
| No token logging | ProGuard strips Timber.d/v/i calls |
| No password clipboard | `android:importantForAutofill="no"` |
| No login screenshots | `FLAG_SECURE` on login screen |
| Clear cache on logout | Room database cleared |

---

## Offline-First Approach

### Philosophy

The Owner Hub is **read-heavy with minimal writes**, which shapes the offline strategy:

- 95% of operations are data retrieval (reports, dashboards)
- 4% are approval actions
- 1% are profile edits

### Offline Capabilities

#### Read Operations (95%)

**Fully offline capable with cache:**
- Dashboard KPIs (10-minute cache)
- All reports (30-minute cache)
- Approval list (5-minute cache)
- Profile data (24-hour cache)

**Behavior when offline:**
1. Show cached data immediately
2. Display "Offline Mode" banner at top
3. Disable pull-to-refresh (network required)
4. Show last updated timestamp

#### Write Operations (5%)

**Approval actions (approve/reject):**
- Queued in `sync_queue` table when offline
- Processed by `ApprovalSyncWorker` when network available
- User sees "Queued for sync" confirmation

**Profile edits:**
- Require network connection (not queued)
- Show "Network required" error if offline

### Sync Queue Architecture

#### SyncQueueEntity

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operation: String,      // "approve" | "reject"
    val endpoint: String,       // Full API endpoint path
    val method: String,         // "POST"
    val payload: String,        // JSON body
    val status: String,         // "pending" | "processing" | "completed" | "failed"
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val createdAt: Long,
    val processedAt: Long? = null,
    val errorMessage: String? = null
)
```

#### ApprovalSyncWorker (WorkManager)

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
                syncQueueDao.updateStatus(item.id, "failed",
                    System.currentTimeMillis(), "Max retries exceeded")
                continue
            }

            try {
                // Process sync item
                // ...
            } catch (e: Exception) {
                syncQueueDao.incrementRetry(item.id)
                allSuccess = false
            }
        }

        return if (allSuccess) Result.success() else Result.retry()
    }
}
```

### Network Monitoring

```kotlin
@Singleton
class NetworkMonitor @Inject constructor(
    private val context: Context
) {
    val isOnline: Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)

        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
```

### Cache Invalidation Rules

| Trigger | Action |
|---------|--------|
| Franchise switch | Clear all cached data EXCEPT franchise list |
| Logout | Clear ALL cached data (Room + EncryptedPrefs) |
| TTL expiry | Fetch fresh data, return stale while loading |
| Manual pull-to-refresh | Bypass cache, fetch fresh from API |
| After approve/reject | Invalidate approval list cache |
| After profile edit | Invalidate profile cache |
| App launch | Check franchise list freshness, refresh dashboard |

### Conflict Resolution

**Server wins** for all approval actions:

```
1. User approves expense while offline → queued in sync_queue
2. Meanwhile, another owner approves same expense from web
3. Device comes online → SyncWorker sends approve request
4. Server responds "already processed"
5. App accepts server state, shows: "This item was already approved"
```

This prevents double-approvals and maintains server as single source of truth.

---

## Technology Stack

### Core Android

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 2.0.10 |
| Min SDK | Android 8.0 (API 26) | 26 |
| Target SDK | Android 14 (API 34) | 34 |
| Build System | Gradle + Kotlin DSL | 8.5.1 |
| Java Version | Java 17 | 17 |

### UI Framework

| Component | Technology | Version |
|-----------|-----------|---------|
| UI Toolkit | Jetpack Compose | BOM 2024.06.00 |
| Design System | Material 3 | (via Compose BOM) |
| Navigation | Navigation Compose | 2.7.7 |
| Image Loading | Coil | 2.7.0 |
| Splash Screen | Core Splashscreen | 1.0.1 |

### Architecture Components

| Component | Technology | Version |
|-----------|-----------|---------|
| Lifecycle | Lifecycle Extensions | 2.8.3 |
| ViewModel | Lifecycle ViewModel Compose | 2.8.3 |
| Dependency Injection | Dagger Hilt | 2.51.1 |
| Background Tasks | WorkManager | 2.9.0 |
| Preferences | DataStore Preferences | 1.1.1 |

### Networking

| Component | Technology | Version |
|-----------|-----------|---------|
| HTTP Client | OkHttp | 4.12.0 |
| REST Client | Retrofit | 2.11.0 |
| Serialization | Kotlin Serialization | 1.7.1 |
| Converter | Retrofit Kotlin Serialization | 1.0.0 |

### Data Persistence

| Component | Technology | Version |
|-----------|-----------|---------|
| Local Database | Room | 2.6.1 |
| Annotation Processor | KSP | 2.0.10-1.0.24 |

### Security

| Component | Technology | Version |
|-----------|-----------|---------|
| Encrypted Storage | Security Crypto | 1.1.0-alpha06 |
| Biometric Auth | Biometric | 1.2.0-alpha05 |
| Certificate Pinning | OkHttp CertificatePinner | 4.12.0 |

### Concurrency

| Component | Technology | Version |
|-----------|-----------|---------|
| Coroutines | Kotlin Coroutines | 1.8.1 |
| Flow | Kotlin Flow | (part of Coroutines) |

### Logging & Debugging

| Component | Technology | Version |
|-----------|-----------|---------|
| Logging | Timber | 5.0.1 |
| HTTP Logging | OkHttp Logging Interceptor | 4.12.0 |

### Testing

| Component | Technology | Version |
|-----------|-----------|---------|
| Unit Testing | JUnit 5 | 5.10.3 |
| Mocking | MockK | 1.13.11 |
| Flow Testing | Turbine | 1.1.0 |
| Assertions | Truth | 1.4.4 |
| Coroutine Testing | Kotlinx Coroutines Test | 1.8.1 |
| UI Testing | Compose UI Test JUnit4 | (via Compose BOM) |
| Instrumentation | Espresso Core | 3.6.1 |
| Hilt Testing | Hilt Android Testing | 2.51.1 |

---

## Build Configuration

### Build Variants

| Property | debug | staging | release |
|----------|-------|---------|---------|
| **Application ID Suffix** | `.debug` | `.staging` | (none) |
| **Version Name Suffix** | `-debug` | `-staging` | (none) |
| **Minify Enabled** | false | true | true |
| **Shrink Resources** | false | false | true |
| **API Base URL** | `http://10.0.2.2/DMS_web/` | `https://staging.dynapharm-dms.com/` | `https://app.dynapharm-dms.com/` |
| **Logging Enabled** | true | true | false |
| **Certificate Pins** | (empty) | staging pin | production pins |

### Build Types Purpose

- **debug**: Local development with emulator/device, no obfuscation
- **staging**: Pre-production testing with staging backend
- **release**: Production build with full optimization

### ProGuard/R8 Configuration

Key optimizations enabled in release builds:

```proguard
# Kotlin Serialization - Keep serializers
-keep,includedescriptorclasses class com.dynapharm.ownerhub.**$$serializer { *; }

# Retrofit - Keep API interfaces
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Room - Keep database classes
-keep class * extends androidx.room.RoomDatabase

# DTOs and Domain Models - Keep all fields
-keep class com.dynapharm.ownerhub.data.dto.** { *; }
-keep class com.dynapharm.ownerhub.data.db.entity.** { *; }
-keep class com.dynapharm.ownerhub.domain.model.** { *; }

# Timber - Strip debug/verbose/info logs in release
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
```

### Kotlin Compiler Options

```kotlin
kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += listOf(
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    )
}
```

### Compose Compiler Configuration

```kotlin
composeCompiler {
    enableStrongSkippingMode = true
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}
```

---

## Additional Resources

### Related Documentation

- **Product Requirements:** `docs/android-app-owner/01_PRD.md`
- **Software Requirements:** `docs/android-app-owner/02_SRS.md`
- **Software Design Spec:** `docs/android-app-owner/03_SDS.md`
- **API Contract:** `docs/android-app-owner/04_API_CONTRACT.md`
- **User Journeys:** `docs/android-app-owner/05_USER_JOURNEYS.md`
- **Testing Strategy:** `docs/android-app-owner/06_TESTING_STRATEGY.md`
- **Release Plan:** `docs/android-app-owner/07_RELEASE_PLAN.md`

### Detailed SDS Sub-Documents

- **Architecture Details:** `docs/android-app-owner/sds/01-architecture.md`
- **Gradle Configuration:** `docs/android-app-owner/sds/02-gradle-config.md`
- **Hilt Modules:** `docs/android-app-owner/sds/03-hilt-modules.md`
- **Offline Caching:** `docs/android-app-owner/sds/04-offline-sync.md`
- **Security:** `docs/android-app-owner/sds/05-security.md`
- **Networking:** `docs/android-app-owner/sds/06-networking.md`

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-09 | Development Team | Initial architecture documentation |

---

**End of Architecture Documentation**
