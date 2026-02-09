# Phase 2+ Roadmap -- Owner Portal Android App

**[Back to Main README](./README.md)** | **Package:** `com.dynapharm.owner`

---

## 1. Overview

This document outlines everything planned after Phase 1 (Auth + Dashboard). Phase 1 establishes the foundation: project scaffold, Hilt DI, Retrofit networking with JWT auth, Room caching, Login screen, and the Executive Dashboard with 5 KPI cards. All subsequent phases build on this foundation.

---

## 2. Phase 2a: Reports Tab

**Goal:** Replace the "Coming Soon" placeholder on the Reports bottom tab with a full report browser.

### 2a.1 Report Catalog

The DMS web system provides 23 reports across 6 categories. The MVP v1.0 delivers the top 6 most-used reports.

| Category | Report | MVP v1.0? | API Endpoint | Status |
|----------|--------|-----------|-------------|--------|
| **Sales** | Daily Sales | Yes | `api/owners/daily-sales.php` | Ready |
| Sales | Sales Summary | Yes | `api/owners/sales-summary.php` | Needs API |
| Sales | Sales Trends | No | -- | Planned |
| Sales | Sales by Product | No | -- | Planned |
| Sales | Top Sellers | No | -- | Planned |
| Sales | Product Performance | No | -- | Planned |
| Sales | Commission Report | No | -- | Planned |
| **Finance** | Profit & Loss | Yes | `api/owners/profit-loss.php` | Needs API |
| Finance | Cash Flow | Yes | `api/owners/cash-flow.php` | Ready |
| Finance | Balance Sheet | No | -- | Planned |
| Finance | Expense Report | Yes | `api/owners/expense-report.php` | Needs API |
| Finance | Account Reconciliation | No | -- | Planned |
| Finance | Employee Debts | No | -- | Planned |
| Finance | Debtors | No | -- | Planned |
| Finance | Inventory Valuation | No | `api/owners/inventory-valuation.php` | Ready |
| **Inventory** | Stock Transfer Log | No | -- | Planned |
| Inventory | Stock Adjustment Log | No | -- | Planned |
| Inventory | Inventory Valuation | No | (shared with Finance) | Ready |
| **HR/Payroll** | Payroll Summary | No | -- | Needs API |
| HR/Payroll | Leave Report | No | -- | Needs API |
| HR/Payroll | User Activity | No | `api/owners/user-activity.php` | Ready |
| **Distributors** | Distributor Performance | Yes | `api/owners/distributor-performance.php` | Ready |
| Distributors | Directory | No | -- | Needs API |
| Distributors | Genealogy | No | -- | Needs API |
| Distributors | Manager Legs | No | `api/owners/manager-legs.php` | Ready |
| Distributors | Rank Report | No | -- | Needs API |
| **Compliance** | Client Map Report | No | -- | Needs API |
| Compliance | User Activity Report | No | `api/owners/user-activity.php` | Ready |

### 2a.2 MVP v1.0 Reports (6 Reports)

1. **Daily Sales** -- Sales breakdown by day with date range filter
2. **Sales Summary** -- Aggregated sales metrics by period
3. **Profit & Loss** -- Revenue vs expenses with net profit calculation
4. **Cash Flow** -- Cash inflows and outflows by category
5. **Distributor Performance** -- BV, sales, and recruitment metrics per distributor
6. **Expense Report** -- Expenses by category with approval status

### 2a.3 Report Filtering

Every report screen includes:

- **Date range picker** -- Material 3 DateRangePicker, defaults to current month
- **Branch filter** -- Dropdown of franchise branches (if multi-branch)
- **Distributor filter** -- Searchable dropdown (for performance/BV reports)
- **Period granularity** -- Daily / Weekly / Monthly toggle (where applicable)

Filters persist in ViewModel state and survive configuration changes.

### 2a.4 Report Caching

- Room table: `tbl_report_cache` with columns: `report_type`, `franchise_id`, `filter_hash`, `json_data`, `fetched_at`
- TTL: 30 minutes (configurable per report type)
- Cache key: SHA-256 hash of `report_type + franchise_id + filters`
- Stale-while-revalidate: show cached data immediately, refresh in background
- Manual cache clear via pull-to-refresh

### 2a.5 PDF Export

- Generate report as PDF using Android's `PrintManager` or a lightweight PDF library
- Share via Android share intent (email, WhatsApp, file manager)
- PDF header includes: franchise name, report title, date range, generation timestamp
- Export button in the top app bar of each report screen

---

## 3. Phase 2b: Approvals Tab

**Goal:** Replace the "Coming Soon" placeholder on the Approvals bottom tab with approval workflows.

### 3b.1 Approval Workflows

| # | Workflow | MVP v1.0? | Web Page Reference | API Status |
|---|---------|-----------|-------------------|------------|
| 1 | Expense Approval | Yes | `expense-approval.php` | Needs API |
| 2 | Purchase Order Approval | No | Planned | Needs API |
| 3 | Stock Transfer Approval | No | Planned | Needs API |
| 4 | Stock Adjustment Approval | No | Planned | Needs API |
| 5 | Payroll Approval | No | Planned | Needs API |
| 6 | Leave Approval | No | Planned | Needs API |
| 7 | Asset Depreciation Approval | No | Planned | Needs API |

### 3b.2 MVP: Expense Approval

**Screen flow:**

1. **Approval Queue** -- List of pending expense requests
   - Filter by status: Pending / Approved / Rejected / All
   - Each card shows: requester name, amount, category, date, urgency badge
   - Badge count on the Approvals tab icon

2. **Approval Detail** -- Single expense request
   - Full expense details: description, amount, category, receipt attachments
   - Approve button (green) with optional comment field
   - Reject button (red) with mandatory reason field
   - Confirmation dialog before action

3. **Action result** -- Success/failure feedback via SweetAlert-style dialog

### 3b.3 Push Notifications for Approvals (FCM)

- Firebase Cloud Messaging integration
- Backend sends push when a new approval request is created
- Notification payload includes: `type: "approval"`, `workflow: "expense"`, `id: 123`
- Tapping the notification deep-links to the approval detail screen
- Badge count updates on the Approvals tab

---

## 4. Phase 2c: Franchise Switching

**Goal:** Replace the "Coming Soon" placeholder on the Franchises bottom tab with multi-franchise support.

### 4c.1 Franchise List

- API: `GET api/owners/franchises.php` (existing, ready)
- List all franchises the owner has access to
- Each row: franchise name, location, currency, active badge
- Current active franchise highlighted with checkmark

### 4c.2 Switch Active Franchise

- Tap a franchise to switch context
- Switching triggers:
  1. Update `TokenManager.activeFranchiseId`
  2. Clear all cached dashboard and report data for the old franchise
  3. Reload Dashboard with new franchise context
  4. Update the `X-Franchise-ID` header for all subsequent API calls
- Animated transition during switch

### 4c.3 Cross-Franchise KPI Comparison

- Summary card at the top of franchise list showing aggregate KPIs across all franchises
- Expandable comparison table: Sales MTD, Cash Balance, Inventory Value per franchise
- Visual indicators (green/red arrows) for month-over-month changes

### 4c.4 TopBar Franchise Badge

- Active franchise name displayed in the top app bar (all screens)
- Tapping the badge opens franchise switcher bottom sheet (quick access)
- Badge color matches franchise brand color (if configured)

---

## 5. Phase 2d: More Tab Features

**Goal:** Fill the "More" tab with profile management and app settings.

### 5d.1 Profile Management

- **View profile:** Owner name, email, phone, photo, associated franchises
- **Edit profile:** Update phone, address, photo
- **Photo upload:** Camera capture or gallery, client-side compression (max 512 KB)
- API: `GET/PUT api/owners/profile.php` (existing, ready)

### 5d.2 Settings

| Setting | Type | Storage |
|---------|------|---------|
| Language | Dropdown (en, fr, ar, sw, es) | DataStore |
| Theme | Toggle (Light / Dark / System) | DataStore |
| Notifications | Switch (enable/disable) | DataStore + FCM topic |
| About | Read-only | BuildConfig |
| Clear cache | Button | Room `deleteAll()` |
| Logout | Button | TokenManager `clearAll()` |

### 5d.3 Biometric Login

- Enable/disable biometric unlock in Settings
- On app open (if enabled): BiometricPrompt before showing Dashboard
- Falls back to PIN/pattern if biometric unavailable
- Tokens remain encrypted; biometric only unlocks the decryption key

---

## 6. Phase 3: Offline and Intelligence

### 6.1 Report Caching for Offline Viewing

- Extend Room caching to store full report JSON blobs
- Reports marked as "saved for offline" persist beyond the 30-min TTL
- Offline indicator banner on cached report screens
- Storage management: show cache size in Settings, allow selective clearing

### 6.2 Background Sync with WorkManager

```kotlin
class SyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. Refresh dashboard KPIs
        // 2. Sync pending approval actions
        // 3. Refresh cached reports nearing TTL
        // 4. Update franchise list
        return Result.success()
    }
}
```

- Periodic sync: every 15 minutes when on WiFi, every 30 minutes on cellular
- Constraints: requires network, not low battery
- Expedited work for pending approval actions when connectivity restored

### 6.3 Dashboard Trend Analysis

- Sparkline charts on KPI cards showing 7-day or 30-day trends
- Color-coded: green for upward trend, red for downward
- Uses ApexCharts-equivalent Android charting library (MPAndroidChart or Vico)
- Data source: historical KPI snapshots stored in Room

### 6.4 Anomaly Detection Highlights

- Backend calculates anomalies (e.g., sales drop > 20% from average)
- Dashboard cards show warning icon for anomalous KPIs
- Tapping the warning shows explanation: "Sales are 25% below the 30-day average"
- Push notification for critical anomalies (optional, configurable)

### 6.5 Home Screen Widget

- Glance widget (Jetpack Glance) showing top 3 KPIs
- Auto-refreshes on widget update interval (30 min minimum)
- Tapping the widget opens the app to Dashboard
- Supports both 2x2 (compact) and 4x2 (expanded) sizes

---

## 7. Phase 4: Advanced Features

### 7.1 Push Notifications for KPI Alerts

- Owner configures alert thresholds (e.g., "Notify me if daily sales < 10,000")
- Backend evaluates thresholds on schedule (cron) and sends FCM push
- Notification categories: Sales, Finance, Inventory, Approvals, System
- Per-category enable/disable in Settings

### 7.2 Tablet Adaptive Layout

- `WindowSizeClass.Expanded`: permanent navigation rail + two-pane layout
- Reports tab: report list on left, selected report on right
- Approvals tab: queue on left, detail on right
- Dashboard: wider KPI cards in a 3-column grid

### 7.3 Certificate Pinning

- Production build pins the TLS certificate of `app.dynapharm-dms.com`
- Configured via `OkHttp.CertificatePinner` in NetworkModule
- Pins rotated quarterly; new pins shipped via app update
- Staging build uses separate pins; debug build disables pinning

### 7.4 Root Detection

- Check for root indicators on app launch (su binary, Magisk, etc.)
- If detected: show warning dialog, allow continue but log to analytics
- Do NOT block usage entirely (some legitimate users have rooted devices)
- Report root status to backend via `X-Device-Integrity` header

### 7.5 Deep Link Support

- URL scheme: `dynapharm-owner://`
- Supported deep links:

| Deep Link | Screen |
|-----------|--------|
| `dynapharm-owner://dashboard` | Dashboard |
| `dynapharm-owner://reports/daily-sales` | Daily Sales Report |
| `dynapharm-owner://approvals/123` | Approval Detail #123 |
| `dynapharm-owner://franchise/switch/5` | Switch to Franchise #5 |

- Used by push notifications and email links
- Requires authentication; unauthenticated deep links redirect to Login first

---

## 8. Dependencies on Phase 1

Phase 1 provides the foundation that all subsequent phases reuse:

| Phase 1 Component | Used By | How |
|-------------------|---------|-----|
| **Hilt DI (NetworkModule, DatabaseModule)** | All phases | Every new feature injects via Hilt |
| **Retrofit + OkHttp stack** | Reports, Approvals, Franchise APIs | New API services added to NetworkModule |
| **AuthInterceptor** | All API calls | Automatically attaches Bearer token |
| **TokenRefreshAuthenticator** | All API calls | Transparently refreshes expired tokens |
| **TokenManager** | Franchise switching, biometric | Stores active franchise ID, biometric key |
| **Room database** | Report cache, approval cache | New DAOs and entities added to database |
| **Result sealed class** | All ViewModels | Unified loading/success/error pattern |
| **safeApiCall helper** | All repositories | Standardized error handling |
| **NetworkMonitor** | Offline mode, sync worker | Connectivity-aware caching decisions |
| **DynapharmTheme** | All screens | Consistent Material 3 styling |
| **Bottom navigation shell** | Tab content replacement | Swap "Coming Soon" with real screens |
| **Login flow** | Session management | Auto-login, re-auth on token expiry |
| **Dashboard caching pattern** | Report caching | Same Room + TTL + stale-while-revalidate |

---

## 9. Feature to Phase Mapping

| Feature | Phase | Tab | Primary API Endpoints | Status |
|---------|-------|-----|----------------------|--------|
| Auth (Login/Logout) | 1 | -- | `mobile-login`, `mobile-refresh`, `mobile-logout` | Done |
| Executive Dashboard | 1 | Dashboard | `dashboard-stats` | Done |
| Daily Sales Report | 2a | Reports | `daily-sales` | Planned |
| Sales Summary Report | 2a | Reports | `sales-summary` (needs API) | Planned |
| Profit & Loss Report | 2a | Reports | `profit-loss` (needs API) | Planned |
| Cash Flow Report | 2a | Reports | `cash-flow` | Planned |
| Distributor Performance | 2a | Reports | `distributor-performance` | Planned |
| Expense Report | 2a | Reports | `expense-report` (needs API) | Planned |
| Report Filtering | 2a | Reports | (client-side, uses existing endpoints) | Planned |
| Report PDF Export | 2a | Reports | (client-side generation) | Planned |
| Expense Approval | 2b | Approvals | `expense-approval` (needs API) | Planned |
| Approval Push Notifications | 2b | Approvals | FCM + backend trigger | Planned |
| Franchise List | 2c | Franchises | `franchises` | Planned |
| Franchise Switching | 2c | Franchises | `franchises`, `dashboard-stats` | Planned |
| Cross-Franchise KPI | 2c | Franchises | `dashboard-stats` (per franchise) | Planned |
| Profile Management | 2d | More | `profile` | Planned |
| Settings (Language/Theme) | 2d | More | (client-side DataStore) | Planned |
| Biometric Login | 2d | More | (client-side BiometricPrompt) | Planned |
| Offline Report Cache | 3 | Reports | (Room persistence layer) | Planned |
| Background Sync | 3 | -- | WorkManager + existing APIs | Planned |
| Dashboard Trends | 3 | Dashboard | `dashboard-stats` (historical) | Planned |
| Anomaly Detection | 3 | Dashboard | `dashboard-stats` + backend logic | Planned |
| Home Screen Widget | 3 | -- | Room cache (no direct API call) | Planned |
| KPI Alert Notifications | 4 | -- | FCM + backend cron | Planned |
| Tablet Adaptive Layout | 4 | All | (client-side WindowSizeClass) | Planned |
| Certificate Pinning | 4 | -- | (OkHttp configuration) | Planned |
| Root Detection | 4 | -- | (client-side check) | Planned |
| Deep Link Support | 4 | -- | (Navigation Compose deep links) | Planned |
| All 23 Reports | v1.1 | Reports | Multiple new APIs needed | Planned |
| All 7 Approval Workflows | v1.1 | Approvals | Multiple new APIs needed | Planned |

---

## 10. API Readiness Summary

| Status | Count | Details |
|--------|-------|---------|
| Ready (existing) | 9 | dashboard-stats, franchises, daily-sales, cash-flow, inventory-valuation, distributor-performance, manager-legs, profile, user-activity |
| Needs API | 14+ | sales-summary, profit-loss, expense-report, expense-approval, directory, genealogy, rank-report, stock-transfer-log, stock-adjustment-log, payroll-summary, leave-report, client-map, PO-approval, leave-approval |
| JWT Auth | 3 | mobile-login, mobile-refresh, mobile-logout (Phase 0/1) |

---

## 11. Release Timeline (Estimated)

| Phase | Target | Key Deliverable |
|-------|--------|----------------|
| Phase 1 | Complete | Auth + Dashboard + infrastructure |
| Phase 2a | +3 weeks | 6 MVP reports with filtering and caching |
| Phase 2b | +2 weeks | Expense approval workflow + FCM |
| Phase 2c | +1 week | Franchise switching + cross-franchise KPIs |
| Phase 2d | +2 weeks | Profile, settings, biometric login |
| **v1.0 Release** | **Phase 2d complete** | **Play Store internal testing** |
| Phase 3 | +4 weeks | Offline mode, sync, trends, widget |
| Phase 4 | +3 weeks | Alerts, tablet layout, security hardening |
| **v1.1 Release** | **Phase 4 complete** | **All 23 reports, all 7 approvals** |

---

## 12. Cross-References

| Topic | Document |
|-------|----------|
| Phase 1 testing | [phase-1/09-testing.md](phase-1/09-testing.md) |
| Phase 1 verification | [phase-1/10-verification.md](phase-1/10-verification.md) |
| Core infrastructure | [phase-1/03-core-infrastructure.md](phase-1/03-core-infrastructure.md) |
| API contract overview | [api-contract/01-overview.md](api-contract/01-overview.md) |
| Auth API endpoints | [api-contract/02-endpoints-auth.md](api-contract/02-endpoints-auth.md) |
| Dashboard API endpoint | [api-contract/03-endpoints-dashboard-kpi.md](api-contract/03-endpoints-dashboard-kpi.md) |
| Report API endpoints | [api-contract/04-endpoints-reports.md](api-contract/04-endpoints-reports.md) |
| Approval API endpoints | [api-contract/05-endpoints-approvals.md](api-contract/05-endpoints-approvals.md) |
| SDS architecture | [sds/01-architecture.md](sds/01-architecture.md) |
| Offline sync design | [sds/04-offline-sync.md](sds/04-offline-sync.md) |
| Main README | [README.md](README.md) |
