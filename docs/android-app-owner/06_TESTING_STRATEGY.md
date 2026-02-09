# Testing Strategy -- Dynapharm Owner Portal App

**Document:** Testing Strategy Index
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft
**Owner:** Dynapharm International

---

## Overview

This document defines the testing strategy for the Dynapharm Owner Portal Android App. The app's **read-heavy, write-light** nature shapes the testing approach: the majority of tests focus on data retrieval, report rendering, caching behavior, and franchise context isolation, rather than CRUD validation.

### Testing Philosophy

1. **Test the read path exhaustively** -- Reports are the core value; every report must render correctly with varied data shapes (empty, single row, thousands of rows, null fields)
2. **Test approval workflows thoroughly** -- The only write path; must be reliable, auditable, and handle network failures gracefully
3. **Test franchise switching rigorously** -- Context isolation is a security requirement; stale data from a previous franchise must never leak
4. **Test caching correctness** -- Stale-while-revalidate must show the right data at the right time with correct freshness indicators
5. **Automate everything possible** -- CI pipeline runs on every PR; no manual testing for regressions

---

## Sub-Documents

The testing strategy is split into two focused sub-files for maintainability.

| # | Document | Path | Summary |
|---|----------|------|---------|
| 1 | [Unit and UI Tests](testing/01-unit-ui-tests.md) | `testing/01-unit-ui-tests.md` | ViewModel tests (report loading, KPI formatting, approval state), UseCase tests (business logic, date range validation), Repository tests (cache TTL, franchise scoping), Compose UI tests (report table rendering, chart display, approval form validation) |
| 2 | [Integration, Security, and Performance](testing/02-integration-security-performance.md) | `testing/02-integration-security-performance.md` | API integration tests (MockWebServer), Room migration tests, end-to-end approval flow, security tests (JWT handling, franchise isolation, cert pinning), performance benchmarks (report load time, cache hit rate, memory) |

---

## Quick Navigation

- **Previous:** [05_USER_JOURNEYS.md](05_USER_JOURNEYS.md) -- User Journeys
- **Next:** [07_RELEASE_PLAN.md](07_RELEASE_PLAN.md) -- Release Plan
- **All Docs:** [README.md](README.md)

---

## Test Pyramid

```
        /\
       /  \        E2E Tests (5%)
      /    \       8-12 tests
     /------\
    /        \     UI Tests (10%)
   /          \    40-60 tests
  /------------\
 /              \  Integration Tests (25%)
/                \ 80-120 tests
/------------------\
                     Unit Tests (60%)
                     200-300 tests
```

| Layer | Percentage | Est. Count | Focus Areas |
|-------|-----------|-----------|-------------|
| Unit | 60% | 200-300 | ViewModels, UseCases, Repositories, Mappers, Formatters |
| Integration | 25% | 80-120 | API + Room, Cache lifecycle, Franchise switching |
| UI | 10% | 40-60 | Compose screens, Report tables, Approval forms |
| E2E | 5% | 8-12 | Login -> Dashboard -> Report -> Approval (critical paths) |
| **Total** | **100%** | **~350-500** | -- |

---

## Test Distribution by Module

| Module | Unit | Integration | UI | E2E | Total |
|--------|------|------------|-----|-----|-------|
| Auth | 15 | 10 | 5 | 2 | 32 |
| Dashboard | 20 | 8 | 8 | 1 | 37 |
| Franchise Switcher | 10 | 8 | 5 | 1 | 24 |
| Sales Reports (7) | 35 | 14 | 7 | 1 | 57 |
| Finance Reports (8) | 40 | 16 | 8 | 1 | 65 |
| Inventory Reports (3) | 15 | 6 | 3 | 0 | 24 |
| HR/Payroll Reports (3) | 15 | 6 | 3 | 0 | 24 |
| Distributor Reports (5) | 25 | 10 | 5 | 1 | 41 |
| Compliance Reports (2) | 10 | 4 | 2 | 0 | 16 |
| Approvals (7) | 35 | 21 | 7 | 2 | 65 |
| Profile | 8 | 5 | 3 | 1 | 17 |
| Caching/Offline | 15 | 12 | 3 | 1 | 31 |
| **Total** | **~243** | **~120** | **~59** | **~11** | **~433** |

---

## Quick Reference Commands

### Run All Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# All tests with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Run Tests by Module

```bash
# Auth tests only
./gradlew testDebugUnitTest --tests "com.dynapharm.ownerhub.*.auth.*"

# Dashboard tests only
./gradlew testDebugUnitTest --tests "com.dynapharm.ownerhub.*.dashboard.*"

# Report tests only
./gradlew testDebugUnitTest --tests "com.dynapharm.ownerhub.*.reports.*"

# Approval tests only
./gradlew testDebugUnitTest --tests "com.dynapharm.ownerhub.*.approvals.*"
```

### Lint and Static Analysis

```bash
# Android Lint
./gradlew lintRelease

# Detekt (Kotlin static analysis)
./gradlew detekt
```

---

## Key Testing Libraries

| Library | Purpose | Version |
|---------|---------|---------|
| JUnit 5 | Test framework | 5.10+ |
| MockK | Kotlin mocking | 1.13+ |
| Turbine | Flow testing | 1.1+ |
| Compose UI Test | Compose screen tests | BOM 2024.06+ |
| MockWebServer | API integration tests | 4.12+ |
| Room Testing | Migration/DAO tests | 2.6+ |
| Robolectric | Android framework mocking | 4.11+ |
| Hilt Testing | DI test configuration | 2.51+ |
| JaCoCo | Code coverage | 0.8.11+ |
| Detekt | Kotlin static analysis | 1.23+ |

---

## Coverage Targets

| Metric | Target | Minimum |
|--------|--------|---------|
| Line coverage (overall) | 80% | 70% |
| Branch coverage (overall) | 70% | 60% |
| ViewModel coverage | 90% | 80% |
| UseCase coverage | 95% | 90% |
| Repository coverage | 85% | 75% |
| UI Screen coverage | 60% | 50% |

---

## CI Pipeline Summary

```
PR Opened / Push to main
    |
    v
[Lint + Detekt] --> fail? --> block merge
    |
    v
[Unit Tests] --> fail? --> block merge
    |
    v
[Integration Tests] --> fail? --> block merge
    |
    v
[Coverage Check] --> below 70%? --> warning (not blocking)
    |
    v
[Build Release APK] --> upload artifact
    |
    v
[PR Approved] --> merge
```

### CI Timing Targets

| Stage | Target Duration | Max Duration |
|-------|----------------|-------------|
| Lint + Detekt | 1 min | 2 min |
| Unit Tests | 3 min | 5 min |
| Integration Tests | 5 min | 10 min |
| Build APK | 3 min | 5 min |
| **Total Pipeline** | **~12 min** | **~22 min** |

---

## Owner-Portal-Specific Test Patterns

### Report Rendering Test Pattern

Every report screen should test these scenarios:

1. **Empty state** -- No data for date range; verify empty state UI
2. **Single row** -- Minimal data; verify table renders correctly
3. **Large dataset** -- 500+ rows; verify pagination and scroll performance
4. **Null fields** -- Missing optional fields; verify graceful handling
5. **Currency formatting** -- Verify locale-specific number formatting
6. **Date range filtering** -- Verify start/end date params passed to API
7. **Cached data** -- Verify stale indicator shows when serving from cache

### Franchise Isolation Test Pattern

Every data-accessing test should verify:

1. Load data for Franchise A
2. Switch to Franchise B
3. Verify Franchise A data is NOT visible
4. Verify Franchise B data loads correctly
5. Switch back to Franchise A
6. Verify data reloads (not stale Franchise B data)

### Approval Workflow Test Pattern

Each approval type should test:

1. **Queue display** -- Pending items listed with correct counts
2. **Detail view** -- All fields rendered, attachments accessible
3. **Approve path** -- Comment required, success confirmation, queue count decrements
4. **Reject path** -- Comment required, success confirmation, queue count decrements
5. **Network failure** -- Error shown, approval NOT submitted, retry available
6. **Concurrent modification** -- Server returns 409 Conflict, user prompted to refresh

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial testing strategy index |
