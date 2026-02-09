# Claude Code Development Guide - Dynapharm Owner Hub

**Version:** 1.1
**Last Updated:** 2026-02-10
**App:** Dynapharm Owner Hub (Android)
**Package:** com.dynapharm.ownerhub

---

## Purpose

This document provides AI development patterns and instructions for Claude Code when working on the Dynapharm Owner Hub Android app. It serves as a concise reference hub for architecture patterns, coding standards, testing requirements, and common workflows.

---

## Quick Reference

### Project Type
- Modern Android app with Jetpack Compose
- Read-heavy business intelligence app (95% reads, 5% writes)
- Multi-franchise owner portal with offline-first caching

### Core Technologies
- Language: Kotlin 2.0.10
- UI: Jetpack Compose + Material 3
- Architecture: MVVM + Clean Architecture (3 layers)
- DI: Dagger Hilt
- Network: Retrofit + OkHttp + Kotlin Serialization
- Database: Room with stale-while-revalidate caching
- Async: Kotlin Coroutines + Flow
- Testing: JUnit 5 + MockK + Turbine + Compose UI Test

### Build Variants & API Configuration

**CRITICAL: Development Environment Pattern**

For local development with WAMP virtual hosts, always use this pattern:

1. **Base URL:** Use host machine's LAN IP address (e.g., `http://192.168.1.5/dms_web/`)
   - NEVER use `10.0.2.2` or `localhost` - they don't work with WAMP virtual hosts
   - Find LAN IP: `ipconfig` → Look for IPv4 under Wi-Fi or Ethernet adapter

2. **HostHeaderInterceptor:** Add `Host: dynapharm.peter` header for virtual host routing
   - Required because WAMP needs correct Host header to route requests
   - Only active in dev mode (when base URL contains local IP)

3. **Network Security Config:** Allow cleartext (HTTP) for local IPs
   ```xml
   <!-- res/xml/network_security_config.xml -->
   <network-security-config>
       <domain-config cleartextTrafficPermitted="true">
           <domain includeSubdomains="true">192.168.1.5</domain>
       </domain-config>
   </network-security-config>

   <!-- AndroidManifest.xml -->
   <application android:networkSecurityConfig="@xml/network_security_config">
   ```

4. **Implementation:**
   ```kotlin
   // build.gradle.kts (dev flavor)
   buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.5/dms_web/\"")

   // HostHeaderInterceptor.kt
   if (BuildConfig.API_BASE_URL.contains("192.168.")) {
       request.newBuilder().header("Host", "dynapharm.peter").build()
   }

   // NetworkModule.kt
   OkHttpClient.Builder()
       .addInterceptor(hostHeaderInterceptor)  // MUST be first
       .addInterceptor(authInterceptor)
       // ... other interceptors
   ```

**Build Variants:**
- `dev`: Local WAMP server
  - API: `http://192.168.1.5/dms_web/` (LAN IP + project path)
  - Host Header: `dynapharm.peter` (virtual host name)
  - Logging: Enabled
  - Cert Pinning: Disabled

- `staging`: Pre-production server
  - API: `https://erp.dynapharmafrica.com/`
  - No Host header needed (real domain)
  - Logging: Enabled
  - Cert Pinning: Enabled

- `prod`: Production server
  - API: `https://clouderp.dynapharmafrica.com/`
  - No Host header needed (real domain)
  - Logging: Disabled
  - Cert Pinning: Enabled

---

## Architecture Patterns

### Layer Structure (Clean Architecture)

```
Presentation (UI) -> Domain (Business Logic) -> Data (API + Cache)
```

**Dependency Rule:** Presentation depends on Domain. Data implements Domain interfaces. Domain depends on nothing (pure Kotlin).

#### Package Structure
```
com.dynapharm.ownerhub/
├── data/           # DTOs, Room entities, API services, Repository impls
│   ├── api/        # Retrofit service interfaces
│   ├── db/         # Room: entities, DAOs, database
│   ├── dto/        # JSON serialization classes (API responses)
│   ├── mapper/     # DTO <-> Entity <-> Domain converters
│   └── repository/ # Repository implementations
├── domain/         # Pure Kotlin (no Android deps)
│   ├── model/      # Business entities (DashboardKpi, Franchise, etc.)
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Single-responsibility business logic
├── presentation/   # Compose screens, ViewModels, navigation
│   ├── auth/       # Login screen + ViewModel
│   ├── dashboard/  # Dashboard screen + ViewModel
│   ├── reports/    # Report list/detail screens + components
│   ├── approvals/  # Approval list/detail screens + components
│   ├── profile/    # Profile screen + ViewModel
│   ├── components/ # Shared composables (KpiCard, ErrorView, etc.)
│   ├── navigation/ # NavGraph, Screen sealed class, BottomNavBar
│   └── theme/      # Material 3 theme (colors, typography, shapes)
├── di/             # Hilt modules + interceptors
├── sync/           # WorkManager background sync
└── util/           # Formatters, NetworkMonitor, TokenManager, etc.
```

### Three-Model Separation

Every data entity has three representations:

1. **DTO** (`data/dto/`): JSON serialization from API
   - `@Serializable` Kotlin classes
   - Matches API response structure exactly
   - Used ONLY in network layer

2. **Entity** (`data/db/entity/`): Room database tables
   - `@Entity` annotated classes
   - Cache representation with TTL fields
   - Used ONLY in local database

3. **Domain Model** (`domain/model/`): Business logic and UI consumption
   - Pure Kotlin data classes
   - Clean API for ViewModels
   - No Android or serialization dependencies

**Mappers** convert between the three in `data/mapper/`.

### State Management Pattern

Every ViewModel follows this pattern:

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardKpiUseCase: GetDashboardKpiUseCase
) : ViewModel() {

    // UI State (single source of truth)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // One-time events (navigation, snackbar)
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun loadDashboard(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            getDashboardKpiUseCase(forceRefresh)
                .collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                        is Resource.Success -> _uiState.update {
                            it.copy(isLoading = false, data = resource.data)
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(isLoading = false, error = resource.message) }
                            _uiEvent.emit(UiEvent.ShowSnackbar(resource.message))
                        }
                    }
                }
        }
    }
}
```

### Caching Strategy (Stale-While-Revalidate)

Repository pattern for read-heavy operations:

```kotlin
override fun getDashboardKpis(
    franchiseId: Long,
    forceRefresh: Boolean
): Flow<Resource<DashboardKpi>> = flow {
    val now = System.currentTimeMillis()

    // 1. Return fresh cache if available (fast path)
    if (!forceRefresh) {
        dao.getFreshStats(franchiseId, now)?.let {
            emit(Resource.Success(mapper.entityToDomain(it)))
            return@flow
        }
    }

    // 2. Emit stale cache while fetching fresh (stale-while-revalidate)
    val stale = dao.getStaleStats(franchiseId)
    emit(Resource.Loading(stale?.let { mapper.entityToDomain(it) }))

    // 3. Fetch from API
    try {
        val response = apiService.getDashboardStats()
        if (response.success && response.data != null) {
            val entity = mapper.dtoToEntity(response.data, franchiseId, now)
            dao.upsertStats(entity)
            emit(Resource.Success(mapper.entityToDomain(entity)))
        } else {
            emit(Resource.Error(response.message ?: "Load failed",
                stale?.let { mapper.entityToDomain(it) }))
        }
    } catch (e: Exception) {
        emit(Resource.Error(e.message ?: "Network error",
            stale?.let { mapper.entityToDomain(it) }))
    }
}
```

**Cache TTL Values:**
- Dashboard KPIs: 10 minutes
- Report data: 30 minutes
- Franchise list: 24 hours
- Approval list: 5 minutes
- Profile data: 24 hours

---

## Coding Standards

### Kotlin Style

1. **Use immutability by default**: `val` over `var`, immutable collections
2. **Leverage Kotlin idioms**:
   - Use `?.let`, `?.also`, `?.takeIf` for null safety
   - Use `when` expressions over if-else chains
   - Prefer extension functions for utility methods
3. **Coroutines best practices**:
   - Use `viewModelScope` in ViewModels (auto-cancels on clear)
   - Use `Flow` for reactive streams, not LiveData
   - Avoid `GlobalScope` - always use structured concurrency
4. **Naming conventions**:
   - `_mutableState` (private) + `state` (public) pattern for StateFlow
   - UseCases: `GetDashboardKpiUseCase`, `ApproveItemUseCase` (verb-noun)
   - Repositories: `DashboardRepository`, `ApprovalRepository` (noun)
   - DTOs: `DashboardStatsDto` (append Dto)
   - Entities: `DashboardStatsEntity` (append Entity)

### Compose Best Practices

1. **Hoisting state**: Pass state down, events up
   ```kotlin
   @Composable
   fun DashboardScreen(
       uiState: DashboardUiState,
       onRefresh: () -> Unit,
       onNavigateToReport: (String) -> Unit
   )
   ```

2. **Preview annotations**: Add `@Preview` for all standalone composables
   ```kotlin
   @Preview(showBackground = true)
   @Composable
   private fun DashboardScreenPreview() {
       OwnerHubTheme {
           DashboardScreen(
               uiState = DashboardUiState(...),
               onRefresh = {},
               onNavigateToReport = {}
           )
       }
   }
   ```

3. **Stable collections**: Wrap lists in `persistentListOf()` or use `@Immutable`
4. **Avoid side effects in composition**: Use `LaunchedEffect`, `SideEffect`, `DisposableEffect`
5. **Material 3 components**: Always use M3 variants (not M2)
   - **CRITICAL**: This project uses Material 3 (androidx.compose.material3)
   - NEVER use Material 2 (androidx.compose.material) components except for pull-refresh
   - Material 3 APIs can differ between versions - follow patterns below

6. **Error Dialog Pattern (SweetAlert-style)**:
   ```kotlin
   // Show errors with Material 3 AlertDialog instead of Snackbar
   AlertDialog(
       onDismissRequest = { showError = false },
       icon = {
           Icon(
               imageVector = Icons.Filled.Error,
               contentDescription = "Error",
               tint = MaterialTheme.colorScheme.error,
               modifier = Modifier.size(64.dp)
           )
       },
       title = {
           Text(
               text = "Operation Failed",
               style = MaterialTheme.typography.headlineSmall,
               fontWeight = FontWeight.Bold
           )
       },
       text = {
           Text(
               text = errorMessage,
               style = MaterialTheme.typography.bodyLarge,
               textAlign = TextAlign.Center
           )
       },
       confirmButton = {
           Button(onClick = { showError = false }, modifier = Modifier.fillMaxWidth()) {
               Text("OK")
           }
       },
       containerColor = MaterialTheme.colorScheme.surface,
       tonalElevation = 6.dp
   )
   ```

7. **Logout Pattern**: Always visible in TopAppBar
   ```kotlin
   @OptIn(ExperimentalMaterial3Api::class)
   TopAppBar(
       title = { Text("App Name") },
       actions = {
           IconButton(onClick = { viewModel.logout(); onLogout() }) {
               Icon(Icons.Default.Logout, "Logout")
           }
       }
   )
   ```

### Material 3 Compatibility Guidelines

**Version:** Material 3 1.3.x (via compose-bom 2024.12.01)

1. **ExposedDropdownMenuBox Pattern** (CRITICAL):
   ```kotlin
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   fun MySelector() {
       ExposedDropdownMenuBox(
           expanded = expanded,
           onExpandedChange = { expanded = it }
       ) {
           OutlinedTextField(
               // ...
               modifier = Modifier
                   .fillMaxWidth()
                   .menuAnchor()  // NO PARAMETERS - MenuAnchorType removed
           )
           ExposedDropdownMenu(...) { /* items */ }
       }
   }
   ```
   - **DO NOT** use `MenuAnchorType` parameter (removed in newer M3 versions)
   - Use `.menuAnchor()` without parameters

2. **Confirmed Working Icons** (material-icons-extended:1.7.6):
   ```kotlin
   // Confirmed available in Icons.Default:
   Icons.Default.Store          // Franchise/business icon
   Icons.Default.Check          // Checkmark/selection
   Icons.Default.SwapHoriz      // Swap/switch action
   Icons.Default.Visibility     // Show password
   Icons.Default.VisibilityOff  // Hide password
   Icons.Default.AccountBalance // Finance/banking
   Icons.Default.Assessment     // Reports/analytics
   Icons.Default.CheckCircle    // Approvals
   ```
   - ONLY use icons from the confirmed list above
   - If you need a new icon, test it first or ask user for confirmation

3. **AlertDialog Pattern**:
   ```kotlin
   AlertDialog(
       onDismissRequest = { /* dismiss logic */ },
       title = { Text("Title") },
       text = { /* content */ },
       confirmButton = {
           TextButton(onClick = { /* action */ }) { Text("OK") }
       },
       dismissButton = {
           TextButton(onClick = { /* dismiss */ }) { Text("Cancel") }
       }
   )
   ```

4. **Card Pattern**:
   ```kotlin
   Card(
       modifier = Modifier.fillMaxWidth(),
       colors = CardDefaults.cardColors(
           containerColor = MaterialTheme.colorScheme.tertiaryContainer,
           contentColor = MaterialTheme.colorScheme.onTertiaryContainer
       ),
       elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
   ) { /* content */ }
   ```

5. **Color Scheme Usage**:
   ```kotlin
   // Primary colors
   MaterialTheme.colorScheme.primary
   MaterialTheme.colorScheme.onPrimary

   // Tertiary (subtle highlights - used for franchise banner)
   MaterialTheme.colorScheme.tertiaryContainer
   MaterialTheme.colorScheme.onTertiaryContainer

   // Error colors
   MaterialTheme.colorScheme.error
   MaterialTheme.colorScheme.onError
   ```

6. **TextField Pattern**:
   ```kotlin
   OutlinedTextField(
       value = value,
       onValueChange = onValueChange,
       label = { Text("Label") },
       leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
       trailingIcon = { /* optional */ },
       colors = OutlinedTextFieldDefaults.colors(), // Use defaults
       modifier = Modifier.fillMaxWidth()
   )
   ```

**Common M3 Migration Pitfalls:**
- ❌ `MenuAnchorType.PrimaryNotEditable` → ✅ Remove parameter entirely
- ❌ `MaterialTheme.colors` (M2) → ✅ `MaterialTheme.colorScheme` (M3)
- ❌ `backgroundColor` → ✅ `containerColor`
- ❌ `contentColor` → ✅ `contentColor` (unchanged)
- ❌ `Icons.Filled.*` for extended icons → ✅ `Icons.Default.*`

### Hilt Dependency Injection

1. **ViewModel injection**:
   ```kotlin
   @HiltViewModel
   class DashboardViewModel @Inject constructor(
       private val getDashboardKpiUseCase: GetDashboardKpiUseCase
   ) : ViewModel()
   ```

2. **Repository injection**:
   ```kotlin
   class DashboardRepositoryImpl @Inject constructor(
       private val api: DashboardApiService,
       private val dao: DashboardDao,
       private val mapper: DashboardMapper
   ) : DashboardRepository
   ```

3. **Binding interfaces**:
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   abstract class RepositoryModule {
       @Binds @Singleton
       abstract fun bindDashboardRepo(impl: DashboardRepositoryImpl): DashboardRepository
   }
   ```

### Error Handling

1. **Repository layer**: Wrap API calls in try-catch, emit Resource.Error
2. **ViewModel layer**: Handle Resource states, update UI state, emit events
3. **UI layer**: Display error UI based on state, show retry button
4. **Never crash on error**: Always provide fallback (cached data, empty state, retry)

---

## Testing Requirements

### Test Pyramid (60% unit, 25% integration, 10% UI, 5% E2E)

#### Unit Tests (ViewModel, UseCase, Repository, Mapper)

**ViewModel test pattern:**
```kotlin
@Test
fun `loadDashboard success updates uiState with data`() = runTest {
    val mockKpi = DashboardKpi(...)
    coEvery { useCase(false) } returns flowOf(Resource.Success(mockKpi))

    viewModel.loadDashboard()

    viewModel.uiState.test {
        val state = awaitItem()
        assertThat(state.isLoading).isFalse()
        assertThat(state.data).isEqualTo(mockKpi)
    }
}
```

**UseCase test pattern:**
```kotlin
@Test
fun `invoke calls repository with correct franchiseId`() = runTest {
    val franchiseId = 1L
    coEvery { repository.getDashboardKpis(franchiseId, false) } returns flowOf(...)

    useCase(false).collect()

    coVerify { repository.getDashboardKpis(franchiseId, false) }
}
```

**Repository test pattern:**
```kotlin
@Test
fun `getDashboardKpis returns fresh cache when available`() = runTest {
    val entity = DashboardStatsEntity(...)
    coEvery { dao.getFreshStats(1L, any()) } returns entity

    repository.getDashboardKpis(1L, false).test {
        val result = awaitItem()
        assertThat(result).isInstanceOf<Resource.Success>()
        cancelAndIgnoreRemainingEvents()
    }

    coVerify(exactly = 0) { apiService.getDashboardStats() }
}
```

#### Integration Tests (API + Room)

Use MockWebServer for API tests:
```kotlin
@Test
fun `dashboard API returns valid response`() = runTest {
    mockWebServer.enqueue(MockResponse()
        .setResponseCode(200)
        .setBody("""{"success": true, "data": {...}}"""))

    val response = apiService.getDashboardStats()

    assertThat(response.success).isTrue()
    assertThat(response.data).isNotNull()
}
```

#### UI Tests (Compose)

```kotlin
@Test
fun dashboardScreen_displaysKpiCards() {
    composeTestRule.setContent {
        DashboardScreen(
            uiState = DashboardUiState(data = listOf(...)),
            onRefresh = {},
            onNavigateToReport = {}
        )
    }

    composeTestRule.onNodeWithText("Sales MTD").assertIsDisplayed()
    composeTestRule.onNodeWithText("UGX 12,500,000").assertIsDisplayed()
}
```

### Coverage Targets
- ViewModel: 90%+
- UseCase: 95%+
- Repository: 85%+
- Mapper: 90%+
- UI: 60%+

Run coverage: `./gradlew testDebugUnitTest jacocoTestReport`

---

## Common Tasks and Workflows

### Adding a New Report

1. **Define domain model** (`domain/model/`):
   ```kotlin
   data class SalesReport(
       val period: String,
       val totalSales: Double,
       val items: List<SalesItem>
   )
   ```

2. **Create DTO** (`data/dto/report/`):
   ```kotlin
   @Serializable
   data class SalesReportDto(
       val period: String,
       val total_sales: Double,
       val items: List<SalesItemDto>
   )
   ```

3. **Create entity if cacheable** (`data/db/entity/`):
   ```kotlin
   @Entity(tableName = "sales_report_cache")
   data class SalesReportEntity(...)
   ```

4. **Add API endpoint** (`data/api/ReportApiService.kt`):
   ```kotlin
   @GET("api/owners/reports/sales-summary.php")
   suspend fun getSalesReport(
       @Query("start_date") startDate: String,
       @Query("end_date") endDate: String
   ): ApiResponse<SalesReportDto>
   ```

5. **Create mapper** (`data/mapper/SalesReportMapper.kt`)

6. **Add to repository** (`data/repository/ReportRepositoryImpl.kt`)

7. **Create UseCase** (`domain/usecase/report/GetSalesReportUseCase.kt`)

8. **Create ViewModel** (`presentation/reports/SalesReportViewModel.kt`)

9. **Create UI** (`presentation/reports/SalesReportScreen.kt`)

10. **Add tests** (unit, integration, UI)

### Implementing an Approval Workflow

1. Define approval domain model with status enum
2. Create approval DTO and entity
3. Add API endpoints (list, detail, approve, reject)
4. Implement offline queueing in `sync_queue` table for approve/reject
5. Create ApprovalSyncWorker for background sync
6. Build approval list and detail screens
7. Add approval badge count to BottomNavBar
8. Test approval flow end-to-end including offline scenarios

### Adding a New Feature Module

1. Create package structure in all three layers
2. Define domain models and repository interface
3. Implement data layer (API, cache, repository)
4. Create UseCases
5. Build ViewModel with state management
6. Design Compose UI with state hoisting
7. Add navigation route and integrate with NavGraph
8. Write comprehensive tests (unit, integration, UI)
9. Update CLAUDE.md with new patterns if needed

### Switching Franchises

Critical: Franchise switching invalidates all cached data except franchise list.

```kotlin
suspend fun onFranchiseSwitch(newFranchiseId: Long) {
    // Clear franchise-specific cache
    dashboardDao.deleteByFranchise(newFranchiseId)
    reportCacheDao.clearFranchiseCache(newFranchiseId)
    approvalDao.clearFranchiseApprovals(newFranchiseId)

    // Preserve franchise list (shared across franchises)
    // Trigger refresh in all ViewModels
}
```

**Security requirement:** NEVER show data from a different franchise. All queries MUST be scoped by franchise_id.

---

## File Structure Conventions

### Naming Patterns

- **Screens**: `DashboardScreen.kt`, `ReportDetailScreen.kt`
- **ViewModels**: `DashboardViewModel.kt`, `ReportViewModel.kt`
- **UseCases**: `GetDashboardKpiUseCase.kt`, `ApproveItemUseCase.kt`
- **Repositories**: `DashboardRepository.kt` (interface), `DashboardRepositoryImpl.kt` (impl)
- **DTOs**: `DashboardStatsDto.kt`, `SalesReportDto.kt`
- **Entities**: `DashboardStatsEntity.kt`, `SalesReportEntity.kt`
- **DAOs**: `DashboardDao.kt`, `ReportCacheDao.kt`
- **Mappers**: `DashboardMapper.kt`, `ReportMapper.kt`

### File Organization

Group by feature, not by layer type:

```
presentation/
├── dashboard/
│   ├── DashboardScreen.kt
│   ├── DashboardViewModel.kt
│   └── components/
│       ├── KpiCard.kt
│       └── QuickActions.kt
├── reports/
│   ├── ReportListScreen.kt
│   ├── ReportDetailScreen.kt
│   ├── ReportViewModel.kt
│   └── components/
│       ├── DateRangeSelector.kt
│       ├── ReportTable.kt
│       └── SummaryCard.kt
```

---

## Anti-Patterns to Avoid

1. **Don't mix layers**: Never import `data.dto.*` or `data.db.*` in `presentation.*`
2. **Don't use LiveData**: Use StateFlow and SharedFlow instead
3. **Don't use GlobalScope**: Always use structured concurrency (viewModelScope, etc.)
4. **Don't hardcode strings**: Use `strings.xml` for all user-facing text
5. **Don't log sensitive data**: No tokens, passwords, or PII in logs
6. **Don't bypass cache**: Respect TTL and stale-while-revalidate pattern
7. **Don't skip franchise validation**: Every data access MUST check franchise ownership
8. **Don't create mutable domain models**: Domain models should be immutable data classes
9. **Don't use `!!` operator**: Use safe calls (`?.`) or `?:` with fallback
10. **Don't test implementation details**: Test behavior, not private methods
11. **DON'T use `10.0.2.2` or `localhost` for WAMP development**:
    - ❌ WRONG: `http://10.0.2.2/` or `http://localhost/`
    - ✅ CORRECT: Use LAN IP + HostHeaderInterceptor pattern (see Build Variants section)
    - Emulators need real network access to WAMP virtual hosts

---

## AI Development Skills Reference

When working on this project, leverage these Claude Code skills:

### Available Skills
- `/android-development` - General Android development patterns and best practices
- `/android-tdd` - Test-driven development for Android with JUnit, MockK, Compose UI Test
- `/jetpack-compose-ui` - Jetpack Compose UI patterns, Material 3, state management
- `/android-data-persistence` - Room database, DataStore, caching strategies

### When to Use Each Skill

**Use `/android-development`** when:
- Setting up new features or modules
- Implementing MVVM or Clean Architecture patterns
- Working with Hilt dependency injection
- Configuring Gradle build scripts
- Handling Android lifecycle issues

**Use `/android-tdd`** when:
- Writing ViewModel tests with Flow and StateFlow
- Creating repository tests with MockK
- Testing Compose UI components
- Setting up integration tests with MockWebServer
- Implementing test doubles for dependencies

**Use `/jetpack-compose-ui`** when:
- Building new screens or UI components
- Implementing Material 3 design patterns
- Managing state in Compose
- Creating navigation flows
- Optimizing recomposition performance

**Use `/android-data-persistence`** when:
- Designing Room database schemas
- Implementing DAOs and queries
- Creating database migrations
- Setting up caching strategies
- Working with DataStore preferences

---

## Key Documentation References

### Comprehensive Planning Docs
- `docs/android-app-owner/01_PRD.md` - Product Requirements Document
- `docs/android-app-owner/02_SRS.md` - Software Requirements Specification
- `docs/android-app-owner/03_SDS.md` - Software Design Specification
- `docs/android-app-owner/04_API_CONTRACT.md` - Complete API contract
- `docs/android-app-owner/05_USER_JOURNEYS.md` - User journey flows
- `docs/android-app-owner/06_TESTING_STRATEGY.md` - Testing approach and patterns
- `docs/android-app-owner/07_RELEASE_PLAN.md` - Release planning and phases

### Quick Reference Docs
- `docs/API.md` - API integration guide for developers
- `docs/DATABASE.md` - Room database schema and patterns
- `ARCHITECTURE.md` - Architecture overview (this document)
- `TECH_STACK.md` - Complete technology stack details
- `PROJECT_BRIEF.md` - Project overview and goals

### Detailed Design Docs
- `docs/android-app-owner/sds/01-architecture.md` - Architecture deep dive
- `docs/android-app-owner/sds/03-hilt-modules.md` - Dependency injection setup
- `docs/android-app-owner/sds/04-offline-sync.md` - Caching and offline strategies
- `docs/android-app-owner/sds/05-security.md` - Security implementation
- `docs/android-app-owner/sds/06-networking.md` - Network layer details
- `docs/android-app-owner/phase-1/07-room-database.md` - Room database implementation

### API Contract Details
- `docs/android-app-owner/api-contract/01-overview.md` - API conventions
- `docs/android-app-owner/api-contract/02-endpoints-auth.md` - Authentication endpoints
- `docs/android-app-owner/api-contract/03-endpoints-dashboard-kpi.md` - Dashboard/KPI endpoints
- `docs/android-app-owner/api-contract/04-endpoints-reports.md` - Report endpoints
- `docs/android-app-owner/api-contract/05-endpoints-approvals.md` - Approval endpoints
- `docs/android-app-owner/api-contract/06-error-codes.md` - Error handling reference

---

## Development Workflow

### Starting a New Feature

1. Read relevant planning docs (PRD, SRS, API contract)
2. Design domain models and repository interface
3. Write failing tests (TDD approach)
4. Implement data layer (API, cache, repository)
5. Create UseCases with business logic
6. Build ViewModel with state management
7. Design and implement Compose UI
8. Write integration and UI tests
9. Test on emulator/device
10. Update documentation

### Before Committing

1. Run tests: `./gradlew testDebugUnitTest`
2. Check lint: `./gradlew lintDebug`
3. Verify build: `./gradlew assembleDebug`
4. Ensure coverage meets targets
5. Update relevant documentation if needed

### Code Review Checklist

- [ ] Follows Clean Architecture layer separation
- [ ] Uses Hilt for dependency injection
- [ ] Implements proper error handling
- [ ] Includes comprehensive tests (unit + integration)
- [ ] Uses StateFlow/SharedFlow (not LiveData)
- [ ] Respects cache TTL and stale-while-revalidate
- [ ] Validates franchise ownership on data access
- [ ] No hardcoded strings (uses strings.xml)
- [ ] No sensitive data in logs
- [ ] Proper Compose state hoisting
- [ ] Material 3 components used
- [ ] Preview annotations on composables

---

## Quick Commands

```bash
# Run tests
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest

# Build
./gradlew assembleDebug
./gradlew assembleStaging
./gradlew assembleRelease

# Lint and analysis
./gradlew lintDebug
./gradlew detekt

# Coverage
./gradlew testDebugUnitTest jacocoTestReport

# Clean
./gradlew clean

# Install on device
./gradlew installDebug
```

---

## Notes for Claude Code

When working on this project:

1. **Always check planning docs first** - Comprehensive specs in `docs/android-app-owner/`
2. **Follow the three-layer architecture strictly** - No shortcuts
3. **Write tests alongside implementation** - Not after
4. **Use the stale-while-revalidate pattern** - For all read operations
5. **Validate franchise ownership** - Security is critical
6. **Keep domain layer pure Kotlin** - No Android dependencies
7. **Leverage existing patterns** - Don't reinvent the wheel
8. **Document new patterns** - Update CLAUDE.md when adding new approaches
9. **Reference the skills** - Use `/android-development`, `/android-tdd`, etc. when appropriate
10. **Keep it concise** - This is a hub file, not comprehensive docs

---

**Last Updated:** 2026-02-09
**Maintainer:** Development Team
**Status:** Living Document (update as patterns evolve)
