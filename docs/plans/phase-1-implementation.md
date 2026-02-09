# Phase 1 Implementation Plan: Login + Dashboard + Empty Tabs

**Status**: Ready to Execute
**Parent Plan**: [Owner App Planning Docs](../android-app-owner/phase-1/README.md)
**Created**: 2026-02-09
**Estimated Duration**: 4 weeks

---

## Overview

Phase 1 establishes the complete vertical slice of the Dynapharm Owner Hub Android app:
- **Authentication**: JWT login, token refresh, logout
- **Dashboard**: 5 KPI cards with offline-first caching
- **Navigation**: 5-tab bottom bar (Dashboard active, 4 placeholder tabs)
- **Infrastructure**: Hilt DI, Retrofit, Room, Material 3 theme
- **Testing**: 47+ unit tests across all layers

**Critical Success Factor**: This phase MUST be fully implemented, tested, and verified before any Phase 2 business features begin.

---

## Implementation Sections

Execute in the following order (11 sections):

### Section 0: Build Variants & Configuration
**Reference**: [docs/android-app-owner/phase-1/00-build-variants.md](../android-app-owner/phase-1/00-build-variants.md)

- [ ] Configure 3 build flavors (development, staging, production)
- [ ] Set API base URLs per environment:
  - Dev: `http://dynapharm.peter/`
  - Staging: `https://erp.dynapharmafrica.com/`
  - Production: `https://coulderp.dynapharmafrica.com/`
- [ ] Configure signing configs (debug auto-signed, release requires keystore)
- [ ] Setup environment-specific app names and icons
- [ ] Configure ProGuard rules for release builds

**Files**:
- `app/build.gradle.kts` (flavor configuration)
- `gradle.properties` (signing config properties)
- `proguard-rules.pro` (obfuscation rules)

**Verification**: Build all 6 variants successfully (dev/staging/prod × debug/release)

---

### Section 1: Project Bootstrap
**Reference**: [docs/android-app-owner/phase-1/01-project-bootstrap.md](../android-app-owner/phase-1/01-project-bootstrap.md)

- [ ] Setup Gradle version catalog (`gradle/libs.versions.toml`)
- [ ] Configure root `build.gradle.kts`
- [ ] Configure app `build.gradle.kts` with all dependencies
- [ ] Setup Android manifest with permissions and app metadata
- [ ] Create base package structure:
  ```
  com.dynapharm.ownerhub/
  ├── di/
  ├── data/
  │   ├── remote/
  │   ├── local/
  │   └── repository/
  ├── domain/
  │   ├── model/
  │   ├── repository/
  │   └── usecase/
  ├── presentation/
  │   ├── navigation/
  │   ├── theme/
  │   ├── common/
  │   └── screens/
  └── util/
  ```
- [ ] Setup Timber logging in Application class
- [ ] Create `OwnerHubApplication` with Hilt setup

**Files**:
- `gradle/libs.versions.toml`
- `build.gradle.kts` (root and app)
- `settings.gradle.kts`
- `AndroidManifest.xml`
- `OwnerHubApplication.kt`

**Verification**: Project syncs without errors, app builds and runs showing blank screen

---

### Section 2: Backend API Setup
**Reference**: [docs/android-app-owner/phase-1/02-backend-api.md](../android-app-owner/phase-1/02-backend-api.md)

**Backend Developer Tasks**:
- [ ] Create `api/auth/owner-mobile-login.php` (JWT login for owners)
- [ ] Create `api/auth/mobile-refresh.php` (refresh access token)
- [ ] Create `api/auth/mobile-logout.php` (invalidate refresh token)
- [ ] Update `api/owners/dashboard-stats.php` to support JWT auth (dual auth: session + JWT)
- [ ] Create `refresh_tokens` table in database
- [ ] Configure JWT secrets in `.env` (shared with distributor app)
- [ ] Update `jwt_auth.php` middleware to support owner role

**API Endpoints**:
1. `POST /api/auth/owner-mobile-login.php` → `{ access_token, refresh_token, user: {...}, franchises: [...] }`
2. `POST /api/auth/mobile-refresh.php` → `{ access_token }`
3. `DELETE /api/auth/mobile-logout.php` → `{ success: true }`
4. `GET /api/owners/dashboard-stats.php` → `{ kpi_data: {...} }`

**Verification**:
```bash
# Test login
curl -X POST http://dynapharm.peter/api/auth/owner-mobile-login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"owner1","password":"test123"}'

# Test dashboard (requires access token from login)
curl http://dynapharm.peter/api/owners/dashboard-stats.php \
  -H "Authorization: Bearer {ACCESS_TOKEN}" \
  -H "X-Franchise-ID: 1"
```

---

### Section 3: Core Infrastructure
**Reference**: [docs/android-app-owner/phase-1/03-core-infrastructure.md](../android-app-owner/phase-1/03-core-infrastructure.md)

- [ ] Create Hilt modules:
  - `NetworkModule` (Retrofit, OkHttp, interceptors)
  - `DatabaseModule` (Room database)
  - `RepositoryModule` (repository bindings)
  - `UtilityModule` (JSON, dispatchers, network monitor)
- [ ] Create `Result<T>` sealed class for API responses
- [ ] Create `ApiResponse<T>` data class for standard envelope
- [ ] Create OkHttp interceptors:
  - `AuthInterceptor` (add JWT token header)
  - `FranchiseContextInterceptor` (add X-Franchise-ID header)
  - `TokenRefreshAuthenticator` (auto-refresh on 401)
  - `LoggingInterceptor` (debug logging)
- [ ] Create `NetworkMonitor` to detect online/offline state
- [ ] Setup Kotlin serialization with custom JSON config

**Files**:
- `di/NetworkModule.kt`
- `di/DatabaseModule.kt`
- `di/RepositoryModule.kt`
- `di/UtilityModule.kt`
- `data/remote/interceptor/AuthInterceptor.kt`
- `data/remote/interceptor/FranchiseContextInterceptor.kt`
- `data/remote/interceptor/TokenRefreshAuthenticator.kt`
- `data/remote/dto/ApiResponse.kt`
- `domain/model/Result.kt`
- `util/NetworkMonitor.kt`

**Verification**: Hilt graph builds successfully, interceptors are registered in correct order

---

### Section 4: Authentication Feature (Full Vertical Slice)
**Reference**: [docs/android-app-owner/phase-1/04-authentication-feature.md](../android-app-owner/phase-1/04-authentication-feature.md)

**Data Layer**:
- [ ] Create `TokenManager` (store/retrieve/clear JWT tokens)
- [ ] Create `AuthApiService` (Retrofit interface for auth endpoints)
- [ ] Create `LoginRequestDto`, `LoginResponseDto`, `TokenResponseDto`
- [ ] Create `AuthRepositoryImpl` implementing `AuthRepository`

**Domain Layer**:
- [ ] Create `User` domain model
- [ ] Create `AuthRepository` interface
- [ ] Create use cases:
  - `LoginUseCase`
  - `RefreshTokenUseCase`
  - `LogoutUseCase`
  - `GetCurrentUserUseCase`

**Presentation Layer**:
- [ ] Create `LoginUiState` sealed class
- [ ] Create `LoginViewModel` with state management
- [ ] Create `LoginScreen` composable (email, password, remember me, login button)
- [ ] Add logo from `dist/img/icons/DynaLogo.png`
- [ ] Add language selector dropdown
- [ ] Handle loading, success, error states

**Files**:
- `data/local/prefs/TokenManager.kt`
- `data/remote/api/AuthApiService.kt`
- `data/remote/dto/AuthDtos.kt`
- `data/repository/AuthRepositoryImpl.kt`
- `domain/model/User.kt`
- `domain/repository/AuthRepository.kt`
- `domain/usecase/auth/*.kt` (4 use cases)
- `presentation/screens/auth/LoginUiState.kt`
- `presentation/screens/auth/LoginViewModel.kt`
- `presentation/screens/auth/LoginScreen.kt`

**Verification**:
- User can log in with valid credentials
- Access token and refresh token stored securely
- Invalid credentials show error message
- Loading state displays correctly

---

### Section 5: Dashboard Feature (Offline-First)
**Reference**: [docs/android-app-owner/phase-1/05-dashboard-feature.md](../android-app-owner/phase-1/05-dashboard-feature.md)

**Data Layer**:
- [ ] Create `DashboardApiService` (Retrofit interface)
- [ ] Create `DashboardStatsDto` and related DTOs
- [ ] Create `DashboardStatsEntity` (Room entity for caching)
- [ ] Create `DashboardDao` (Room DAO)
- [ ] Create `DashboardRepositoryImpl` with stale-while-revalidate strategy
- [ ] Implement caching logic: always show cached first, then refresh

**Domain Layer**:
- [ ] Create `DashboardStats` domain model (5 KPIs)
- [ ] Create `DashboardRepository` interface
- [ ] Create `GetDashboardStatsUseCase`
- [ ] Create `RefreshDashboardUseCase`

**Presentation Layer**:
- [ ] Create `DashboardUiState` data class
- [ ] Create `DashboardViewModel` with StateFlow
- [ ] Create `DashboardScreen` composable
- [ ] Create `KpiCard` reusable component (icon, label, value, trend indicator)
- [ ] Implement pull-to-refresh
- [ ] Show stale indicator when offline

**5 KPI Cards**:
1. Sales MTD (Month-to-Date)
2. Cash Balance
3. Inventory Value
4. Total Business Volume
5. Pending Approvals (count with badge)

**Files**:
- `data/remote/api/DashboardApiService.kt`
- `data/remote/dto/DashboardDtos.kt`
- `data/local/db/entity/DashboardStatsEntity.kt`
- `data/local/db/dao/DashboardDao.kt`
- `data/repository/DashboardRepositoryImpl.kt`
- `domain/model/DashboardStats.kt`
- `domain/repository/DashboardRepository.kt`
- `domain/usecase/dashboard/*.kt` (2 use cases)
- `presentation/screens/dashboard/DashboardUiState.kt`
- `presentation/screens/dashboard/DashboardViewModel.kt`
- `presentation/screens/dashboard/DashboardScreen.kt`
- `presentation/common/KpiCard.kt`

**Verification**:
- Dashboard loads from cache immediately when available
- Fresh data fetched in background
- Stale indicator shows when data is old
- Pull-to-refresh works
- Dashboard works offline (shows cached data)

---

### Section 6: Navigation & Tab Structure
**Reference**: [docs/android-app-owner/phase-1/06-navigation-tabs.md](../android-app-owner/phase-1/06-navigation-tabs.md)

- [ ] Create `Screen` sealed class for all routes
- [ ] Create `NavGraph.kt` with navigation host
- [ ] Create `BottomNavigationBar` with 5 tabs:
  1. **Dashboard** (home icon) → `DashboardScreen`
  2. **Reports** (bar_chart icon) → `PlaceholderScreen("Reports")`
  3. **Approvals** (check_circle icon) → `PlaceholderScreen("Approvals")`
  4. **Franchises** (store icon) → `PlaceholderScreen("Franchises")`
  5. **More** (more_horiz icon) → `MoreScreen` (Profile, Settings, Logout)
- [ ] Create `PlaceholderScreen` composable ("Coming Soon" message)
- [ ] Create `MoreScreen` with navigation list
- [ ] Setup authentication flow (login → dashboard, logout → login)

**Files**:
- `presentation/navigation/Screen.kt`
- `presentation/navigation/NavGraph.kt`
- `presentation/navigation/BottomNavigationBar.kt`
- `presentation/screens/placeholder/PlaceholderScreen.kt`
- `presentation/screens/more/MoreScreen.kt`

**Verification**:
- Bottom navigation shows all 5 tabs
- Dashboard tab is functional
- Other 4 tabs show "Coming Soon"
- More tab shows profile/settings/logout options
- Navigation state persists on configuration change

---

### Section 7: Room Database Setup
**Reference**: [docs/android-app-owner/phase-1/07-room-database.md](../android-app-owner/phase-1/07-room-database.md)

- [ ] Create `AppDatabase` abstract class with version 1
- [ ] Create entities:
  - `DashboardStatsEntity` (id, franchise_id, data JSON, timestamp)
- [ ] Create DAOs:
  - `DashboardDao` (insert, getByFranchise, deleteOld)
- [ ] Create type converters for JSON and Date
- [ ] Setup auto-migration strategy for future versions
- [ ] Configure database in `DatabaseModule`

**Files**:
- `data/local/db/AppDatabase.kt`
- `data/local/db/entity/DashboardStatsEntity.kt`
- `data/local/db/dao/DashboardDao.kt`
- `data/local/db/converters/Converters.kt`

**Verification**:
- Database creates successfully on first launch
- Dashboard data caches to Room
- Cached data retrieved correctly
- Old cache entries auto-deleted after TTL

---

### Section 8: Theme & UI Components
**Reference**: [docs/android-app-owner/phase-1/08-theme-ui-components.md](../android-app-owner/phase-1/08-theme-ui-components.md)

- [ ] Create Material 3 color scheme (Dynapharm green primary)
- [ ] Create `Theme.kt` with light and dark themes
- [ ] Create typography scale
- [ ] Create shape definitions
- [ ] Create reusable composables:
  - `KpiCard` (icon, label, value, trend, loading state)
  - `LoadingIndicator` (centered circular progress)
  - `ErrorState` (icon, message, retry button)
  - `EmptyState` (icon, message)
  - `StaleDataBanner` (shows when data is outdated)

**Colors**:
- Primary: Dynapharm Green (#2E7D32 or brand color)
- Secondary: Complementary accent
- Error: Material red
- Background: White (light), Dark grey (dark)

**Files**:
- `presentation/theme/Color.kt`
- `presentation/theme/Theme.kt`
- `presentation/theme/Type.kt`
- `presentation/theme/Shape.kt`
- `presentation/common/KpiCard.kt`
- `presentation/common/LoadingIndicator.kt`
- `presentation/common/ErrorState.kt`
- `presentation/common/EmptyState.kt`
- `presentation/common/StaleDataBanner.kt`

**Verification**:
- App displays correct brand colors
- Dark theme works correctly
- All reusable components render properly

---

### Section 9: Testing (47+ Unit Tests)
**Reference**: [docs/android-app-owner/phase-1/09-testing.md](../android-app-owner/phase-1/09-testing.md)

**Test Categories**:

1. **ViewModel Tests** (15 tests)
   - `LoginViewModelTest` (6 tests: successful login, invalid credentials, network error, empty fields, remember me, logout)
   - `DashboardViewModelTest` (9 tests: load success, load error, refresh, stale data, franchise switch, cache hit, cache miss, offline mode, loading state)

2. **UseCase Tests** (10 tests)
   - `LoginUseCaseTest` (3 tests: success, failure, validation)
   - `RefreshTokenUseCaseTest` (2 tests: success, failure)
   - `GetDashboardStatsUseCaseTest` (3 tests: cache hit, cache miss, stale data)
   - `RefreshDashboardUseCaseTest` (2 tests: success, failure)

3. **Repository Tests** (12 tests)
   - `AuthRepositoryImplTest` (4 tests: login, logout, refresh, token storage)
   - `DashboardRepositoryImplTest` (8 tests: fetch fresh, fetch cached, stale-while-revalidate, cache invalidation, franchise context, TTL expiry, offline, error handling)

4. **DAO Tests** (5 tests)
   - `DashboardDaoTest` (5 tests: insert, query by franchise, update, delete old, empty result)

5. **Interceptor Tests** (5 tests)
   - `AuthInterceptorTest` (2 tests: adds token, handles missing token)
   - `FranchiseContextInterceptorTest` (1 test: adds franchise header)
   - `TokenRefreshAuthenticatorTest` (2 tests: refreshes on 401, fails on invalid refresh token)

**Setup**:
- [ ] Configure JUnit 5
- [ ] Add MockK for mocking
- [ ] Add Turbine for Flow testing
- [ ] Add Truth for assertions
- [ ] Configure test coroutine dispatcher

**Files**:
- `test/viewmodel/LoginViewModelTest.kt`
- `test/viewmodel/DashboardViewModelTest.kt`
- `test/usecase/auth/*.kt`
- `test/usecase/dashboard/*.kt`
- `test/repository/AuthRepositoryImplTest.kt`
- `test/repository/DashboardRepositoryImplTest.kt`
- `test/dao/DashboardDaoTest.kt`
- `test/interceptor/*.kt`

**Verification**: All 47+ tests pass with `./gradlew testDebugUnitTest`

---

### Section 10: Verification & Quality Gates
**Reference**: [docs/android-app-owner/phase-1/10-verification.md](../android-app-owner/phase-1/10-verification.md)

**Backend Verification**:
```bash
# 1. Test login
curl -X POST http://dynapharm.peter/api/auth/owner-mobile-login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"owner1","password":"test123"}'

# 2. Test refresh token
curl -X POST http://dynapharm.peter/api/auth/mobile-refresh.php \
  -H "Content-Type: application/json" \
  -d '{"refresh_token":"YOUR_REFRESH_TOKEN"}'

# 3. Test dashboard with JWT
curl http://dynapharm.peter/api/owners/dashboard-stats.php \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "X-Franchise-ID: 1"

# 4. Test logout
curl -X DELETE http://dynapharm.peter/api/auth/mobile-logout.php \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"refresh_token":"YOUR_REFRESH_TOKEN"}'
```

**Android Build Verification**:
- [ ] `./gradlew clean assemblyDevelopmentDebug` succeeds
- [ ] `./gradlew clean assemblyStagingDebug` succeeds
- [ ] `./gradlew clean assembleProductionRelease` succeeds (requires keystore)
- [ ] `./gradlew lintDebug` passes with zero errors
- [ ] `./gradlew testDebugUnitTest` passes all 47+ tests

**Manual Testing Checklist**:
- [ ] User can log in with valid owner credentials
- [ ] Invalid credentials show error message
- [ ] Dashboard displays 5 KPI cards
- [ ] Dashboard loads from cache when available
- [ ] Pull-to-refresh updates dashboard data
- [ ] Stale data banner shows when offline
- [ ] All 5 tabs render (4 show "Coming Soon")
- [ ] Logout clears tokens and returns to login
- [ ] App works offline (cached dashboard visible)
- [ ] App icon and splash screen display correctly
- [ ] Dynapharm logo displays on login screen

---

## Phase 1 → Phase 2 Gate Criteria

**CRITICAL**: Phase 2 MUST NOT begin until ALL criteria are met:

- [x] Backend: All 4 auth + dashboard API endpoints working
- [ ] Android: Owner can log in via JWT and see dashboard KPIs
- [ ] Android: Token refresh works silently (no re-login for 30 days)
- [ ] Android: Dashboard loads from Room cache when offline
- [ ] Android: All 5 bottom tabs render (4 show "Coming Soon")
- [ ] Android: 47+ unit tests pass in local build
- [ ] Android: Lint passes with zero errors
- [ ] Android: APK builds successfully for all 3 flavors
- [ ] Documentation: All Phase 1 sections completed and verified

---

## File Checklist

By the end of Phase 1, the following files MUST exist:

**Configuration** (5 files):
- `gradle/libs.versions.toml`
- `build.gradle.kts` (root)
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `AndroidManifest.xml`

**DI Modules** (4 files):
- `di/NetworkModule.kt`
- `di/DatabaseModule.kt`
- `di/RepositoryModule.kt`
- `di/UtilityModule.kt`

**Data Layer - Remote** (12 files):
- `data/remote/api/AuthApiService.kt`
- `data/remote/api/DashboardApiService.kt`
- `data/remote/dto/AuthDtos.kt`
- `data/remote/dto/DashboardDtos.kt`
- `data/remote/dto/ApiResponse.kt`
- `data/remote/interceptor/AuthInterceptor.kt`
- `data/remote/interceptor/FranchiseContextInterceptor.kt`
- `data/remote/interceptor/TokenRefreshAuthenticator.kt`

**Data Layer - Local** (8 files):
- `data/local/prefs/TokenManager.kt`
- `data/local/db/AppDatabase.kt`
- `data/local/db/entity/DashboardStatsEntity.kt`
- `data/local/db/dao/DashboardDao.kt`
- `data/local/db/converters/Converters.kt`

**Data Layer - Repository** (2 files):
- `data/repository/AuthRepositoryImpl.kt`
- `data/repository/DashboardRepositoryImpl.kt`

**Domain Layer** (12 files):
- `domain/model/User.kt`
- `domain/model/DashboardStats.kt`
- `domain/model/Result.kt`
- `domain/repository/AuthRepository.kt`
- `domain/repository/DashboardRepository.kt`
- `domain/usecase/auth/LoginUseCase.kt`
- `domain/usecase/auth/RefreshTokenUseCase.kt`
- `domain/usecase/auth/LogoutUseCase.kt`
- `domain/usecase/auth/GetCurrentUserUseCase.kt`
- `domain/usecase/dashboard/GetDashboardStatsUseCase.kt`
- `domain/usecase/dashboard/RefreshDashboardUseCase.kt`

**Presentation Layer - Theme** (4 files):
- `presentation/theme/Color.kt`
- `presentation/theme/Theme.kt`
- `presentation/theme/Type.kt`
- `presentation/theme/Shape.kt`

**Presentation Layer - Common Components** (5 files):
- `presentation/common/KpiCard.kt`
- `presentation/common/LoadingIndicator.kt`
- `presentation/common/ErrorState.kt`
- `presentation/common/EmptyState.kt`
- `presentation/common/StaleDataBanner.kt`

**Presentation Layer - Navigation** (3 files):
- `presentation/navigation/Screen.kt`
- `presentation/navigation/NavGraph.kt`
- `presentation/navigation/BottomNavigationBar.kt`

**Presentation Layer - Screens** (10 files):
- `presentation/screens/auth/LoginUiState.kt`
- `presentation/screens/auth/LoginViewModel.kt`
- `presentation/screens/auth/LoginScreen.kt`
- `presentation/screens/dashboard/DashboardUiState.kt`
- `presentation/screens/dashboard/DashboardViewModel.kt`
- `presentation/screens/dashboard/DashboardScreen.kt`
- `presentation/screens/placeholder/PlaceholderScreen.kt`
- `presentation/screens/more/MoreScreen.kt`

**Utilities** (2 files):
- `util/NetworkMonitor.kt`
- `OwnerHubApplication.kt`

**Tests** (15+ test files):
- ViewModel tests (2 files)
- UseCase tests (4 files)
- Repository tests (2 files)
- DAO tests (1 file)
- Interceptor tests (3 files)

**Total**: ~90 production files + 15+ test files

---

## Timeline Estimate

| Week | Focus | Deliverables |
|------|-------|-------------|
| **Week 1** | Project setup, backend APIs, core infrastructure | Sections 0-3 complete |
| **Week 2** | Authentication feature, Room database | Sections 4, 7 complete |
| **Week 3** | Dashboard feature, navigation, theme | Sections 5, 6, 8 complete |
| **Week 4** | Testing, verification, polish | Section 9, 10 complete, gate criteria met |

---

## Success Criteria

Phase 1 is considered **COMPLETE** when:

1. ✅ All 11 sections implemented
2. ✅ All 47+ unit tests passing
3. ✅ All backend endpoints functional
4. ✅ All 3 build variants compile and run
5. ✅ Lint passes with zero errors
6. ✅ Manual testing checklist 100% complete
7. ✅ Gate criteria verified by independent reviewer
8. ✅ APK installable and functional on physical device

---

## Next Steps After Phase 1

Upon successful completion and gate approval:

1. **Tag Release**: `git tag v0.1.0-phase1`
2. **Review Session**: Team reviews Phase 1 learnings
3. **Phase 2 Planning**: Prioritize first business features
4. **Begin Phase 2**: [phase-2-roadmap.md](../android-app-owner/phase-2-roadmap.md)

---

## References

- **Detailed Specs**: [docs/android-app-owner/phase-1/](../android-app-owner/phase-1/)
- **Architecture**: [ARCHITECTURE.md](../../ARCHITECTURE.md)
- **Tech Stack**: [TECH_STACK.md](../../TECH_STACK.md)
- **API Contract**: [docs/android-app-owner/04_API_CONTRACT.md](../android-app-owner/04_API_CONTRACT.md)
- **Testing Strategy**: [docs/android-app-owner/06_TESTING_STRATEGY.md](../android-app-owner/06_TESTING_STRATEGY.md)

---

**Last Updated**: 2026-02-09
**Status**: Ready to Execute
