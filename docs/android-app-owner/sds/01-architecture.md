# SDS 01 -- Architecture Overview

**Parent:** [03_SDS.md](../03_SDS.md) | [All Docs](../README.md)

---

## 1. Architectural Pattern

**MVVM + Clean Architecture** with three strictly separated layers.

### Layer Diagram

```
+---------------------------------------------+
|              PRESENTATION LAYER              |
|  Compose Screens <- ViewModels <- UiState    |
+---------------------------------------------+
|                DOMAIN LAYER                  |
|  UseCases <- Repository Interfaces           |
+---------------------------------------------+
|                 DATA LAYER                   |
|  Repository Impl -> Remote (Retrofit)        |
|                  -> Local (Room Cache)        |
+---------------------------------------------+
```

### Data Flow

```
Screen -> ViewModel -> UseCase -> Repository -> Remote API
                                             -> Room Cache
```

### Dependency Direction

```
Presentation  --->  Domain  <---  Data
   (knows)         (knows       (implements
    Domain)        nothing)      Domain)
```

---

## 2. Layer Rules

| Layer | Depends On | Contains | Kotlin Type |
|-------|-----------|----------|-------------|
| **Presentation** | Domain only | Screens, ViewModels, UI state, navigation | Android + Compose |
| **Domain** | Nothing (pure Kotlin) | Models, UseCases, Repository interfaces | Pure Kotlin |
| **Data** | Domain | API services, Room DAOs, DTOs, Mappers, Repository implementations | Android + Libraries |

**Enforcement:** Hilt modules wire Data implementations to Domain interfaces at compile time.

---

## 3. Three-Model Separation

Each data entity has three representations:

| Model Type | Location | Purpose | Example |
|------------|----------|---------|---------|
| **DTO** | `data/dto/` | JSON serialization from API responses | `DashboardStatsDto` |
| **Entity** | `data/db/entity/` | Room table mapping for local cache | `DashboardKpiEntity` |
| **Domain Model** | `domain/model/` | Business logic and UI consumption | `DashboardKpi` |

**Mappers** in `data/mapper/` convert between the three:

```
API Response (JSON)
    |
    v
  DTO  -------> Domain Model -------> UI State
    |                ^
    v                |
 Entity  -----------+
 (Room)
```

---

## 4. Complete Project Structure

```
com.dynapharm.ownerhub/
|
+-- data/
|   +-- api/
|   |   +-- AuthApiService.kt
|   |   +-- DashboardApiService.kt
|   |   +-- FranchiseApiService.kt
|   |   +-- ReportApiService.kt
|   |   +-- ApprovalApiService.kt
|   |   +-- ProfileApiService.kt
|   |
|   +-- db/
|   |   +-- OwnerHubDatabase.kt
|   |   +-- dao/
|   |   |   +-- FranchiseDao.kt
|   |   |   +-- DashboardKpiDao.kt
|   |   |   +-- ReportCacheDao.kt
|   |   |   +-- ApprovalDao.kt
|   |   |   +-- SyncQueueDao.kt
|   |   +-- entity/
|   |       +-- OwnerEntity.kt
|   |       +-- FranchiseEntity.kt
|   |       +-- DashboardKpiEntity.kt
|   |       +-- ReportCacheEntity.kt
|   |       +-- ApprovalEntity.kt
|   |       +-- SyncQueueEntity.kt
|   |
|   +-- dto/
|   |   +-- auth/
|   |   |   +-- LoginRequest.kt
|   |   |   +-- LoginResponse.kt
|   |   |   +-- RefreshTokenResponse.kt
|   |   +-- dashboard/
|   |   |   +-- DashboardStatsDto.kt
|   |   +-- franchise/
|   |   |   +-- FranchiseDto.kt
|   |   +-- report/
|   |   |   +-- ReportRequestDto.kt
|   |   |   +-- ReportResponseDto.kt
|   |   +-- approval/
|   |       +-- ApprovalDto.kt
|   |       +-- ApprovalActionRequest.kt
|   |
|   +-- mapper/
|   |   +-- FranchiseMapper.kt
|   |   +-- DashboardMapper.kt
|   |   +-- ReportMapper.kt
|   |   +-- ApprovalMapper.kt
|   |
|   +-- repository/
|       +-- AuthRepositoryImpl.kt
|       +-- DashboardRepositoryImpl.kt
|       +-- FranchiseRepositoryImpl.kt
|       +-- ReportRepositoryImpl.kt
|       +-- ApprovalRepositoryImpl.kt
|       +-- ProfileRepositoryImpl.kt
|
+-- domain/
|   +-- model/
|   |   +-- Owner.kt
|   |   +-- Franchise.kt
|   |   +-- DashboardKpi.kt
|   |   +-- ReportData.kt
|   |   +-- Approval.kt
|   |   +-- ApprovalAction.kt
|   |
|   +-- repository/
|   |   +-- AuthRepository.kt
|   |   +-- DashboardRepository.kt
|   |   +-- FranchiseRepository.kt
|   |   +-- ReportRepository.kt
|   |   +-- ApprovalRepository.kt
|   |   +-- ProfileRepository.kt
|   |
|   +-- usecase/
|       +-- auth/
|       |   +-- LoginUseCase.kt
|       |   +-- LogoutUseCase.kt
|       |   +-- RefreshTokenUseCase.kt
|       +-- dashboard/
|       |   +-- GetDashboardKpiUseCase.kt
|       +-- franchise/
|       |   +-- GetFranchisesUseCase.kt
|       |   +-- SwitchFranchiseUseCase.kt
|       +-- report/
|       |   +-- GetReportUseCase.kt
|       |   +-- ExportReportUseCase.kt
|       +-- approval/
|       |   +-- GetApprovalsUseCase.kt
|       |   +-- ApproveItemUseCase.kt
|       |   +-- RejectItemUseCase.kt
|       +-- profile/
|           +-- GetProfileUseCase.kt
|           +-- UpdateProfileUseCase.kt
|
+-- presentation/
|   +-- auth/
|   |   +-- LoginScreen.kt
|   |   +-- LoginViewModel.kt
|   +-- dashboard/
|   |   +-- DashboardScreen.kt
|   |   +-- DashboardViewModel.kt
|   +-- franchise/
|   |   +-- FranchiseSwitcherSheet.kt
|   |   +-- FranchiseViewModel.kt
|   +-- reports/
|   |   +-- ReportListScreen.kt
|   |   +-- ReportDetailScreen.kt
|   |   +-- ReportViewModel.kt
|   |   +-- components/
|   |       +-- DateRangeSelector.kt
|   |       +-- ReportTable.kt
|   |       +-- SummaryCard.kt
|   |       +-- QuickDatePresets.kt
|   +-- approvals/
|   |   +-- ApprovalListScreen.kt
|   |   +-- ApprovalDetailScreen.kt
|   |   +-- ApprovalViewModel.kt
|   |   +-- components/
|   |       +-- ApprovalCard.kt
|   |       +-- ApprovalActionDialog.kt
|   +-- profile/
|   |   +-- ProfileScreen.kt
|   |   +-- ProfileViewModel.kt
|   +-- components/
|   |   +-- KpiCard.kt
|   |   +-- LoadingIndicator.kt
|   |   +-- ErrorView.kt
|   |   +-- EmptyStateView.kt
|   |   +-- TopBar.kt
|   +-- navigation/
|   |   +-- OwnerNavGraph.kt
|   |   +-- Screen.kt
|   |   +-- BottomNavBar.kt
|   +-- theme/
|       +-- Color.kt
|       +-- Theme.kt
|       +-- Type.kt
|       +-- Shape.kt
|
+-- di/
|   +-- AppModule.kt
|   +-- NetworkModule.kt
|   +-- DatabaseModule.kt
|   +-- RepositoryModule.kt
|   +-- SecurityModule.kt
|
+-- sync/
|   +-- SyncWorker.kt
|   +-- SyncManager.kt
|
+-- util/
    +-- CurrencyFormatter.kt
    +-- DateFormatter.kt
    +-- NetworkMonitor.kt
    +-- Resource.kt
    +-- Extensions.kt
```

---

## 5. Navigation Architecture

### Bottom Navigation Tabs

| Tab | Icon | Root Screen | Nested Screens |
|-----|------|-------------|----------------|
| **Dashboard** | `dashboard` | `DashboardScreen` | None |
| **Reports** | `bar_chart` | `ReportListScreen` | `ReportDetailScreen` |
| **Approvals** | `check_circle` | `ApprovalListScreen` | `ApprovalDetailScreen` |
| **Profile** | `person` | `ProfileScreen` | None |

### Navigation Graph

```
OwnerNavGraph
|
+-- AuthGraph (not logged in)
|   +-- LoginScreen
|
+-- MainGraph (logged in, bottom nav)
    +-- DashboardTab
    |   +-- DashboardScreen
    |
    +-- ReportsTab
    |   +-- ReportListScreen
    |   +-- ReportDetailScreen(reportType, dateFrom?, dateTo?)
    |
    +-- ApprovalsTab
    |   +-- ApprovalListScreen
    |   +-- ApprovalDetailScreen(approvalId, approvalType)
    |
    +-- ProfileTab
        +-- ProfileScreen
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

### Report Categories (Nested Navigation)

```
ReportListScreen
|
+-- Sales Reports (7)
|   +-- Daily Sales
|   +-- Sales Summary
|   +-- Sales Trends
|   +-- Sales by Product
|   +-- Top Sellers
|   +-- Product Performance
|   +-- Commission Report
|
+-- Finance Reports (8)
|   +-- Profit & Loss
|   +-- Cash Flow
|   +-- Balance Sheet
|   +-- Expense Report
|   +-- Account Reconciliation
|   +-- Employee Debts
|   +-- Debtors
|   +-- Inventory Valuation
|
+-- Inventory Reports (3)
|   +-- Stock Transfer Log
|   +-- Stock Adjustment Log
|   +-- Inventory Valuation
|
+-- HR/Payroll Reports (3)
|   +-- Payroll Summary
|   +-- Leave Report
|   +-- User Activity
|
+-- Distributor Reports (5)
|   +-- Directory
|   +-- Performance
|   +-- Genealogy
|   +-- Manager Legs
|   +-- Rank Report
|
+-- Compliance Reports (2)
    +-- Client Map
    +-- User Activity
```

---

## 6. State Management

### UiState Pattern

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

### Side Effects

One-time events (navigation, snackbar) use `SharedFlow`:

```kotlin
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class NavigateTo(val route: String) : UiEvent()
    data object NavigateBack : UiEvent()
}
```

---

## 7. Franchise Context

The active franchise is stored in `FranchiseViewModel` (scoped to `SingletonComponent`):

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
            // Triggers data refresh across all other ViewModels
        }
    }
}
```

When the franchise changes, all cached data is invalidated and ViewModels re-fetch.

---

## 8. Error Handling Strategy

| Error Type | Handling | User Feedback |
|-----------|---------|---------------|
| Network timeout | Retry with backoff, show cached data | Snackbar with retry |
| 401 Unauthorized | Token refresh, if fails -> login | Redirect to login |
| 403 Forbidden | Log, show error | "Access denied" dialog |
| 500 Server Error | Log, show generic error | Snackbar with retry |
| No network | Show cached data if available | Banner "Offline mode" |
| Parse error | Log, show generic error | Snackbar |

---

## 9. Localization Architecture

Five languages supported: English (en), French (fr), Arabic (ar), Swahili (sw), Spanish (es).

### Resource Structure

```
res/
+-- values/          (English - default)
|   +-- strings.xml
+-- values-fr/       (French)
|   +-- strings.xml
+-- values-ar/       (Arabic - RTL)
|   +-- strings.xml
+-- values-sw/       (Swahili)
|   +-- strings.xml
+-- values-es/       (Spanish)
    +-- strings.xml
```

### RTL Support

Arabic requires RTL layout mirroring:
- Use `start`/`end` instead of `left`/`right`
- Enable `android:supportsRtl="true"` in manifest
- Compose: `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`

### Translation Mapping

Web `src/lang/{lang}.php` keys are mapped to Android `strings.xml` during build.

---

## 10. Cross-References

| Topic | Document |
|-------|----------|
| Gradle configuration | [02-gradle-config.md](02-gradle-config.md) |
| Hilt DI modules | [03-hilt-modules.md](03-hilt-modules.md) |
| Offline caching | [04-offline-sync.md](04-offline-sync.md) |
| Security | [05-security.md](05-security.md) |
| Networking | [06-networking.md](06-networking.md) |
| API endpoints | [../api-contract/](../api-contract/) |
| Functional requirements | [../srs/01-functional-requirements.md](../srs/01-functional-requirements.md) |
