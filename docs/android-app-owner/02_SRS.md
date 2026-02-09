# Software Requirements Specification (SRS) -- Dynapharm Owner Portal App

**Document:** SRS Index
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft
**Owner:** Dynapharm International

---

## Overview

This SRS defines the functional requirements, non-functional requirements, data models, and error handling strategy for the Dynapharm Owner Portal Android App. The app serves as a **read-only strategic command center** for franchise owners, providing executive dashboards, 23 reports across 6 categories, 7 approval workflows, and multi-franchise switching.

Given the app's read-heavy nature (95% of interactions are data retrieval), the SRS places particular emphasis on:

- **Data freshness** -- how stale can cached reports be before requiring a refresh
- **Report rendering** -- displaying tabular and chart data efficiently on mobile screens
- **Approval state management** -- ensuring approve/reject actions are reliable and auditable
- **Multi-franchise context** -- all queries scoped to the currently selected franchise

---

## Sub-Documents

This SRS is split into three focused sub-files for maintainability.

| # | Document | Path | Summary |
|---|----------|------|---------|
| 1 | [Functional Requirements](srs/01-functional-requirements.md) | `srs/01-functional-requirements.md` | Detailed functional specs for all 11 modules: Auth, Dashboard, Franchise Switcher, Sales Reports (7), Finance Reports (8), Inventory Reports (3), HR/Payroll Reports (3), Distributor Reports (5), Compliance Reports (2), Approvals (7 workflows), Profile |
| 2 | [Non-Functional Requirements and Data Models](srs/02-nonfunctional-data-models.md) | `srs/02-nonfunctional-data-models.md` | Performance targets (report load < 3s), security requirements, Room entities for report caching, DTO definitions, data freshness policies per report category, accessibility standards |
| 3 | [Error Handling and Traceability](srs/03-error-handling-traceability.md) | `srs/03-error-handling-traceability.md` | Error taxonomy (network, auth, server, business rule), user-facing error messages, retry policies, requirement traceability matrix (SRS -> PRD user stories) |

---

## Quick Navigation

- **Previous:** [01_PRD.md](01_PRD.md) -- Product Requirements Document
- **Next:** [03_SDS.md](03_SDS.md) -- Software Design Specification
- **All Docs:** [README.md](README.md)

---

## Requirements Summary

### Functional Requirements by Module

| Module | Req Count | Complexity | Key Behaviors |
|--------|-----------|-----------|---------------|
| Auth | 8 | Medium | JWT login, token refresh, biometric unlock, multi-franchise token |
| Dashboard | 6 | Medium | 5 KPI cards, trend sparklines, pull-to-refresh, auto-refresh |
| Franchise Switcher | 4 | Low | List franchises, switch context, persist selection, badge counts |
| Sales Reports (7) | 21 | High | Date filtering, chart rendering, table pagination, export |
| Finance Reports (8) | 24 | High | Hierarchical data (P&L, Balance Sheet), cash flow visualization |
| Inventory Reports (3) | 9 | Medium | Stock levels, transfer logs, adjustment history |
| HR/Payroll Reports (3) | 9 | Medium | Payroll summaries, leave tracking, activity logs |
| Distributor Reports (5) | 15 | High | Genealogy tree, performance metrics, rank distribution |
| Compliance Reports (2) | 6 | Medium | Map visualization, audit trail |
| Approvals (7) | 28 | High | Queue list, detail view, approve/reject, comments, audit trail |
| Profile | 5 | Low | View/edit contact, upload photo, change password |
| **Total** | **~135** | -- | -- |

### Non-Functional Requirements Summary

| Category | Key Targets |
|----------|------------|
| Performance | Dashboard load < 2s, report load < 3s, approval action < 1s |
| Availability | 99.5% uptime for API, graceful degradation with cached data |
| Security | JWT with 15-min access token, encrypted local storage, cert pinning |
| Scalability | Support 500+ concurrent owner sessions |
| Localization | 5 languages (en, fr, ar, sw, es), RTL support for Arabic |
| Accessibility | WCAG 2.1 AA, TalkBack support, min 48dp touch targets |
| Data Freshness | Dashboard: 5 min TTL, Reports: 15 min TTL, Approvals: real-time |
| Offline | Cached reports viewable offline, approvals require connectivity |

### Data Freshness Policy

| Data Type | Cache TTL | Stale Strategy | Requires Network |
|-----------|----------|----------------|-----------------|
| Dashboard KPIs | 5 minutes | Show stale + refresh indicator | No (cached OK) |
| Report data | 15 minutes | Show stale + refresh indicator | No (cached OK) |
| Approval queue | 0 (always fresh) | Must fetch live | Yes |
| Franchise list | 24 hours | Show cached | No (cached OK) |
| Profile data | 1 hour | Show cached | No (cached OK) |

---

## Key Design Constraints

1. **Read-only by design** -- No invoice creation, no stock management, no distributor onboarding
2. **Approval is the only write** -- approve/reject with mandatory comments; requires network
3. **Multi-franchise context** -- Every API call includes `franchise_id` from current selection
4. **JWT authentication** -- Shared JWT infrastructure with distributor app
5. **Report pagination** -- Large reports (1000+ rows) must use server-side pagination
6. **Chart rendering** -- Use MPAndroidChart or Compose-based charting for inline visualizations
7. **Existing API reuse** -- 9 endpoints in `api/owners/` are already available

---

## How to Read This SRS

1. **Start with this index** to understand scope and key constraints
2. **Read `srs/01-functional-requirements.md`** for detailed module-by-module specifications
3. **Read `srs/02-nonfunctional-data-models.md`** for performance, security, and Room entities
4. **Read `srs/03-error-handling-traceability.md`** for error handling and requirement tracing
5. **Proceed to [03_SDS.md](03_SDS.md)** for architecture and design decisions

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial SRS index |
