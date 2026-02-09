# Dynapharm Owner Portal Android App -- Planning Documentation

> Native Android companion for franchise owners of the Dynapharm DMS (Distributor Management System).
> A **read-only strategic command center** with executive dashboards, 23 reports, 7 approval workflows, and multi-franchise switching.

Status: Planning complete. Development follows the Phase 1 Bootstrap Pattern.

---

## Implementation Plan

### Phase 1: Login + Dashboard + Empty Tabs (START HERE)

> **[`phase-1/README.md`](phase-1/README.md)** -- 11 implementation sections covering JWT auth, dashboard with offline caching, 5-tab navigation with placeholders, and 47+ unit tests. This phase MUST be fully implemented, tested, and verified before any business features.

### Phase 2+: Business Features

> **[`phase-2-roadmap.md`](phase-2-roadmap.md)** -- Reports, approvals, franchise switching, profile management, offline intelligence. Only begins after Phase 1 gate criteria are met.

---

## Planning Documents

| # | Document | Description | Path |
|---|----------|-------------|------|
| 1 | **PRD** | Product vision, personas, user stories, MVP scope, success metrics | [`01_PRD.md`](01_PRD.md) -> [`prd/`](prd/) |
| 2 | **SRS** | Functional and non-functional requirements, data models, error handling | [`02_SRS.md`](02_SRS.md) -> [`srs/`](srs/) |
| 3 | **SDS** | Architecture, Gradle, Hilt, offline caching, security, networking | [`03_SDS.md`](03_SDS.md) -> [`sds/`](sds/) |
| 4 | **API Contract** | Every endpoint with request/response JSON, error codes | [`04_API_CONTRACT.md`](04_API_CONTRACT.md) -> [`api-contract/`](api-contract/) |
| 5 | **User Journeys** | 12 journeys with ASCII flow diagrams and error recovery paths | [`05_USER_JOURNEYS.md`](05_USER_JOURNEYS.md) -> [`journeys/`](journeys/) |
| 6 | **Testing Strategy** | Test pyramid, examples, CI pipeline, performance benchmarks | [`06_TESTING_STRATEGY.md`](06_TESTING_STRATEGY.md) -> [`testing/`](testing/) |
| 7 | **Release Plan** | Play Store, signing, staged rollout, rollback, monitoring | [`07_RELEASE_PLAN.md`](07_RELEASE_PLAN.md) |

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.0+ |
| UI | Jetpack Compose + Material 3 | BOM 2024.06+ |
| Architecture | MVVM + Clean Architecture | -- |
| DI | Dagger Hilt | 2.51+ |
| Networking | Retrofit + OkHttp + Kotlin Serialization | 2.11+ / 4.12+ |
| Local DB | Room | 2.6+ |
| Async | Coroutines + Flow | 1.8+ |
| Background | WorkManager | 2.9+ |
| Navigation | Navigation Compose | 2.7+ |
| Image Loading | Coil | 2.7+ |
| Security | EncryptedSharedPreferences, BiometricPrompt | AndroidX |
| Logging | Timber | 5.0+ |
| Testing | JUnit 5, MockK, Turbine, Compose UI Testing | -- |
| CI/CD | GitHub Actions | -- |

### Backend

| Field | Value |
|-------|-------|
| Product | DMS_web -- Dynapharm Distributor Management System |
| Backend Stack | PHP 8.2+ / MySQL 9.1 |
| API Base URL (Dev) | `http://10.0.2.2/DMS_web/` |
| API Base URL (Prod) | `https://app.dynapharm-dms.com/` |
| Auth Model | JWT (Access 15 min + Refresh 30 days) |
| Multi-tenancy | `franchise_id` in JWT payload, every query scoped |
| Web Owner Panel | `ownerpanel/` (28 pages, 9 API endpoints in `api/owners/`) |

---

## Module Matrix

| Module | MVP v1.0? | Priority | API Ready? | Notes |
|--------|-----------|----------|------------|-------|
| **Auth** (Login/Logout/Biometric) | Yes | P0 | Needs JWT endpoint | Reuse distributor JWT infra |
| **Executive Dashboard** (5 KPIs) | Yes | P0 | Ready (`dashboard-stats`) | Sales MTD, Cash, Inventory, BV, Approvals |
| **Franchise Switcher** | Yes | P0 | Ready (`franchises`) | Owner may own multiple franchises |
| **Sales Reports** (7) | Partial | P0-P1 | Partial | Daily Sales, Sales Summary, Trends, By Product, Top Sellers, Product Performance, Commission |
| **Finance Reports** (8) | Partial | P1 | Partial | P&L, Cash Flow, Balance Sheet, Expense, Account Reconciliation, Employee Debts, Debtors, Inventory Valuation |
| **Inventory Reports** (3) | v1.1 | P1 | Partial | Stock Transfer Log, Stock Adjustment Log, Inventory Valuation |
| **HR/Payroll Reports** (3) | v1.1 | P2 | Needs API | Payroll Summary, Leave Report, User Activity |
| **Distributor Reports** (5) | v1.1 | P1 | Partial | Directory, Performance, Genealogy, Manager Legs, Rank Report |
| **Compliance Reports** (2) | v1.1 | P2 | Needs API | Client Map, User Activity |
| **Approvals** (7 workflows) | Partial | P0-P1 | Partial | Expenses, POs, Stock Transfers, Adjustments, Payroll, Leave, Asset Depreciation |
| **Profile Management** | Yes | P0 | Ready (`profile`) | Edit contact info, upload photo |

### Report Breakdown by Category

| Category | Reports | MVP Count |
|----------|---------|-----------|
| Sales | Daily Sales, Sales Summary, Sales Trends, Sales by Product, Top Sellers, Product Performance, Commission Report | 3 in MVP |
| Finance | Profit & Loss, Cash Flow, Balance Sheet, Expense Report, Account Reconciliation, Employee Debts, Debtors, Inventory Valuation | 2 in MVP |
| Inventory | Stock Transfer Log, Stock Adjustment Log, Inventory Valuation | 0 in MVP |
| HR/Payroll | Payroll Summary, Leave Report, User Activity | 0 in MVP |
| Distributors | Directory, Performance, Genealogy, Manager Legs, Rank Report | 1 in MVP |
| Compliance | Client Map Report, User Activity Report | 0 in MVP |

### Approval Workflows

| Workflow | MVP? | Current Web Page |
|----------|------|-----------------|
| Expense Approval | Yes | `expense-approval.php` |
| Purchase Order Approval | v1.1 | Planned |
| Stock Transfer Approval | v1.1 | Planned |
| Stock Adjustment Approval | v1.1 | Planned |
| Payroll Approval | v1.1 | Planned |
| Leave Approval | v1.1 | Planned |
| Asset Depreciation Approval | v2.0 | Planned |

---

## Release Roadmap

### v1.0 -- MVP

- Auth (JWT login, biometric unlock, session management)
- Executive KPI Dashboard (5 cards + trend sparklines)
- Franchise Switcher (multi-franchise owners)
- Top 6 Reports: Daily Sales, Sales Summary, P&L, Cash Flow, Distributor Performance, Expense Report
- Expense Approval workflow (approve/reject with comments)
- Profile Management (view/edit contact, upload photo)
- 5-language support (en, fr, ar, sw, es)

### v1.1 -- All Reports + Approvals

- All 23 reports across 6 categories
- All 7 approval workflows
- Report filtering (date range, branch, distributor)
- Export report as PDF (share intent)
- Push notification for pending approvals

### v2.0 -- Offline + Intelligence

- Offline report caching (Room DB, stale-while-revalidate)
- Push notifications (FCM) for approvals, KPI alerts, anomalies
- Biometric login (fingerprint/face)
- Dashboard trend analysis and anomaly highlighting
- Widget for KPI summary on home screen

---

## Navigation

1. **Start here**, then read documents in order (1 through 7)
2. Each index file (`01_PRD.md`, `02_SRS.md`, etc.) links to its sub-files
3. Every sub-file links back to its parent index
4. Cross-references between documents use relative paths

## Related Documents

| Document | Location | Description |
|----------|----------|-------------|
| Distributor App Docs | [`../android-app-distributor/`](../android-app-distributor/) | Distributor-facing Android app planning |
| Web App CLAUDE.md | [`../../CLAUDE.md`](../../CLAUDE.md) | DMS_web development standards |
| Skills Repository | [`../../skills/README.md`](../../skills/README.md) | Skills index |
| Owner Panel (Web) | [`../../ownerpanel/`](../../ownerpanel/) | Existing 28-page web owner portal |
| Owner APIs | [`../../api/owners/`](../../api/owners/) | 9 existing owner API endpoints |

---

## Target Personas (Summary)

| Persona | Device | Connectivity | Primary Use |
|---------|--------|-------------|-------------|
| Single-Franchise Owner | Mid-range Android | Reliable 4G/WiFi | Dashboard, reports, expense approvals |
| Multi-Franchise Owner | Flagship Android | Always-on WiFi/5G | Franchise switching, cross-franchise KPIs |
| Regional Director | Android Tablet | Reliable WiFi | All reports, compliance, HR oversight |
| Absentee Owner | Any Android | Variable | Quick KPI checks, approval queue clearing |

## Read-Only Design Philosophy

The Owner Portal app is intentionally **read-heavy, write-light**:

| Action Type | Examples | Frequency |
|-------------|----------|-----------|
| **Read** (95%) | View dashboard, browse reports, check approvals queue | Every session |
| **Approve/Reject** (4%) | Expense approval, PO approval, leave approval | Several per week |
| **Edit** (1%) | Update profile contact info, change photo | Rarely |

This shapes the caching strategy: aggressive read caching with short TTLs, minimal write queues.

## Existing Owner API Endpoints

| # | Endpoint | File | Purpose |
|---|----------|------|---------|
| 1 | `GET /api/owners/dashboard-stats.php` | `dashboard-stats.php` | Executive KPI data |
| 2 | `GET /api/owners/franchises.php` | `franchises.php` | List owner's franchises |
| 3 | `GET /api/owners/daily-sales.php` | `daily-sales.php` | Daily sales data |
| 4 | `GET /api/owners/cash-flow.php` | `cash-flow.php` | Cash flow report data |
| 5 | `GET /api/owners/inventory-valuation.php` | `inventory-valuation.php` | Inventory value data |
| 6 | `GET /api/owners/distributor-performance.php` | `distributor-performance.php` | Distributor metrics |
| 7 | `GET /api/owners/manager-legs.php` | `manager-legs.php` | Manager leg structure |
| 8 | `GET/PUT /api/owners/profile.php` | `profile.php` | Owner profile CRUD |
| 9 | `GET /api/owners/user-activity.php` | `user-activity.php` | User activity logs |

---

*Generated per the `android-saas-planning` skill. Cross-referenced with: `android-development`, `android-tdd`, `android-data-persistence`, `jetpack-compose-ui`, `dual-auth-rbac`, `multi-tenant-saas-architecture`.*
