# Phase 1: Login + Dashboard + Empty Tabs

**Parent:** [Owner App Docs](../README.md) | **Status:** Not Started

> The mandatory bootstrap phase. Proves the entire vertical slice (UI -> ViewModel -> UseCase -> Repository -> API -> Backend -> Database) and establishes all infrastructure patterns that every future feature reuses.

---

## What Phase 1 Delivers

| Component | Android | Backend |
|-----------|---------|---------|
| **Auth** | LoginScreen, LoginViewModel, AuthRepository, AuthApiService, TokenManager, interceptors | owner-mobile-login.php, mobile-refresh.php, mobile-logout.php, MobileAuthHelper, jwt_auth.php middleware |
| **Dashboard** | DashboardScreen, DashboardViewModel, DashboardRepository, Room cache | dashboard-stats.php (dual auth: JWT + session) |
| **Navigation** | 5-tab BottomBar, NavGraph, PlaceholderScreen for future tabs | -- |
| **Infrastructure** | Hilt DI modules, Material 3 theme, encrypted prefs, network monitor | refresh_tokens table, .env JWT config |
| **Tests** | 47+ unit tests across all layers | curl/Postman endpoint verification |

### Bottom Navigation (5 Tabs)

| Tab | Icon | Phase 1 Content |
|-----|------|-----------------|
| **Dashboard** | `home` | Live KPI cards (offline-first) |
| **Reports** | `bar_chart` | "Coming Soon" placeholder |
| **Approvals** | `check_circle` | "Coming Soon" placeholder |
| **Franchises** | `store` | "Coming Soon" placeholder |
| **More** | `more_horiz` | Profile link, Settings link, Logout |

---

## Plan Sections (11 Documents)

| # | Section | Lines | Description |
|---|---------|-------|-------------|
| 00 | [Build Variants](00-build-variants.md) | 397 | Dev/Staging/Prod flavors, API base URLs, signing configs |
| 01 | [Project Bootstrap](01-project-bootstrap.md) | 395 | Gradle version catalog, build.gradle.kts, manifest, package structure |
| 02 | [Backend API](02-backend-api.md) | 405 | JWT endpoints (owner-mobile-login, refresh, logout), dual auth middleware |
| 03 | [Core Infrastructure](03-core-infrastructure.md) | 429 | Hilt DI modules, Retrofit, OkHttp interceptors, NetworkMonitor, Result types |
| 04 | [Authentication Feature](04-authentication-feature.md) | 488 | Full vertical slice: TokenManager -> AuthRepository -> LoginUseCase -> LoginViewModel -> LoginScreen |
| 05 | [Dashboard Feature](05-dashboard-feature.md) | 489 | Offline-first dashboard: Room cache, stale-while-revalidate, 5 KPI cards, pull-to-refresh |
| 06 | [Navigation & Tabs](06-navigation-tabs.md) | 360 | 5-tab BottomBar, NavGraph, Screen sealed class, PlaceholderScreen, MoreScreen |
| 07 | [Room Database](07-room-database.md) | 412 | AppDatabase, DashboardStatsEntity, DashboardDao, converters, migration strategy |
| 08 | [Theme & UI Components](08-theme-ui-components.md) | 494 | Material 3 theme (Dynapharm green), StatCard, LoadingState, ErrorState, reusable composables |
| 09 | [Testing](09-testing.md) | 466 | 47+ unit tests: ViewModels, UseCases, Repositories, Interceptors, Room DAOs, CI config |
| 10 | [Verification](10-verification.md) | 362 | Backend curl tests, Android build checklist, Phase 1 -> Phase 2 gate criteria |

---

## Implementation Order

```
Week 1:  00 Build Variants -> 01 Project Bootstrap -> 02 Backend API
Week 2:  03 Core Infrastructure -> 07 Room Database -> 08 Theme & UI
Week 3:  04 Authentication Feature -> 05 Dashboard Feature
Week 4:  06 Navigation & Tabs -> 09 Testing -> 10 Verification
```

## Phase 1 -> Phase 2 Gate Criteria

Phase 2 features MUST NOT begin until ALL of these are true:

- [ ] Owner can log in via JWT and see dashboard KPIs
- [ ] Token refresh works silently (no re-login for 30 days)
- [ ] Dashboard loads from Room cache when offline (stale data shown)
- [ ] All 5 bottom tabs render (4 show "Coming Soon" placeholder)
- [ ] 47+ unit tests pass in CI
- [ ] Backend curl tests pass for all 4 auth + dashboard endpoints
- [ ] APK builds successfully for all 3 flavors (dev, staging, prod)

## What Comes Next

See [Phase 2+ Roadmap](../phase-2-roadmap.md) for the business feature rollout plan:

- **Phase 2a:** Reports (6 reports across Sales, Finance, Distributors)
- **Phase 2b:** Expense Approval workflow
- **Phase 2c:** Franchise Switcher (multi-franchise context)
- **Phase 2d:** Profile Management
- **Phase 3:** Offline caching, push notifications, biometric login
- **Phase 4:** All 23 reports, all 7 approval workflows, PDF export

---

*Generated per the `android-saas-planning` Phase 1 Bootstrap Pattern.*
