# Software Design Specification (SDS) -- Dynapharm Owner Portal App

**Document:** SDS Index
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft
**Owner:** Dynapharm International

---

## Overview

This SDS defines the technical architecture, build configuration, dependency injection, caching strategy, security implementation, and networking layer for the Dynapharm Owner Portal Android App.

The Owner Portal is architecturally distinct from the Distributor App in several ways:

- **Read-heavy, write-light** -- 95% of operations are data retrieval (reports, dashboards)
- **Report-centric caching** -- Room DB used primarily as a report cache, not a sync source
- **Multi-franchise context** -- Architecture must support seamless franchise switching without app restart
- **Approval workflow** -- The only stateful write path; requires careful optimistic UI and rollback handling
- **Shared JWT infrastructure** -- Reuses the same auth backend as the distributor app

---

## Sub-Documents

This SDS is split into six focused sub-files for maintainability.

| # | Document | Path | Summary |
|---|----------|------|---------|
| 1 | [Architecture](sds/01-architecture.md) | `sds/01-architecture.md` | MVVM + Clean Architecture layers, package structure, module dependency graph, franchise context management, navigation graph |
| 2 | [Gradle Configuration](sds/02-gradle-config.md) | `sds/02-gradle-config.md` | Version catalog, build variants (dev/staging/prod), ProGuard rules, module structure, dependency declarations |
| 3 | [Hilt Modules](sds/03-hilt-modules.md) | `sds/03-hilt-modules.md` | DI module definitions (Network, Database, Repository, Auth), custom scopes (FranchiseScope), qualifier annotations |
| 4 | [Offline Caching](sds/04-offline-sync.md) | `sds/04-offline-sync.md` | Report caching strategy (stale-while-revalidate), Room schema for cached reports, TTL management, cache invalidation on franchise switch, WorkManager background refresh |
| 5 | [Security](sds/05-security.md) | `sds/05-security.md` | JWT storage (EncryptedSharedPreferences), certificate pinning, biometric authentication, data encryption at rest, ProGuard obfuscation, network security config |
| 6 | [Networking](sds/06-networking.md) | `sds/06-networking.md` | Retrofit service definitions, OkHttp interceptors (auth, franchise context, logging), response envelope handling, pagination support, error mapping |

---

## Quick Navigation

- **Previous:** [02_SRS.md](02_SRS.md) -- Software Requirements Specification
- **Next:** [04_API_CONTRACT.md](04_API_CONTRACT.md) -- API Contract
- **All Docs:** [README.md](README.md)

---

## Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Architecture | MVVM + Clean Architecture | Separation of concerns, testability, follows Android best practices |
| UI Framework | Jetpack Compose + Material 3 | Declarative UI, less boilerplate, modern Android standard |
| DI | Dagger Hilt | Compile-time DI, Android lifecycle integration, industry standard |
| Networking | Retrofit + OkHttp | Mature, well-tested, interceptor chain for auth/franchise context |
| Serialization | Kotlin Serialization | Type-safe, compile-time, no reflection |
| Local Cache | Room | Report caching with TTL, structured queries for offline report viewing |
| Auth Storage | EncryptedSharedPreferences | Secure JWT storage, Android Keystore backed |
| Report Charts | Vico (Compose-native) | Native Compose charting, no View interop needed |
| Image Loading | Coil | Kotlin-first, Compose integration, lightweight |
| Caching Strategy | Stale-While-Revalidate | Show cached data immediately, refresh in background |
| Franchise Context | Hilt custom scope | Rebuild DI graph on franchise switch, scoped repositories |
| Navigation | Navigation Compose | Type-safe, deep link support, single activity |

---

## Architecture Overview

```
Presentation Layer (Compose UI + ViewModels)
        |
        v
Domain Layer (Use Cases + Repository Interfaces)
        |
        v
Data Layer (Repository Implementations + Data Sources)
    /           \
Remote          Local
(Retrofit)      (Room Cache)
```

### Package Structure

```
com.dynapharm.ownerhub/
  di/                     # Hilt modules
  data/
    remote/
      api/                # Retrofit service interfaces
      dto/                # Data Transfer Objects
      interceptor/        # OkHttp interceptors
    local/
      db/                 # Room database, DAOs, entities
      prefs/              # EncryptedSharedPreferences
    repository/           # Repository implementations
  domain/
    model/                # Domain models
    repository/           # Repository interfaces
    usecase/
      auth/               # Auth use cases
      dashboard/          # Dashboard use cases
      reports/            # Report use cases (by category)
      approvals/          # Approval use cases
      profile/            # Profile use cases
  presentation/
    navigation/           # NavHost, routes
    theme/                # Material 3 theme, colors, typography
    common/               # Shared composables (KPICard, ReportTable, etc.)
    screens/
      auth/               # Login, biometric screens
      dashboard/          # Dashboard screen + ViewModel
      franchise/          # Franchise switcher
      reports/
        sales/            # 7 sales report screens
        finance/          # 8 finance report screens
        inventory/        # 3 inventory report screens
        hr/               # 3 HR/payroll report screens
        distributors/     # 5 distributor report screens
        compliance/       # 2 compliance report screens
      approvals/          # Approval queue + detail screens
      profile/            # Profile screen
  util/                   # Extensions, formatters, constants
```

### Franchise Context Flow

```
Owner logs in
  --> JWT contains list of franchise_ids owner has access to
  --> App loads franchise list from /api/owners/franchises.php
  --> User selects franchise (or auto-selects if single franchise)
  --> FranchiseContextManager stores selected franchise_id
  --> All API calls include franchise_id header/parameter
  --> On franchise switch:
      1. Clear all cached report data (Room)
      2. Reset ViewModels
      3. Refresh dashboard for new franchise
      4. Update approval badge count
```

---

## Reading Order

For implementers, read the sub-documents in this order:

1. **`sds/01-architecture.md`** -- Understand the layer structure and module boundaries
2. **`sds/02-gradle-config.md`** -- Set up the project build
3. **`sds/03-hilt-modules.md`** -- Understand DI setup and scoping
4. **`sds/06-networking.md`** -- Understand API communication layer
5. **`sds/04-offline-sync.md`** -- Understand caching and offline behavior
6. **`sds/05-security.md`** -- Understand security requirements and implementation

---

## Differences from Distributor App

| Aspect | Distributor App | Owner Portal App |
|--------|----------------|-----------------|
| Primary use | CRUD operations (clients, invoices) | Report viewing, approvals |
| Offline writes | Sync queue for creates/updates | No offline writes (approvals need network) |
| Room usage | Source of truth with sync | Report cache with TTL |
| Write operations | Many (clients, invoices, payout requests) | Few (approve/reject, profile edit) |
| Franchise context | Single franchise per user | Multiple franchises, switchable |
| Data volume | Moderate (user's own data) | High (franchise-wide aggregate reports) |
| Chart complexity | Simple (personal stats) | Complex (multi-series, trends, comparisons) |
| Cache invalidation | On sync conflict | On franchise switch + TTL expiry |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial SDS index |
