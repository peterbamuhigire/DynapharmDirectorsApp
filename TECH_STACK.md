# Technology Stack - Dynapharm Owner Hub

**Product:** Dynapharm Owner Hub | **Package:** `com.dynapharm.ownerhub`
**Platform:** Android Native | **Architecture:** MVVM + Clean Architecture
**Last Updated:** 2026-02-09

---

## Table of Contents

- [Overview](#overview)
- [Core Technologies](#core-technologies)
- [Android SDK & Build Tools](#android-sdk--build-tools)
- [Programming Languages](#programming-languages)
- [UI Framework](#ui-framework)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [Dependency Injection](#dependency-injection)
- [Networking](#networking)
- [Local Data Persistence](#local-data-persistence)
- [Asynchronous Programming](#asynchronous-programming)
- [Background Processing](#background-processing)
- [Navigation](#navigation)
- [Security](#security)
- [Image Loading](#image-loading)
- [Charts & Visualization](#charts--visualization)
- [Utilities](#utilities)
- [Testing Frameworks](#testing-frameworks)
- [Development Tools](#development-tools)
- [Backend Integration](#backend-integration)
- [CI/CD Pipeline](#cicd-pipeline)
- [Monitoring & Analytics](#monitoring--analytics)
- [Technology Selection Rationale](#technology-selection-rationale)

---

## Overview

The Dynapharm Owner Hub is a **read-heavy, write-light** native Android application designed for franchise owners to monitor business performance, review reports, and process approval workflows. The technology stack is optimized for:

- **Report-centric operations** - Fast rendering of complex financial, sales, and HR reports
- **Multi-franchise context switching** - Secure data isolation between franchises
- **Offline-first caching** - Stale-while-revalidate pattern for responsive UX
- **Enterprise security** - JWT authentication, encrypted storage, certificate pinning
- **Scalable architecture** - Clean separation of concerns, testable components

---

## Core Technologies

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| Language | Kotlin | 2.0.10 | Primary development language |
| UI Framework | Jetpack Compose | BOM 2024.06.00 | Declarative UI toolkit |
| Architecture | MVVM + Clean Architecture | - | Separation of concerns, testability |
| DI Framework | Dagger Hilt | 2.51.1 | Compile-time dependency injection |
| Build System | Gradle (Kotlin DSL) | 8.5.1 (AGP) | Build configuration and automation |

---

## Android SDK & Build Tools

### SDK Versions

| Target | Version | Rationale |
|--------|---------|-----------|
| **Minimum SDK** | API 29 (Android 10) | Covers 95%+ of active devices, modern crypto APIs |
| **Target SDK** | API 34 (Android 14) | Latest stable API, Google Play requirement |
| **Compile SDK** | API 34 | Matches target SDK for consistency |

### Build Configuration

| Tool | Version | Purpose |
|------|---------|---------|
| Android Gradle Plugin (AGP) | 8.5.1 | Build automation, variant management |
| Gradle | 8.9+ | Build system |
| Kotlin Gradle Plugin | 2.0.10 | Kotlin compilation |
| KSP (Kotlin Symbol Processing) | 2.0.10-1.0.24 | Annotation processing (Hilt, Room) |

### Java Compatibility

- **Source/Target Compatibility:** Java 17
- **JVM Target:** 17

---

## Programming Languages

### Kotlin

**Version:** 2.0.10

**Key Features Used:**
- Coroutines & Flow for asynchronous programming
- Sealed classes for state management (Resource, UiState)
- Data classes for immutable models
- Extension functions for utility methods
- Kotlin Serialization for JSON parsing
- Null safety for crash prevention

**Compiler Options:**
```kotlin
freeCompilerArgs:
  - "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
  - "-opt-in=kotlinx.coroutines.FlowPreview"
  - "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
```

**Rationale:** Modern, concise, null-safe, excellent Android tooling, coroutines for async operations, 100% Java interoperability.

---

## UI Framework

### Jetpack Compose

**Version:** BOM 2024.06.00 (ties all Compose library versions)

| Library | Purpose |
|---------|---------|
| `androidx.compose.ui` | Core UI primitives |
| `androidx.compose.ui.graphics` | Graphics and drawing |
| `androidx.compose.ui.tooling` | Preview and debug tools (debug only) |
| `androidx.compose.ui.tooling.preview` | @Preview annotations |
| `androidx.compose.material3` | Material 3 design components |
| `androidx.compose.material:material-icons-extended` | Extended icon set |

**Key Features:**
- Declarative UI (UI = f(state))
- No XML layouts
- Built-in state management
- Recomposition optimization (strong skipping mode enabled)
- Material 3 theming (dynamic colors, accessibility)

**Rationale:** Modern Android standard, less boilerplate than XML, better performance through smart recomposition, easier testing with semantics tree.

---

## Architecture & Design Patterns

### MVVM (Model-View-ViewModel)

**Layers:**
```
Presentation Layer (Compose UI + ViewModels)
        ↓
Domain Layer (Use Cases + Repository Interfaces)
        ↓
Data Layer (Repository Implementations + Data Sources)
    ↙           ↘
Remote          Local
(Retrofit)      (Room Cache)
```

**Key Components:**
- **UI (Compose Screens):** Renders state, emits user events
- **ViewModel:** Manages UI state, business logic orchestration
- **Use Cases:** Single-responsibility business operations
- **Repository:** Data source abstraction (network + cache)
- **Data Sources:** Remote API (Retrofit) + Local cache (Room)

**Package Structure:**
```
com.dynapharm.ownerhub/
  di/                     # Hilt modules
  data/
    remote/               # Retrofit services, DTOs, interceptors
    local/                # Room DB, DAOs, entities, prefs
    repository/           # Repository implementations
  domain/
    model/                # Domain models
    repository/           # Repository interfaces
    usecase/              # Use cases (auth, reports, approvals, etc.)
  presentation/
    navigation/           # NavHost, routes
    theme/                # Material 3 theme
    common/               # Shared composables
    screens/              # Feature screens (auth, dashboard, reports, etc.)
  util/                   # Extensions, formatters, constants
```

**Rationale:** Testable layers, clear separation of concerns, scalable for large teams, aligns with Android best practices.

---

## Dependency Injection

### Dagger Hilt

**Version:** 2.51.1

**Hilt Extensions:**
- `androidx.hilt:hilt-navigation-compose` (1.2.0) - ViewModel injection in Compose
- `androidx.hilt:hilt-work` (1.2.0) - WorkManager integration

**Module Structure:**

| Module | Provides | Scope |
|--------|----------|-------|
| `NetworkModule` | OkHttp, Retrofit, API services | `@Singleton` |
| `DatabaseModule` | Room DB, DAOs | `@Singleton` |
| `RepositoryModule` | Repository implementations | `@Singleton` |
| `AuthModule` | Auth manager, token storage | `@Singleton` |
| `FranchiseModule` | Franchise context manager | Custom `@FranchiseScope` |

**Custom Scopes:**
- `@FranchiseScope` - Rebuilds DI graph on franchise switch (clears cached data)

**Rationale:** Compile-time DI (faster than runtime), Android lifecycle aware, official Google recommendation, excellent integration with Jetpack libraries.

---

## Networking

### HTTP Client Stack

| Component | Library | Version | Role |
|-----------|---------|---------|------|
| **REST Client** | Retrofit | 2.11.0 | Type-safe HTTP client |
| **HTTP Engine** | OkHttp | 4.12.0 | Connection pooling, interceptors |
| **Logging Interceptor** | OkHttp Logging | 4.12.0 | Request/response logging (debug only) |
| **JSON Serialization** | Kotlin Serialization | 1.7.1 | Compile-time JSON parsing |
| **Converter** | Retrofit KotlinX Serialization Converter | 1.0.0 | Retrofit ↔ Kotlin Serialization bridge |

### Retrofit Service Interfaces

- `AuthApiService` - Login, token refresh, logout
- `DashboardApiService` - Executive KPI dashboard
- `FranchiseApiService` - Franchise list, switching
- `ReportApiService` - 23+ report endpoints
- `ApprovalApiService` - Approval workflows (expense, PO, etc.)
- `ProfileApiService` - Owner profile CRUD, photo upload

### OkHttp Interceptors

1. **AuthInterceptor** - Injects JWT access token in `Authorization: Bearer <token>` header
2. **FranchiseContextInterceptor** - Injects `X-Franchise-ID` header for multi-tenancy
3. **TokenRefreshInterceptor** - Auto-refreshes expired tokens (401 → refresh → retry)
4. **HttpLoggingInterceptor** - Logs requests/responses (debug builds only)

### Timeout Configuration

| Setting | Value | Rationale |
|---------|-------|-----------|
| Connect timeout | 30s | Accommodates slow mobile networks |
| Read timeout | 30s | Large report payloads |
| Write timeout | 30s | Profile photo uploads |

### Network Monitoring

**NetworkMonitor** (singleton) - Tracks online/offline state using `ConnectivityManager` callbacks, exposes `Flow<Boolean>` for reactive UI updates.

**Rationale:** Retrofit is the industry standard for REST APIs, OkHttp provides robust interceptor chain for auth/logging, Kotlin Serialization is faster and safer than Gson/Moshi (no reflection).

---

## Local Data Persistence

### Room Database

**Version:** 2.6.1

**Components:**
- `room-runtime` - Database abstraction
- `room-ktx` - Coroutines/Flow extensions
- `room-compiler` - Annotation processor (KSP)

**Usage:**
- **Primary role:** Report cache (NOT source of truth)
- **Cache strategy:** Stale-while-revalidate (show cached data, fetch fresh in background)
- **TTL:** 10 minutes for dashboard KPIs, configurable per report type
- **Invalidation triggers:** Franchise switch, manual refresh, TTL expiry

**DAOs:**
- `DashboardKpiDao` - Cached KPI cards
- `ReportCacheDao` - Generic report caching (JSON blobs)
- `ApprovalDao` - Approval queue cache (offline viewing)

**Entities:**
- `DashboardKpiEntity` (franchise_id, data, cached_at, expires_at)
- `ReportCacheEntity` (franchise_id, report_type, filters_hash, data, cached_at, expires_at)
- `ApprovalEntity` (franchise_id, approval_id, type, data, cached_at)

**Schema Export:**
- Location: `app/schemas/`
- Purpose: Migration testing

### EncryptedSharedPreferences

**Library:** `androidx.security:security-crypto` (1.1.0-alpha06)

**Usage:**
- JWT access token (15 min expiry)
- JWT refresh token (30 day expiry)
- Selected franchise ID
- User preferences (language, biometric enabled)

**Security:** Backed by Android Keystore, AES-256-GCM encryption.

### DataStore (Preferences)

**Library:** `androidx.datastore:datastore-preferences` (1.1.1)

**Usage:**
- Non-sensitive user preferences (theme, report filters)
- Type-safe, coroutine-based, replaces SharedPreferences for new code

**Rationale:** Room provides structured caching with TTL support, EncryptedSharedPreferences secures tokens, DataStore offers modern reactive preferences API.

---

## Asynchronous Programming

### Kotlin Coroutines

**Version:** 1.8.1

**Libraries:**
- `kotlinx-coroutines-android` - Android main thread dispatcher
- `kotlinx-coroutines-test` - Testing utilities (Turbine, TestDispatcher)

**Dispatchers:**
- `Dispatchers.Main` - UI updates (ViewModels, Compose recomposition)
- `Dispatchers.IO` - Network calls, database queries
- `Dispatchers.Default` - CPU-intensive work (JSON parsing, mapping)

**Key Patterns:**
- `Flow` for reactive data streams (Room queries, network state)
- `StateFlow`/`SharedFlow` for ViewModel state management
- `suspend fun` for async operations
- `coroutineScope` for structured concurrency

**Example:**
```kotlin
viewModelScope.launch {
    dashboardRepository.getDashboardKpis(franchiseId)
        .collect { resource ->
            _uiState.value = when (resource) {
                is Resource.Success -> UiState.Success(resource.data)
                is Resource.Error -> UiState.Error(resource.message)
                is Resource.Loading -> UiState.Loading(resource.data)
            }
        }
}
```

**Rationale:** Coroutines are Kotlin-native, less error-prone than RxJava, better IDE support, simpler mental model, cancelable by default.

---

## Background Processing

### WorkManager

**Version:** 2.9.0

**Usage:**
- Background report cache refresh (daily at 3 AM)
- Periodic approval queue sync (every 30 minutes)
- Retry logic for failed approval submissions

**Work Types:**
- `PeriodicWorkRequest` - Scheduled cache refresh
- `OneTimeWorkRequest` - Immediate retry on failure

**Constraints:**
- Network required
- Battery not low (for periodic work)
- Device idle preferred (for cache refresh)

**Hilt Integration:**
- `androidx.hilt:hilt-work` (1.2.0) - Inject dependencies into Workers
- Custom `HiltWorkerFactory`

**Rationale:** WorkManager guarantees execution (even after app kill/reboot), respects system constraints (battery, network), integrates with Hilt for DI.

---

## Navigation

### Navigation Compose

**Version:** 2.7.7

**Library:** `androidx.navigation:navigation-compose`

**Routes:**
- `login` - Login screen
- `dashboard` - Dashboard (5 KPI cards)
- `franchise_switcher` - Multi-franchise selection
- `reports/{category}` - Report category list
- `report_detail/{type}` - Individual report screen
- `approvals` - Approval queue
- `approval_detail/{id}` - Approval detail
- `profile` - Owner profile

**Features:**
- Type-safe arguments (String, Long)
- Deep linking support
- Single-activity architecture
- Composable navigation graph
- Back stack management

**Rationale:** Official Jetpack library, type-safe, integrates with Compose, supports deep links for push notifications.

---

## Security

### Authentication

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Auth Model | JWT (JSON Web Tokens) | Stateless authentication |
| Token Storage | EncryptedSharedPreferences | Secure local storage (AES-256-GCM) |
| Token Expiry | Access: 15 min, Refresh: 30 days | Balance security and UX |
| Biometric Auth | BiometricPrompt (v2.0) | Fingerprint/Face unlock |

### Data Security

| Layer | Mechanism | Implementation |
|-------|-----------|---------------|
| **Network** | TLS 1.2+ | HTTPS only, no cleartext traffic |
| **Certificate Pinning** | OkHttp CertificatePinner | Pin production cert SHA-256 hashes |
| **Storage** | EncryptedSharedPreferences | Android Keystore backed, AES-256-GCM |
| **Database** | SQLCipher (planned v2.0) | Encrypted Room database |
| **Code Obfuscation** | ProGuard/R8 | Minify, obfuscate, shrink resources |

### Network Security Config

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    <domain-config>
        <domain includeSubdomains="true">coulderp.dynapharmafrica.com</domain>
        <pin-set>
            <pin digest="SHA-256">PRIMARY_PIN_HASH</pin>
            <pin digest="SHA-256">BACKUP_PIN_HASH</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### ProGuard Rules

- Keep DTO classes (serialized/deserialized)
- Keep Room entities and DAOs
- Keep Hilt modules and injected classes
- Obfuscate business logic
- Remove Timber logs in release builds

**Libraries:**
- `androidx.security:security-crypto` (1.1.0-alpha06) - EncryptedSharedPreferences
- `androidx.biometric:biometric` (1.2.0-alpha05) - BiometricPrompt API

**Rationale:** JWT is stateless and scalable, certificate pinning prevents MITM attacks, encrypted storage protects tokens at rest, ProGuard prevents reverse engineering.

---

## Image Loading

### Coil

**Version:** 2.7.0

**Library:** `io.coil-kt:coil-compose`

**Usage:**
- Owner profile photos
- Approval attachments (receipts, invoices)
- Product images in reports (if applicable)

**Features:**
- Kotlin-first, Compose-native
- Memory/disk caching
- Placeholder/error images
- Transformations (crop, blur, grayscale)
- SVG support

**Example:**
```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(profilePhotoUrl)
        .crossfade(true)
        .build(),
    contentDescription = "Owner photo",
    modifier = Modifier.size(80.dp).clip(CircleShape)
)
```

**Rationale:** Lightweight, Compose integration, Kotlin coroutines support, actively maintained, less overhead than Glide/Picasso.

---

## Charts & Visualization

### Vico (Compose Charts)

**Version:** 3.0+ (estimated, not in current version catalog)

**Purpose:**
- Dashboard KPI trend sparklines
- Sales report line charts (trends over time)
- Finance report bar charts (P&L, cash flow)
- Distributor performance pie charts

**Features:**
- Compose-native (no View interop)
- Line, bar, column, pie charts
- Customizable colors, labels, axes
- Animated chart rendering

**Alternative Considered:**
- MPAndroidChart (requires View interop, not Compose-native)

**Rationale:** Vico is built for Compose, no need for AndroidView wrapper, modern declarative API, better performance in Compose UI.

---

## Utilities

### Logging

**Library:** Timber (5.0.1)

**Usage:**
- Debug logging (network calls, cache hits/misses)
- Error logging (caught exceptions)
- Analytics events (screen views, button taps)

**Configuration:**
- Debug builds: `DebugTree()` (logs to Logcat)
- Release builds: Custom tree that sends errors to Crashlytics (future)

**ProGuard:** Removes all `Timber.d()`, `Timber.v()`, `Timber.i()` calls in release builds.

**Rationale:** Simple API, easy to disable in production, integrates with crash reporting, widely adopted.

### Splash Screen

**Library:** `androidx.core:core-splashscreen` (1.0.1)

**Purpose:** Android 12+ splash screen API compatibility (branded splash screen).

### Date/Time Formatting

- `java.time.LocalDate`, `java.time.LocalDateTime` (API 29+)
- Custom formatters for report date ranges

---

## Testing Frameworks

### Test Pyramid Summary

| Layer | Percentage | Count | Tools |
|-------|-----------|-------|-------|
| Unit Tests | 60% | 200-300 | JUnit 5, MockK, Turbine, Truth |
| Integration Tests | 25% | 80-120 | Room Testing, MockWebServer, Hilt Testing |
| UI Tests | 10% | 40-60 | Compose UI Test, Espresso |
| E2E Tests | 5% | 8-12 | Full user journeys |
| **Total** | **100%** | **~350-500** | - |

### Unit Testing

| Library | Version | Purpose |
|---------|---------|---------|
| **JUnit 5** | 5.10.3 | Test framework (assertions, lifecycle) |
| **MockK** | 1.13.11 | Kotlin mocking library |
| **Turbine** | 1.1.0 | Flow testing utilities |
| **Truth** | 1.4.4 | Fluent assertions |
| **kotlinx-coroutines-test** | 1.8.1 | TestDispatcher, runTest |

**Focus Areas:**
- ViewModel logic (state transitions, error handling)
- Use Cases (business rules, date validation)
- Repositories (cache TTL, franchise scoping)
- Mappers (DTO ↔ Domain)
- Formatters (currency, date)

### Integration Testing

| Library | Version | Purpose |
|---------|---------|---------|
| **MockWebServer** | 4.12.0 | Mock HTTP server for API tests |
| **Room Testing** | 2.6.1 | In-memory database, migration tests |
| **Hilt Testing** | 2.51.1 | DI test configuration |

**Focus Areas:**
- API + Room integration (cache-then-network flow)
- Franchise context switching (data isolation)
- Token refresh interceptor (401 → refresh → retry)
- Cache TTL expiration logic

### UI Testing (Compose)

| Library | Version | Purpose |
|---------|---------|---------|
| **Compose UI Test** | BOM 2024.06.00 | Semantics tree testing |
| **Espresso** | 3.6.1 | View interactions (if needed) |
| **Hilt Android Testing** | 2.51.1 | Inject test doubles |

**Focus Areas:**
- Report table rendering (empty state, single row, large dataset)
- Approval form validation (required comments)
- Dashboard KPI cards (formatting, click behavior)
- Franchise switcher (selection updates UI)

### Static Analysis

| Tool | Version | Purpose |
|------|---------|---------|
| **Android Lint** | Built-in | XML, resource, API usage checks |
| **Detekt** | 1.23+ | Kotlin code smell detection |

### Code Coverage

| Tool | Version | Target Coverage |
|------|---------|----------------|
| **JaCoCo** | 0.8.11+ | 80% line, 70% branch |

### Test Runners

- `HiltTestRunner` - Custom instrumentation runner for Hilt
- `Robolectric` (4.11+) - Android framework mocking (future)

**Rationale:** JUnit 5 is modern and feature-rich, MockK is Kotlin-first, Turbine simplifies Flow testing, Compose UI Test uses semantics for robust tests, MockWebServer isolates API tests.

---

## Development Tools

### IDE

**Android Studio** (Iguana 2023.2.1+)
- Kotlin plugin
- Compose preview
- Layout inspector
- Database inspector (Room)
- Network profiler

### Build Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Gradle Wrapper | 8.9+ | Build automation |
| Gradle Kotlin DSL | - | Type-safe build scripts |
| Version Catalog | TOML | Centralized dependency versions |

### Version Control

- **Git** - Source control
- **GitHub** - Repository hosting (assumed)

### Code Quality Tools

- **ktlint** - Kotlin linter (enforces style guide)
- **Detekt** - Static analysis
- **SonarQube** - Code quality platform (optional)

### Debugging Tools

- **Timber** - Logging
- **Chucker** - HTTP inspector (debug builds, optional)
- **LeakCanary** - Memory leak detection (debug builds, optional)

**Rationale:** Android Studio is the official IDE with best tooling, Gradle Kotlin DSL provides type safety, version catalog centralizes dependency management.

---

## Backend Integration

### DMS_web Backend

| Field | Value |
|-------|-------|
| **Product** | DMS_web - Dynapharm Distributor Management System |
| **Backend Stack** | PHP 8.2+ / MySQL 9.1 |
| **API Style** | RESTful (custom envelope format) |
| **Authentication** | JWT (Access + Refresh tokens) |
| **Multi-tenancy** | `franchise_id` in JWT payload, query scoping |

### API Base URLs

| Environment | URL |
|-------------|-----|
| **Development** | `http://dynapharm.peter/` (Android emulator localhost) |
| **Staging** | `https://erp.dynapharmafrica.com/` |
| **Production** | `https://coulderp.dynapharmafrica.com/` |

### API Endpoints

| Category | Endpoint Count | Examples |
|----------|---------------|----------|
| Auth | 3 | login, refresh, logout |
| Dashboard | 1 | dashboard-stats |
| Franchises | 2 | list, switch |
| Reports | 23+ | daily-sales, profit-loss, cash-flow, distributor-performance, etc. |
| Approvals | 7 | expense, purchase-order, stock-transfer, payroll, leave, etc. |
| Profile | 2 | get, update (multipart for photo) |

### Response Envelope

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "meta": {
    "page": 1,
    "perPage": 50,
    "total": 250,
    "totalPages": 5
  }
}
```

### Error Handling

| HTTP Code | Meaning | App Behavior |
|-----------|---------|--------------|
| 200 | Success | Show data |
| 400 | Bad request | Show validation errors |
| 401 | Unauthorized | Refresh token or redirect to login |
| 403 | Forbidden | Show "Access denied" |
| 404 | Not found | Show "Resource not found" |
| 409 | Conflict | Show "Data changed, refresh" (for approvals) |
| 500 | Server error | Show "Server error, try later" |
| Network error | No connection | Show cached data + offline indicator |

**Rationale:** Shared backend reduces maintenance, JWT enables stateless auth, envelope format standardizes responses, franchise_id scoping ensures multi-tenancy security.

---

## CI/CD Pipeline

### CI Platform

**GitHub Actions** (assumed based on Git usage)

### Pipeline Stages

```
PR Opened / Push to main
    ↓
[Lint + Detekt] → fail? → block merge
    ↓
[Unit Tests] → fail? → block merge
    ↓
[Integration Tests] → fail? → block merge
    ↓
[Coverage Check] → below 70%? → warning (not blocking)
    ↓
[Build Release APK] → upload artifact
    ↓
[PR Approved] → merge
```

### Timing Targets

| Stage | Target | Max |
|-------|--------|-----|
| Lint + Detekt | 1 min | 2 min |
| Unit Tests | 3 min | 5 min |
| Integration Tests | 5 min | 10 min |
| Build APK | 3 min | 5 min |
| **Total** | **~12 min** | **~22 min** |

### Artifacts

- **Debug APK** - Every commit
- **Staging APK** - Tagged releases
- **Production APK** - Manual trigger (signed with upload key)
- **Test Reports** - JUnit XML, JaCoCo HTML
- **Lint Reports** - HTML

### Secrets Management

- Keystore file (Base64 encoded in GitHub Secrets)
- Keystore password
- Key alias and password
- Staging/Production API base URLs
- Certificate pins

**Rationale:** GitHub Actions integrates with GitHub repos, free for open source, YAML-based configuration, extensive marketplace for actions.

---

## Monitoring & Analytics

### Planned (v1.1+)

| Tool | Purpose | Integration |
|------|---------|-------------|
| **Firebase Crashlytics** | Crash reporting | SDK integration, Timber custom tree |
| **Firebase Analytics** | User behavior | Screen views, button taps, report opens |
| **Firebase Performance Monitoring** | App startup, network latency | Automatic traces |
| **Firebase Cloud Messaging (FCM)** | Push notifications | Approval alerts, KPI anomalies |
| **Play Console Vitals** | Crash rate, ANR rate | Built-in (no SDK) |

### Key Metrics

| Metric | Target | Source |
|--------|--------|--------|
| Crash-free users | > 99.5% | Crashlytics |
| ANR rate | < 0.2% | Play Console Vitals |
| API success rate | > 99.9% | Server logs |
| Report load time | < 2s (cached), < 3s (network) | Performance Monitoring |
| Cache hit rate | > 60% | Custom analytics |

**Rationale:** Firebase suite provides comprehensive monitoring, integrates with Google Play Console, free tier covers most use cases, Crashlytics is industry standard for crash reporting.

---

## Technology Selection Rationale

### Why Kotlin over Java?

- **Null safety** - Reduces NullPointerExceptions by design
- **Conciseness** - Less boilerplate (data classes, extension functions)
- **Coroutines** - Simpler async code than callbacks/RxJava
- **Tooling** - First-class support in Android Studio
- **Adoption** - 95%+ of top Android apps use Kotlin

### Why Compose over XML Layouts?

- **Declarative** - UI = f(state), easier to reason about
- **Less code** - No separate XML files, no findViewById
- **Recomposition** - Efficient UI updates (skip unnecessary redraws)
- **Theming** - Material 3 built-in, dynamic colors
- **Testing** - Semantics tree is more robust than View hierarchy

### Why Hilt over Manual DI?

- **Compile-time** - Errors caught at build time, not runtime
- **Lifecycle-aware** - Auto-handles Android lifecycles (Activity, Fragment, ViewModel)
- **Standard** - Official Google recommendation, replaces Dagger-Android
- **Less boilerplate** - @HiltAndroidApp, @AndroidEntryPoint, @Inject

### Why Room over Realm/SQLite?

- **SQL power** - Full SQL support, complex queries
- **Type-safe** - DAO methods return typed results
- **Jetpack integration** - LiveData, Flow support
- **Migration** - Clear migration path, schema export
- **Testing** - In-memory database for fast tests

### Why Retrofit over Ktor/Volley?

- **Maturity** - Battle-tested since 2013
- **Ecosystem** - Large community, many converters (Gson, Moshi, KotlinX Serialization)
- **OkHttp integration** - Reuses OkHttp's robust HTTP stack
- **Interceptors** - Easy auth, logging, retry logic
- **Type-safe** - Generates API clients from interfaces

### Why Kotlin Serialization over Gson/Moshi?

- **Compile-time** - No reflection, faster startup
- **Multiplatform** - Works on JVM, JS, Native
- **Type-safe** - Compiler enforces schema
- **Smaller** - No runtime reflection library
- **Official** - JetBrains-maintained

### Why WorkManager over AlarmManager/JobScheduler?

- **Guaranteed execution** - Survives app kill, device reboot
- **Constraints** - Network, battery, storage conditions
- **Backward compat** - Works on API 14+ (uses JobScheduler/AlarmManager under the hood)
- **Hilt integration** - Inject dependencies into Workers
- **Chaining** - Sequential/parallel work requests

---

## Build Variants

| Variant | Application ID | API URL | Minify | Cert Pinning | Logging |
|---------|---------------|---------|--------|--------------|---------|
| **debug** | `com.dynapharm.ownerhub.debug` | `http://dynapharm.peter/` | No | No | Full |
| **staging** | `com.dynapharm.ownerhub.staging` | `https://erp.dynapharmafrica.com/` | Yes | Staging pin | Full |
| **release** | `com.dynapharm.ownerhub` | `https://coulderp.dynapharmafrica.com/` | Yes | Prod pins | Errors only |

---

## Multi-Language Support

**i18n Libraries:** Android `strings.xml` (built-in)

**Supported Languages (v1.0):**
- English (en) - Default
- French (fr)
- Arabic (ar) - RTL layout support
- Swahili (sw)
- Spanish (es)

**Strategy:**
- `strings.xml` per locale (`values/`, `values-fr/`, `values-ar/`, etc.)
- `Locale.getDefault()` for date/currency formatting
- RTL layout testing for Arabic

---

## Accessibility

- **TalkBack** - Screen reader support via Compose semantics
- **Large text** - Respect system font scaling
- **High contrast** - Material 3 dynamic colors
- **Keyboard navigation** - Focusable composables

---

## Performance Optimizations

| Optimization | Implementation |
|-------------|---------------|
| **Compose strong skipping** | Enabled via compiler flag |
| **ProGuard/R8** | Minify, shrink resources |
| **Coil caching** | Memory + disk cache for images |
| **Room indexing** | Index on `franchise_id`, `expires_at` |
| **OkHttp connection pooling** | Reuse HTTP connections |
| **Lazy lists** | `LazyColumn` for large reports |
| **Pagination** | Server-side pagination for 500+ row reports |

---

## Version Catalog Highlights

All dependency versions centralized in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.0.10"
compose-bom = "2024.06.00"
hilt = "2.51.1"
retrofit = "2.11.0"
room = "2.6.1"
coil = "2.7.0"
...

[libraries]
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
...

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
...
```

**Benefits:** Single source of truth, easy updates, type-safe in Gradle Kotlin DSL.

---

## Related Documentation

| Document | Location | Description |
|----------|----------|-------------|
| **Planning Docs** | `docs/android-app-owner/` | PRD, SRS, SDS, API Contract, User Journeys, Testing, Release Plan |
| **Phase 1 Guide** | `docs/android-app-owner/phase-1/` | Bootstrap implementation guide (login, dashboard, tabs) |
| **Backend Source** | `../../DMS_web/` | PHP/MySQL backend (assumed sibling directory) |
| **Distributor App** | `docs/android-app-distributor/` | Sister app for distributors (shares backend) |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-09 | Claude Code | Initial tech stack documentation based on planning docs |

---

**End of Document**
