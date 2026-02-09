# User Journeys -- Dynapharm Owner Portal App

**Document:** User Journeys Index
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft
**Owner:** Dynapharm International

---

## Overview

This document catalogs 12 user journeys for the Dynapharm Owner Portal Android App. Each journey maps the complete user flow from entry to outcome, including happy paths, error handling, and offline scenarios.

The Owner Portal is a **read-only strategic command center** -- most journeys involve viewing data (dashboards, reports) rather than creating or editing records. The only write-path journeys are Expense Approval (Journey 8) and Profile Management (Journey 11).

---

## Sub-Documents

The 12 journeys are split into two sub-files for maintainability.

| # | Document | Path | Journeys Covered |
|---|----------|------|-----------------|
| 1 | [Journeys 1 to 6](journeys/01-journeys-1-to-6.md) | `journeys/01-journeys-1-to-6.md` | Onboarding, Login, Dashboard KPI, Franchise Switching, Sales Report, Finance Report |
| 2 | [Journeys 7 to 12](journeys/02-journeys-7-to-12.md) | `journeys/02-journeys-7-to-12.md` | Inventory Report, Expense Approval, Distributor Report, Genealogy Tree, Profile Management, Error Recovery |

---

## Quick Navigation

- **Previous:** [04_API_CONTRACT.md](04_API_CONTRACT.md) -- API Contract
- **Next:** [06_TESTING_STRATEGY.md](06_TESTING_STRATEGY.md) -- Testing Strategy
- **All Docs:** [README.md](README.md)

---

## Journey Index

| # | Journey | Type | MVP? | Key Screens | Complexity |
|---|---------|------|------|-------------|-----------|
| 1 | First-Time Onboarding | Setup | Yes | Welcome, Login, Franchise Select, Dashboard | Medium |
| 2 | Returning Login | Auth | Yes | Biometric/PIN, Dashboard | Low |
| 3 | Dashboard KPI Review | Read | Yes | Dashboard (5 KPIs, trends, sparklines) | Medium |
| 4 | Franchise Switching | Context | Yes | Franchise List, Dashboard refresh | Medium |
| 5 | Sales Report Deep Dive | Read | Yes | Report List, Daily Sales, Filters, Chart, Table | High |
| 6 | Finance Report Review | Read | Yes | Report List, P&L, Cash Flow, Filters | High |
| 7 | Inventory Report Check | Read | v1.1 | Report List, Stock Levels, Transfer Log | Medium |
| 8 | Expense Approval Flow | Write | Yes | Approval Queue, Expense Detail, Approve/Reject | High |
| 9 | Distributor Performance Review | Read | v1.1 | Report List, Performance Table, Drill-Down | Medium |
| 10 | Genealogy Tree Exploration | Read | v1.1 | Genealogy Search, Tree View, Node Detail | High |
| 11 | Profile Management | Write | Yes | Profile View, Edit Form, Photo Upload | Low |
| 12 | Error Recovery | System | Yes | Error Screen, Retry, Offline Indicator | Medium |

---

## ASCII Symbol Legend

All journey diagrams in the sub-documents use this consistent symbol set.

| Symbol | Meaning |
|--------|---------|
| `[Screen Name]` | A screen or page the user sees |
| `(Action)` | A user action (tap, swipe, input) |
| `{Decision}` | A system or user decision point |
| `-->` | Flow direction (forward) |
| `<--` | Flow direction (backward/return) |
| `***` | Error/exception path |
| `~~~` | Offline/cached path |
| `===` | Background process |
| `!!!` | Critical action (approve/reject, submit) |
| `...` | Loading/waiting state |

### Journey Diagram Template

```
[Start Screen]
    |
    (User Action)
    |
    {Decision Point}
   / \
  Y   N
  |   |
  v   v
[Screen A]  [Screen B]
    |
    (Next Action)
    |
[End Screen]
```

---

## Journey Categories

### Setup Journeys (1-2)

These cover the initial app experience and returning user authentication.

- **Journey 1 -- First-Time Onboarding:** Download -> open -> login -> select franchise -> view dashboard. Includes language selection, terms acceptance, and franchise context initialization.
- **Journey 2 -- Returning Login:** App open -> biometric/PIN -> auto-load cached dashboard -> background refresh. Covers token refresh, biometric failure fallback, and offline cached login.

### Read Journeys (3-7, 9-10)

These are the core of the owner portal -- viewing data and exploring reports.

- **Journey 3 -- Dashboard KPI Review:** View 5 KPI cards (Sales MTD, Cash Balance, Inventory Value, Total BV, Pending Approvals) with trend sparklines. Pull-to-refresh for live data.
- **Journey 4 -- Franchise Switching:** Open franchise drawer -> select different franchise -> dashboard and cache reset -> new franchise data loads.
- **Journey 5 -- Sales Report Deep Dive:** Select sales category -> pick report type -> apply date/branch filters -> view chart -> scroll table -> export/share.
- **Journey 6 -- Finance Report Review:** Similar to Journey 5 but with hierarchical data (P&L sections, balance sheet categories).
- **Journey 7 -- Inventory Report Check:** View stock levels, transfer history, adjustment logs with date filtering.
- **Journey 9 -- Distributor Performance Review:** View performance table with ranking, drill into individual distributor details.
- **Journey 10 -- Genealogy Tree Exploration:** Search distributor -> view tree structure -> tap node for details -> navigate up/down tree.

### Write Journeys (8, 11)

These are the only journeys involving data mutation.

- **Journey 8 -- Expense Approval Flow:** View approval queue badge count -> tap pending item -> review expense details (amount, category, requester, attachments) -> approve or reject with mandatory comment -> confirmation -> queue updates.
- **Journey 11 -- Profile Management:** View profile -> edit contact info -> upload/change photo -> save changes.

### System Journeys (12)

- **Journey 12 -- Error Recovery:** Covers network errors, auth expiry, server errors, and offline states. Shows retry flows, cached data fallback, and re-authentication paths.

---

## Offline Behavior Summary

| Journey | Offline Behavior |
|---------|-----------------|
| 1 - Onboarding | Cannot proceed (requires network) |
| 2 - Login | Biometric unlock with cached session (if token not expired) |
| 3 - Dashboard | Show cached KPIs with "Last updated X ago" indicator |
| 4 - Franchise Switch | Show cached franchise list; data refresh deferred until online |
| 5 - Sales Report | Show cached report with stale indicator |
| 6 - Finance Report | Show cached report with stale indicator |
| 7 - Inventory Report | Show cached report with stale indicator |
| 8 - Expense Approval | Cannot approve/reject (queued message shown, retry when online) |
| 9 - Distributor Report | Show cached report with stale indicator |
| 10 - Genealogy Tree | Show cached tree with stale indicator |
| 11 - Profile Edit | Cannot save (queued message shown, retry when online) |
| 12 - Error Recovery | Show offline banner, enable retry when connectivity returns |

---

## Cross-References

| Journey | Related PRD User Stories | Related SRS Requirements | Related API Endpoints |
|---------|------------------------|-------------------------|----------------------|
| 1 | US-AUTH-01 to US-AUTH-04 | FR-AUTH-* | `/api/auth/login.php` |
| 2 | US-AUTH-05 to US-AUTH-08 | FR-AUTH-* | `/api/auth/refresh.php` |
| 3 | US-DASH-01 to US-DASH-06 | FR-DASH-* | `/api/owners/dashboard-stats.php` |
| 4 | US-FRAN-01 to US-FRAN-04 | FR-FRAN-* | `/api/owners/franchises.php` |
| 5 | US-SRPT-01 to US-SRPT-07 | FR-SRPT-* | `/api/owners/daily-sales.php` + 6 more |
| 6 | US-FRPT-01 to US-FRPT-08 | FR-FRPT-* | `/api/owners/cash-flow.php` + 7 more |
| 7 | US-IRPT-01 to US-IRPT-03 | FR-IRPT-* | `/api/owners/inventory-valuation.php` + 2 more |
| 8 | US-APPR-01 to US-APPR-07 | FR-APPR-* | `/api/owners/approvals/*.php` |
| 9 | US-DRPT-01 to US-DRPT-05 | FR-DRPT-* | `/api/owners/distributor-performance.php` + 4 more |
| 10 | US-DRPT-04 | FR-DRPT-04 | `/api/owners/genealogy.php` |
| 11 | US-PROF-01 to US-PROF-05 | FR-PROF-* | `/api/owners/profile.php` |
| 12 | US-SYS-01 to US-SYS-04 | NFR-ERR-* | N/A (client-side) |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial user journeys index |
