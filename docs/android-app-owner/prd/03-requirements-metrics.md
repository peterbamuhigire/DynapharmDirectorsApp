# PRD: Requirements, Metrics, and Risks

**Parent:** [01_PRD.md](../01_PRD.md)
**Version:** 1.0
**Last Updated:** 2026-02-08

---

## 1. Success Metrics

### Primary Metrics

| Metric | Target (6 months post-launch) | Measurement |
|--------|-------------------------------|-------------|
| DAU (Daily Active Users) | 60% of active franchise owners | Firebase Analytics |
| D7 Retention | >= 70% | Firebase Analytics (cohort) |
| D30 Retention | >= 50% | Firebase Analytics (cohort) |
| Approval response time | < 2 hours (from push notification to action) | Backend timestamp delta: notification_sent_at to action_at |
| Report load time (p95) | < 3 seconds | Firebase Performance Monitoring |
| Dashboard load time (p95) | < 2 seconds | Firebase Performance Monitoring |
| Crash-free rate | >= 99.5% | Firebase Crashlytics |
| ANR (App Not Responding) rate | < 0.5% | Google Play Console vitals |
| Play Store rating | >= 4.2 stars | Google Play Console |
| API latency (p50) | < 500ms | Custom backend logging with response_time header |
| API latency (p95) | < 2000ms | Custom backend logging |
| App startup time (cold) | < 3 seconds on mid-range device | Firebase Performance |
| App startup time (warm) | < 1 second | Firebase Performance |

### Secondary Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Average session duration | >= 4 minutes | Firebase Analytics |
| Screens per session | >= 6 | Firebase Analytics |
| Approval completion rate | 90% of notifications actioned within 24 hours | Backend analytics |
| Report feature adoption | 70% of MAU view at least 3 different reports weekly | Custom event tracking |
| Franchise switching frequency | Multi-franchise owners switch at least 2x per session | Custom event tracking |
| Push notification open rate | >= 40% | FCM analytics |
| Offline usage sessions | 10% of total sessions include offline report views | Connectivity state logging |
| KPI card tap-through rate | >= 50% of sessions include a KPI drill-down | Custom event tracking |

---

## 2. App Store Strategy

### Play Store Listing

| Field | Content |
|-------|---------|
| **App Name** | Dynapharm Owner Hub |
| **Short Description** (80 chars) | Strategic franchise dashboard, reports & approvals for Dynapharm owners |
| **Developer Name** | Dynapharm International |
| **Category** | Business |
| **Content Rating** | Everyone |
| **Target Age** | 18+ (business professionals) |
| **Countries** | Uganda, Kenya, Tanzania, Ghana, Nigeria, DRC, Morocco, Ivory Coast (initial); global later |

### Long Description (4000 chars max)

```
Dynapharm Owner Hub is the official mobile command center for Dynapharm
franchise owners. Monitor your franchise performance, review and approve
requests, and access 23 strategic reports -- all from your Android phone
or tablet.

EXECUTIVE DASHBOARD
- Real-time KPI cards: Sales MTD, Cash Balance, Inventory Value, Total BV,
  Pending Approvals
- Month-over-month trend indicators on every metric
- Quick report buttons for instant access to key reports
- Approval summary with pending counts by category

MULTI-FRANCHISE MANAGEMENT
- Switch between franchises with a single tap
- Each franchise shows its own currency, country, and performance data
- Compare franchise performance without logging out and back in
- Designed for owners managing multiple countries across Africa

23 STRATEGIC REPORTS IN 6 CATEGORIES
- Sales: Summary, Daily Sales, Trends, By Product, Product Performance,
  Top Sellers, Commission Report
- Finance: P&L, Cash Flow, Balance Sheet, Account Reconciliation, Expenses,
  Tax, Debtors List, Staff Credits
- Inventory: Valuation, Stock Adjustment Log, Stock Transfer Log
- HR/Payroll: Payroll Reports, Salary Reports, Leave Summary
- Distributors: Performance, Genealogy, Manager Legs, Rank Report, Directory
- Compliance: User Activity, Client Map

7 APPROVAL WORKFLOWS
- Approve or reject expenses, purchase orders, stock transfers, stock
  adjustments, payroll, leave requests, and asset depreciation
- One-tap approve/reject with optional comments
- Push notifications when new items need your attention

BUILT FOR FRANCHISE OWNERS
- Multi-language: English, French, Arabic (RTL), Swahili, Spanish
- Multi-currency: UGX, KES, TZS, GHS, NGN, USD, MAD, XOF, and more
- Optimized for African mobile networks -- fast on 3G
- Offline access to previously viewed reports
- Lightweight app under 30 MB

Dynapharm Owner Hub -- your franchise, in your pocket.
```

### ASO Keywords

Primary: `dynapharm`, `franchise management`, `owner dashboard`, `business reports`, `MLM management`

Secondary: `franchise owner app`, `business KPI`, `approval workflow`, `sales reports`, `profit loss`, `cash flow`, `inventory valuation`, `distributor performance`, `Dynapharm owner`

Long-tail: `franchise owner mobile dashboard`, `MLM franchise reports`, `multi-franchise business app`, `business approval mobile`, `African franchise management`

### Screenshot Plan (8 Screenshots)

| # | Screen | Focus | Caption |
|---|--------|-------|---------|
| 1 | Login | Clean login form with Dynapharm branding | "Secure login for franchise owners" |
| 2 | Dashboard | 5 KPI cards, quick reports, approval summary | "Executive KPI dashboard at a glance" |
| 3 | Franchise Switcher | List of franchises with country flags and currencies | "Switch franchises with one tap" |
| 4 | Sales Summary Report | Table with sales data, date filters | "23 reports across 6 categories" |
| 5 | Profit & Loss | P&L statement with revenue, expenses, net profit | "Full financial reporting on mobile" |
| 6 | Expense Approval | Pending expense with approve/reject buttons | "Approve requests from anywhere" |
| 7 | Commission Report | Distributor commissions with period filter | "Track distributor commissions" |
| 8 | Profile | Owner profile with photo, name, franchise list | "Manage your profile and settings" |

### Feature Graphic

1024x500 banner showing: Dynapharm logo (left), phone mockup with dashboard screen (center), tagline "Your Franchise, In Your Pocket" (right), African continent silhouette in subtle background gradient.

---

## 3. Risk Register

| # | Risk | Probability | Impact | Mitigation |
|---|------|------------|--------|-----------|
| R1 | **JWT auth integration delay.** Current web system uses session-based PHP auth. JWT layer must be built from scratch for mobile. | High | High | Start JWT implementation in Phase 0; use existing distributor app JWT pattern as template; parallel backend/mobile development |
| R2 | **API endpoint gaps for reports.** Only 9 of 23 reports have dedicated owner API endpoints. Remaining 14 need new APIs. | Medium | High | Audit all owner report pages; prioritize P0 report APIs for MVP; reuse stored procedures from web reports where possible |
| R3 | **Low Android adoption by older owners.** Some franchise owners (Persona 1: James, age 52) may resist switching from desktop web portal. | Medium | Medium | White-glove onboarding with tutorial; push notification value proposition; keep web portal fully functional as fallback |
| R4 | **Offline sync complexity for reports.** Caching report data for offline access requires careful invalidation and storage management. | Low | High | Start with simple last-viewed cache (Room DB); no offline writes in v1.0; clear cache older than 7 days; show "last refreshed" timestamp |
| R5 | **Multi-currency display errors.** Franchises use different currencies (UGX no decimals, KES 2 decimals, MAD, XOF) with different formatting rules. | Medium | Medium | Centralized `CurrencyFormatter` using franchise locale from API; unit tests for all 8+ supported currencies; server sends currency_code with every amount |
| R6 | **Arabic RTL layout bugs.** RTL layout for Arabic UI may break Compose layouts, especially with LTR numbers, currency codes, and distributor codes. | Medium | Medium | Use `CompositionLocalLayoutDirection`; test all 23 report screens in Arabic; bidirectional text handling for mixed content; dedicated QA pass |
| R7 | **Slow report APIs over 3G networks.** Complex reports (P&L, genealogy, commission) may timeout on slow African mobile connections. | High | Medium | Server-side query optimization; paginate large result sets; implement response compression (gzip); show skeleton loading states; progressive data loading |
| R8 | **Play Store review rejection.** Google may flag the app for limited audience (franchise owners only) or business restrictions. | Low | High | Prepare clear app description explaining target audience; provide demo account for reviewers; follow all Play Store policies; test with pre-launch report |
| R9 | **Data isolation breach across franchises.** A bug in franchise_id filtering could expose one franchise's data to another owner. | Low | Critical | Every API call validates franchise ownership via `tbl_franchise_owners`; JWT claims include owner_id (not franchise_id); server-side ownership check on every request; automated security tests; penetration testing before launch |
| R10 | **Push notification delivery in Africa.** FCM delivery rates vary across African carriers; some countries have poor push infrastructure. | High | Medium | Implement FCM with high-priority messages; fallback to in-app polling every 5 minutes; SMS fallback for critical approvals (v2.0); test delivery rates per country |

---

## 4. Assumptions and Constraints

### Assumptions

1. **JWT auth layer will be built.** Mobile authentication requires JWT tokens since PHP session-based auth cannot extend to native mobile. The distributor app JWT pattern will be reused and extended for owner role.
2. **Existing owner API endpoints are stable.** The 9 existing endpoints (`dashboard-stats`, `daily-sales`, `cash-flow`, `distributor-performance`, `franchises`, `inventory-valuation`, `manager-legs`, `profile`, `user-activity`) will remain backward-compatible.
3. **Owner role is established in the database.** `tbl_franchise_owners` and the `user_type = 'owner'` check in `tbl_users` are already implemented and functional.
4. **Franchise switcher persists in session.** The web portal already implements franchise switching via `$_SESSION['franchise_id']`; the mobile API will use JWT claims plus explicit franchise_id parameters.
5. **Internet required for first launch.** Initial login and franchise data sync require connectivity; subsequent usage can leverage cached data for offline report viewing.
6. **Push notification infrastructure available.** Firebase Cloud Messaging (FCM) can be integrated with the PHP backend via HTTP API for server-to-device notifications.
7. **Approval workflows mirror web behavior.** The approve/reject logic and business rules from the web expense-approval page apply identically to mobile.
8. **Owners are read-only for report data.** The mobile app will never create invoices, register distributors, or modify inventory. Write operations are limited to approvals and profile editing.

### Constraints

1. **Android-only.** Phase 1 targets Android. iOS may be considered in Phase 2 using KMP (Kotlin Multiplatform).
2. **No data creation on mobile.** Owners cannot create expenses, POs, invoices, or distributors. Creation stays on the web portal for staff.
3. **APK size under 30 MB.** Smaller than the distributor app target (50 MB) because the owner app has no offline product catalog or image-heavy features.
4. **Minimum Android 10 (API 29).** Covers 95%+ of active Android devices in target African markets.
5. **Backend remains PHP/MySQL.** No backend rewrite; mobile app adapts to existing architecture with new JWT-secured endpoints.
6. **No real-time data streaming.** Reports show point-in-time data; no WebSocket or SSE for live updates. Pull-to-refresh for latest data.
7. **Report APIs paginate at 50 rows.** To maintain 3G performance, no report API returns more than 50 rows per page.
8. **Session-based auth cannot serve mobile.** JWT must be implemented; shared cookie/session approach is not viable for native apps.

---

## 5. Glossary

| Term | Definition |
|------|-----------|
| **BV (Business Volume)** | Point value assigned to each product sale, used to calculate commissions and rank qualifications. |
| **PV (Personal Volume)** | BV accumulated from a distributor's own purchases and direct sales. |
| **Group BV** | Total BV from a distributor's entire downline network, used for bonus calculations. |
| **MTD (Month to Date)** | Cumulative total from the 1st of the current month through today. |
| **KPI (Key Performance Indicator)** | A measurable value demonstrating how effectively the franchise is achieving business objectives. |
| **DPC (Distribution Point Center)** | A physical or virtual sales point where distributors purchase products. Each DPC has its own inventory. |
| **Franchise** | An independent Dynapharm country operation (e.g., Dynapharm Uganda). Each franchise is a separate tenant. |
| **franchise_id** | The database tenant isolation key. Every query must include this filter to prevent cross-tenant data leakage. |
| **Upline** | The distributor who sponsored/recruited a given distributor. Parent node in the genealogy tree. |
| **Downline** | Distributors recruited by a given distributor. Child nodes in the genealogy tree. |
| **Genealogy Tree** | Hierarchical tree showing MLM network relationships, up to 20 levels deep. |
| **Manager Legs** | Direct downline branches of a Manager-rank distributor. Each leg's BV is tracked separately for qualification. |
| **Encoding Period** | Monthly reporting window during which sales and BV count toward commissions. |
| **NBV (Net Book Value)** | Current value of a fixed asset after depreciation. Used in balance sheet calculations. |
| **Paid Receipt** | Record in `tbl_paid_receipts` confirming payment for an invoice. Only receipts with `date_recognised` count toward BV. |
| **Arrears** | Invoices with outstanding balances (unpaid or partially paid). |
| **P&L (Profit & Loss)** | Financial statement showing revenue, cost of goods sold, expenses, and net profit/loss for a period. |
| **Cash Flow** | Report tracking money flowing in (sales, payments) and out (expenses, payroll, purchases) of the franchise. |
| **Tenant Isolation** | Security principle ensuring data from one franchise is never visible to users of another franchise. |
| **JWT (JSON Web Token)** | Stateless authentication token used for mobile API authentication, containing owner_id and role claims. |
| **FCM (Firebase Cloud Messaging)** | Google's push notification service used to deliver real-time alerts to mobile devices. |
| **RTL (Right-to-Left)** | Text direction for Arabic language support. UI layouts must mirror for RTL locales. |
| **ASO (App Store Optimization)** | Techniques to improve app visibility and ranking in the Google Play Store. |

---

## 6. API Endpoint Appendix

### Existing Owner Endpoints (Web Portal)

| Feature | Method | Endpoint | Auth | Status |
|---------|--------|----------|------|--------|
| Dashboard KPIs | GET | `/api/owners/dashboard-stats.php` | Session | Exists -- needs JWT variant |
| Franchise List | GET | `/api/owners/franchises.php?action=list` | Session | Exists -- needs JWT variant |
| Franchise Switch | POST | `/api/owners/franchises.php?action=switch` | Session | Exists -- needs JWT variant |
| Daily Sales | POST | `/api/owners/daily-sales.php` | Session | Exists -- needs JWT variant |
| Cash Flow | GET | `/api/owners/cash-flow.php` | Session | Exists -- needs JWT variant |
| Distributor Performance | GET | `/api/owners/distributor-performance.php` | Session | Exists -- needs JWT variant |
| Manager Legs | GET | `/api/owners/manager-legs.php` | Session | Exists -- needs JWT variant |
| Inventory Valuation | GET | `/api/owners/inventory-valuation.php` | Session | Exists -- needs JWT variant |
| Owner Profile | GET/PUT | `/api/owners/profile.php` | Session | Exists -- needs JWT variant |
| User Activity | GET | `/api/owners/user-activity.php` | Session | Exists -- needs JWT variant |

### New Endpoints Required for Mobile

| Feature | Method | Endpoint (Proposed) | Purpose |
|---------|--------|-------------------|---------|
| JWT Login | POST | `/api/mobile/auth/owner-login.php` | Returns JWT access + refresh tokens for owner role |
| JWT Refresh | POST | `/api/mobile/auth/refresh.php` | Exchanges refresh token for new access token |
| JWT Logout | POST | `/api/mobile/auth/logout.php` | Revokes refresh token on server |
| Register FCM Token | POST | `/api/mobile/auth/fcm-token.php` | Stores FCM push token for approval notifications |
| Sales Summary | GET | `/api/mobile/owners/sales-summary.php` | Sales totals by period for active franchise |
| Sales Trends | GET | `/api/mobile/owners/sales-trends.php` | Daily/monthly sales data for chart rendering |
| Sales by Product | GET | `/api/mobile/owners/sales-by-product.php` | Product-level sales breakdown |
| Product Performance | GET | `/api/mobile/owners/product-performance.php` | Top/bottom products by revenue and BV |
| Top Sellers | GET | `/api/mobile/owners/top-sellers.php` | Ranked distributors by sales volume |
| Commission Report | GET | `/api/mobile/owners/commission-report.php` | Distributor commission data by encoding period |
| P&L Statement | GET | `/api/mobile/owners/profit-loss.php` | Revenue, COGS, expenses, net profit |
| Balance Sheet | GET | `/api/mobile/owners/balance-sheet.php` | Assets, liabilities, equity as-of date |
| Account Reconciliation | GET | `/api/mobile/owners/account-reconciliation.php` | Account balances with reconciliation status |
| Expense Report | GET | `/api/mobile/owners/expense-report.php` | Expense breakdown by category |
| Tax Report | GET | `/api/mobile/owners/tax-report.php` | Tax liabilities by rate and period |
| Debtors List | GET | `/api/mobile/owners/debtors.php` | Outstanding balances by distributor/staff |
| Staff Credits | GET | `/api/mobile/owners/staff-credits.php` | Staff credit issuances and repayments |
| Stock Adjustment Log | GET | `/api/mobile/owners/stock-adjustments.php` | Adjustment records with filters |
| Stock Transfer Log | GET | `/api/mobile/owners/stock-transfers.php` | Transfer records with filters |
| Payroll Report | GET | `/api/mobile/owners/payroll.php` | Payroll summary by period |
| Salary Report | GET | `/api/mobile/owners/salary.php` | Salary structure per employee |
| Leave Summary | GET | `/api/mobile/owners/leave-summary.php` | Leave balances and usage |
| Rank Report | GET | `/api/mobile/owners/rank-report.php` | Distributor distribution by rank |
| Distributor Directory | GET | `/api/mobile/owners/distributor-directory.php` | Searchable distributor list |
| Genealogy | GET | `/api/mobile/owners/genealogy.php` | Tree data for specified distributor |
| Expense Approvals | GET/POST | `/api/mobile/owners/approvals/expenses.php` | List pending, approve, reject |
| PO Approvals | GET/POST | `/api/mobile/owners/approvals/purchase-orders.php` | List pending, approve, reject |
| Stock Transfer Approvals | GET/POST | `/api/mobile/owners/approvals/stock-transfers.php` | List pending, approve, reject |
| Stock Adjustment Approvals | GET/POST | `/api/mobile/owners/approvals/stock-adjustments.php` | List pending, approve, reject |
| Payroll Approvals | GET/POST | `/api/mobile/owners/approvals/payroll.php` | List pending, approve, reject |
| Leave Approvals | GET/POST | `/api/mobile/owners/approvals/leave.php` | List pending, approve, reject |
| Asset Depreciation Approvals | GET/POST | `/api/mobile/owners/approvals/asset-depreciation.php` | List pending, approve, reject |
| App Config | GET | `/api/mobile/config.php` | Feature flags, min app version, maintenance mode |
| Approval Counts | GET | `/api/mobile/owners/approvals/counts.php` | Pending approval counts by type for badge display |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Claude Code | Initial creation |

---

**Line count: ~285**
