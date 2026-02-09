# PRD: User Stories and MVP Scope

**Parent:** [01_PRD.md](../01_PRD.md)
**Version:** 1.0
**Last Updated:** 2026-02-08

---

## 1. User Stories by Module

Priority key: **P0** = MVP must-have | **P1** = v1.1 fast-follow | **P2** = v2.0 future

### 1.1 Authentication

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-AUTH-001 | As an owner, I want to log in with my email and password so I can access my franchise data | P0 | JWT token returned and stored in EncryptedSharedPreferences; owner role verified; redirected to Dashboard |
| US-AUTH-002 | As an owner, I want to use biometric login (fingerprint/face) so I can access the app quickly | P1 | BiometricPrompt integration; fallback to PIN; opt-in from Profile settings |
| US-AUTH-003 | As an owner, I want to stay logged in for 30 days so I don't re-enter credentials | P0 | Token auto-refreshed if valid; session persists for 30 days; re-login only on token expiry or explicit logout |
| US-AUTH-004 | As an owner, I want to reset my password via email so I can recover access | P1 | Calls `/api/auth/request-password-reset.php`; shows confirmation message with email sent indicator |
| US-AUTH-005 | As an owner, I want to log out so I can secure my device | P0 | Clears JWT, cached data, and EncryptedSharedPreferences; returns to login screen |

### 1.2 Dashboard

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-DASH-001 | As an owner, I want to see KPI cards (Sales MTD, Cash Balance, Inventory Value, Total BV, Pending Approvals) | P0 | 5 KPI cards from `/api/owners/dashboard-stats.php`; values formatted in franchise currency |
| US-DASH-002 | As an owner, I want to tap a KPI card to drill into the corresponding report | P0 | Sales MTD -> Sales Summary; Cash Balance -> Cash Flow; Inventory Value -> Inventory Valuation; Total BV -> Distributor Performance; Pending Approvals -> Approval Queue |
| US-DASH-003 | As an owner, I want quick report buttons (Daily Sales, Cash Flow, P&L, Inventory, Distributor Perf, Sales Summary) | P0 | 6 quick action buttons matching web portal; each navigates to the respective report screen |
| US-DASH-004 | As an owner, I want an approval summary card showing counts by type (Expenses, POs, Stock Transfers, Stock Adjustments, Leave) | P0 | Badge counts per approval type; tap opens Approvals screen filtered to that type |
| US-DASH-005 | As an owner, I want to see last month's comparison trend on each KPI card | P1 | Small trend indicator (up/down arrow with percentage) showing month-over-month change |
| US-DASH-006 | As an owner, I want to see a welcome card with my name, photo, and total franchise count | P0 | Owner profile from dashboard-stats API; avatar image; franchise count badge |

### 1.3 Franchise Switcher

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-FRAN-001 | As an owner, I want to see a list of my franchises with name, country, and currency | P0 | List from `/api/owners/franchises.php?action=list`; each item shows franchise name, country flag, currency code |
| US-FRAN-002 | As an owner, I want to switch active franchise and see all data update immediately | P0 | POST to `/api/owners/franchises.php?action=switch`; all dashboard KPIs, reports, and approvals refresh |
| US-FRAN-003 | As an owner, I want the currently active franchise displayed prominently in the top bar | P0 | Franchise name and currency visible in app header; tap to open switcher |
| US-FRAN-004 | As an owner, I want the app to default to my primary franchise on launch | P0 | Last-used franchise persisted in local storage; restored on cold start |
| US-FRAN-005 | As an owner, I want to see a franchise count badge in navigation | P1 | Badge showing total franchise count on the switcher icon in the top bar |

### 1.4 Sales Reports (7 Reports)

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-SALE-001 | As an owner, I want to view the Sales Summary report with totals by period | P0 | Table from sales-summary API; columns: period, total sales, total BV, invoice count; date range filter |
| US-SALE-002 | As an owner, I want to view the Daily Sales report for a selected date | P0 | Data from `/api/owners/daily-sales.php`; shows invoices, amounts, DPC breakdown for chosen date |
| US-SALE-003 | As an owner, I want to see Sales Trends as a chart over time | P1 | ApexCharts-style line/bar chart showing daily or monthly sales trend; date range picker |
| US-SALE-004 | As an owner, I want to view Sales by Product breakdown | P1 | Table showing product name, qty sold, revenue, BV; sortable columns; date range filter |
| US-SALE-005 | As an owner, I want to see Product Performance metrics | P1 | Report showing top/bottom performing products by revenue and BV; sparkline trend per product |
| US-SALE-006 | As an owner, I want to view Top Sellers (distributors) ranked by sales volume | P1 | Ranked list with distributor name, code, total sales, total BV; date range filter |
| US-SALE-007 | As an owner, I want to view the Commission Report for a period | P0 | Commission data showing distributor, earned, paid, balance; filter by encoding period |

### 1.5 Finance Reports (8 Reports)

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-FIN-001 | As an owner, I want to view the Profit & Loss statement | P0 | P&L from profit-loss API; revenue, COGS, gross profit, expenses, net profit; date range filter |
| US-FIN-002 | As an owner, I want to view the Cash Flow report | P0 | Cash flow from `/api/owners/cash-flow.php`; inflows, outflows, net cash flow by category |
| US-FIN-003 | As an owner, I want to view the Balance Sheet | P1 | Assets (NBV, inventory at local price, receivables, cash/bank), liabilities, equity; as-of date |
| US-FIN-004 | As an owner, I want to view Account Reconciliation | P1 | Account balances with reconciliation status; shows matched and unmatched transactions |
| US-FIN-005 | As an owner, I want to view the Expense Report | P0 | Expense breakdown by category with totals; date range filter; status filter (approved, pending, rejected) |
| US-FIN-006 | As an owner, I want to view the Tax Report | P2 | Tax liabilities by rate and period; summary totals |
| US-FIN-007 | As an owner, I want to view the Debtors List | P1 | Distributors and staff with outstanding balances; sorted by amount; aging buckets |
| US-FIN-008 | As an owner, I want to view Staff Credit Issuance report | P1 | Staff credit issuances with amounts, dates, and repayment status |

### 1.6 Inventory Reports (3 Reports)

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-INV-001 | As an owner, I want to view Inventory Valuation | P0 | Data from `/api/owners/inventory-valuation.php`; product name, qty, local price, total value; grand total |
| US-INV-002 | As an owner, I want to view the Stock Adjustment Log | P1 | Adjustment records with date, product, qty, type (increase/decrease), reason, user |
| US-INV-003 | As an owner, I want to view the Stock Transfer Log | P1 | Transfer records with date, product, qty, from-warehouse, to-warehouse, status |

### 1.7 HR/Payroll Reports (3 Reports)

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-HR-001 | As an owner, I want to view Payroll Reports | P1 | Payroll summary by period; total gross, deductions, net pay; per-employee breakdown |
| US-HR-002 | As an owner, I want to view Salary Reports | P2 | Salary structure per employee; basic pay, allowances, deductions |
| US-HR-003 | As an owner, I want to view Leave Summary | P1 | Leave balances and usage by employee; leave type breakdown; pending requests |

### 1.8 Distributor Reports (5 Reports)

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-DIST-001 | As an owner, I want to view Distributor Performance | P0 | Data from `/api/owners/distributor-performance.php`; name, code, total sales, BV, rank; date filter |
| US-DIST-002 | As an owner, I want to view the Genealogy tree for any distributor | P1 | Tree from genealogy API; expandable nodes; shows distributor name, code, rank, BV per node |
| US-DIST-003 | As an owner, I want to view Manager Legs report | P0 | Data from `/api/owners/manager-legs.php`; leg-wise BV breakdown; top performers per leg |
| US-DIST-004 | As an owner, I want to view the Rank Report | P1 | Distribution of distributors by rank; count per rank tier; chart visualization |
| US-DIST-005 | As an owner, I want to view the Distributor Directory | P1 | Searchable list with name, code, phone, rank, join date; pagination |

### 1.9 Compliance Reports (2 Reports)

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-COMP-001 | As an owner, I want to view User Activity report | P1 | Data from `/api/owners/user-activity.php`; user actions with timestamps, IP, page visited |
| US-COMP-002 | As an owner, I want to view Client Map showing distributor client locations | P2 | Map view using OSM/Mapbox; client pins grouped by distributor; cluster markers |

### 1.10 Approval Workflows (7 Workflows)

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-APPR-001 | As an owner, I want to approve/reject pending Expense requests | P0 | List of pending expenses; tap to view detail (amount, category, requestor, receipt); approve/reject buttons with optional comment |
| US-APPR-002 | As an owner, I want to approve/reject Purchase Orders | P1 | List of pending POs; detail shows supplier, items, total; approve/reject with comment |
| US-APPR-003 | As an owner, I want to approve/reject Stock Transfer requests | P1 | Pending transfers with from/to warehouse, products, quantities; approve/reject |
| US-APPR-004 | As an owner, I want to approve/reject Stock Adjustment requests | P1 | Pending adjustments with product, qty change, reason; approve/reject |
| US-APPR-005 | As an owner, I want to approve/reject Payroll submissions | P2 | Payroll summary with total amount, employee count; approve/reject |
| US-APPR-006 | As an owner, I want to approve/reject Leave Requests | P1 | Leave details with employee, type, dates, duration; approve/reject |
| US-APPR-007 | As an owner, I want to approve/reject Asset Depreciation schedules | P2 | Depreciation schedule with asset, method, amount; approve/reject |

### 1.11 Profile

| ID | Story | Priority | Acceptance Criteria |
|----|-------|----------|-------------------|
| US-PROF-001 | As an owner, I want to view my profile with name, email, phone, and photo | P0 | Profile screen from `/api/owners/profile.php`; displays owner details and franchise list |
| US-PROF-002 | As an owner, I want to edit my contact information (phone, email) | P0 | Editable fields; PUT request to profile API; validation on phone format and email |
| US-PROF-003 | As an owner, I want to change my profile photo | P1 | Camera/gallery picker; upload to profile API; image compressed before upload |

---

## 2. MVP Scope and Release Roadmap

### v1.0 -- MVP (Months 1-4)

All P0 user stories. Core owner experience for daily operations.

**Modules:** Auth (login, logout, persistent session), Dashboard (5 KPI cards, quick reports, approval summary), Franchise Switcher, 6 Key Reports (Daily Sales, Sales Summary, P&L, Cash Flow, Expense Report, Inventory Valuation), Distributor Performance, Manager Legs, Commission Report, Expense Approval Workflow, Profile (view and edit)

**Key capabilities:**
- JWT authentication with owner role validation
- Multi-franchise switching with instant data refresh
- Executive KPI dashboard with 5 metric cards
- 9 reports across sales, finance, inventory, and distributors
- Expense approval workflow (approve/reject with comments)
- Profile viewing and editing

### v1.1 -- Fast Follow (Months 5-6)

All P1 user stories. Full report coverage and remaining approvals.

**Additions:**
- Biometric login and password reset
- All 23 reports across 6 categories
- 6 remaining approval workflows (PO, Stock Transfer, Stock Adjustment, Leave, plus enhanced)
- Push notifications for pending approvals and KPI alerts
- Month-over-month KPI trend indicators
- Franchise count badge
- Genealogy tree visualization
- Profile photo upload
- Sales Trends chart, Sales by Product, Product Performance, Top Sellers
- Balance Sheet, Account Reconciliation, Debtors List, Staff Credit Issuance
- Stock Adjustment Log, Stock Transfer Log
- Payroll Reports, Leave Summary
- Rank Report, Distributor Directory
- User Activity report

### v2.0 -- Advanced (Months 7-10)

All P2 user stories. Offline, analytics, and advanced features.

**Additions:**
- Offline report caching (previously viewed reports available without connection)
- PDF export of reports
- Tax Report
- Salary Reports
- Client Map with GIS visualization
- Payroll and Asset Depreciation approval workflows
- Cross-franchise comparison dashboard
- Scheduled report delivery (push notification with summary)
- In-app update prompts

---

## 3. Feature Dependency Graph

```
                    [Authentication]
                          |
                  [Franchise Switcher]
                          |
                    [Dashboard]
                   /     |     \
                  /      |      \
          [Reports]  [Approvals]  [Profile]
             |
    -------------------------
    |    |    |    |    |   |
  Sales Fin  Inv  HR  Dist Comp

  Dependency rules:
  Auth ---------> ALL modules (required for every screen)
  Franchise ----> ALL data screens (franchise_id scoping)
  Dashboard ----> reads KPI data from Sales, Finance, Inventory APIs
  Dashboard ----> reads approval counts from Approval APIs
  Reports ------> each report is independent; all require franchise_id
  Approvals ----> Expense approval depends on Expense Report data
  Profile ------> independent; owner data from profile API
```

---

## 4. Module Unlock Model

**All modules are available to all authenticated owners.** There is no subscription gating, tiering, or module unlocking.

Access is controlled by:

| Control | Mechanism | Example |
|---------|-----------|---------|
| **Owner Role** | `user_type = 'owner'` in JWT claims | Only owners access `/api/owners/*` endpoints |
| **Franchise Ownership** | `tbl_franchise_owners` join table | Owner sees only franchises they own |
| **Read-Only** | No write APIs for reports | Owners view reports but cannot modify underlying data |
| **Approval Write** | Specific approval endpoints | Owners can only approve/reject; cannot create requests |
| **Feature Flags** | Server-side flags for unreleased features | Tax Report shows "Coming Soon" until P2 release |

No in-app purchases, no premium tiers, no module locks. Every authenticated franchise owner gets the full app experience based on their franchise ownership.

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Claude Code | Initial creation |

---

**Line count: ~195**
