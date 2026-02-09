# SRS 01 -- Functional Requirements

**Parent:** [02_SRS.md](../02_SRS.md) | [All Docs](../README.md)

---

## 1. Authentication Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-AUTH-001 | App shall authenticate owner via email + password | P0 | `POST /api/auth/mobile-login.php` |
| FR-AUTH-002 | App shall store JWT access token in EncryptedSharedPreferences | P0 | -- |
| FR-AUTH-003 | App shall store JWT refresh token securely | P0 | -- |
| FR-AUTH-004 | App shall auto-refresh expired access tokens transparently | P0 | `POST /api/auth/refresh.php` |
| FR-AUTH-005 | App shall redirect to login screen on refresh token failure | P0 | -- |
| FR-AUTH-006 | App shall support biometric authentication (fingerprint/face) | P1 | -- |
| FR-AUTH-007 | App shall validate owner role from JWT claims before granting access | P0 | -- |
| FR-AUTH-008 | App shall log out and clear all tokens and cached data | P0 | `POST /api/auth/logout.php` |
| FR-AUTH-009 | App shall support password reset via email link | P1 | `POST /api/auth/password-reset.php` |
| FR-AUTH-010 | App shall enforce minimum password requirements (8+ chars, mixed case, digit) | P0 | -- |
| FR-AUTH-011 | App shall display contextual login errors (invalid credentials, network, locked) | P0 | -- |
| FR-AUTH-012 | App shall persist login session for 30 days via refresh token | P0 | -- |
| FR-AUTH-013 | App shall lock account after 5 failed login attempts for 15 minutes | P1 | -- |
| FR-AUTH-014 | App shall provide show/hide password toggle on login screen | P0 | -- |
| FR-AUTH-015 | App shall display app version number on login screen | P2 | -- |
| FR-AUTH-016 | App shall support "remember email" checkbox on login screen | P1 | -- |

## 2. Dashboard Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-DASH-001 | Display Sales MTD KPI card with franchise currency symbol | P0 | `GET /api/owners/dashboard-stats.php` |
| FR-DASH-002 | Display Cash Balance KPI card with franchise currency symbol | P0 | `GET /api/owners/dashboard-stats.php` |
| FR-DASH-003 | Display Inventory Value KPI card with franchise currency symbol | P0 | `GET /api/owners/dashboard-stats.php` |
| FR-DASH-004 | Display Total BV KPI card | P0 | `GET /api/owners/dashboard-stats.php` |
| FR-DASH-005 | Display Pending Approvals count badge on dashboard | P0 | `GET /api/owners/dashboard-stats.php` |
| FR-DASH-006 | Display trend indicator (up/down arrow + percentage) vs last month for each KPI | P1 | `GET /api/owners/dashboard-stats.php` |
| FR-DASH-007 | Display 6 quick report buttons: Daily Sales, Cash Flow, P&L, Inventory, Distributor Perf, Expenses | P0 | -- |
| FR-DASH-008 | Navigate to corresponding report screen on quick button tap | P0 | -- |
| FR-DASH-009 | Display approval summary card with counts per approval type | P0 | `GET /api/owners/dashboard-stats.php` |
| FR-DASH-010 | Navigate to approval queue on approval summary card tap | P0 | -- |
| FR-DASH-011 | Support pull-to-refresh to reload all dashboard data | P0 | -- |
| FR-DASH-012 | Display franchise name and currency code in dashboard header | P0 | -- |

## 3. Franchise Switcher Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-FRAN-001 | List all franchises owned by authenticated user | P0 | `GET /api/owners/franchises.php?action=list` |
| FR-FRAN-002 | Display franchise name, country, and currency for each franchise | P0 | -- |
| FR-FRAN-003 | Switch active franchise context via selection | P0 | `POST /api/owners/franchises.php?action=switch` |
| FR-FRAN-004 | Reload all displayed data (dashboard, reports, approvals) after franchise switch | P0 | -- |
| FR-FRAN-005 | Persist last active franchise ID locally in DataStore | P0 | -- |
| FR-FRAN-006 | Default to primary franchise on first launch | P0 | -- |
| FR-FRAN-007 | Show active franchise name indicator in top app bar | P0 | -- |
| FR-FRAN-008 | Franchise switcher accessible from any screen via top bar dropdown | P0 | -- |

## 4. Sales Reports Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-SALE-001 | Display Sales Summary report with total sales, total BV, invoice count for date range | P0 | `POST /api/owners/sales-summary.php` |
| FR-SALE-002 | Display Daily Sales report with per-day breakdown (date, invoice count, total, BV) | P0 | `POST /api/owners/daily-sales.php` |
| FR-SALE-003 | Display Sales Trends report with line chart (daily/weekly/monthly aggregation) | P1 | `POST /api/owners/sales-trends.php` |
| FR-SALE-004 | Display Sales by Product report with product name, quantity sold, revenue, BV | P0 | `POST /api/owners/sales-by-product.php` |
| FR-SALE-005 | Display Product Performance report with ranking by revenue and BV | P1 | `POST /api/owners/product-performance.php` |
| FR-SALE-006 | Display Top Sellers report listing top distributors by sales volume | P1 | `POST /api/owners/top-sellers.php` |
| FR-SALE-007 | Display Commission Report with distributor, rank, personal BV, group BV, commission amount | P0 | `POST /api/owners/commission-report.php` |
| FR-SALE-008 | All sales reports shall support start date and end date filter | P0 | -- |
| FR-SALE-009 | All sales reports shall display summary totals row below data | P0 | -- |
| FR-SALE-010 | All sales reports shall support share as PDF action | P1 | -- |
| FR-SALE-011 | All sales reports shall support print action | P1 | -- |
| FR-SALE-012 | All sales reports shall support export as CSV action | P2 | -- |
| FR-SALE-013 | Sales Trends chart shall support pinch-to-zoom and pan gestures | P2 | -- |
| FR-SALE-014 | Top Sellers report shall support configurable top-N (10, 25, 50) | P1 | -- |
| FR-SALE-015 | Commission Report shall display BV from recognized paid receipts only | P0 | -- |

## 5. Finance Reports Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-FIN-001 | Display Profit & Loss report with revenue, COGS, gross profit, expenses, net profit | P0 | `POST /api/owners/profit-loss.php` |
| FR-FIN-002 | Display Cash Flow report with opening balance, inflows, outflows, closing balance | P0 | `POST /api/owners/cash-flow.php` |
| FR-FIN-003 | Display Balance Sheet with assets (NBV, inventory, receivables, cash), liabilities, equity | P0 | `POST /api/owners/balance-sheet.php` |
| FR-FIN-004 | Display Account Reconciliation report per payment account | P1 | `POST /api/owners/account-reconciliation.php` |
| FR-FIN-005 | Display Expense Report grouped by category with totals | P0 | `POST /api/owners/expense-report.php` |
| FR-FIN-006 | Display Tax Report with taxable income, tax collected, tax payable | P1 | `POST /api/owners/tax-report.php` |
| FR-FIN-007 | Display Debtors List with distributor name, outstanding amount, aging buckets | P0 | `POST /api/owners/debtors-list.php` |
| FR-FIN-008 | Display Staff Credit report with staff member, credit amount, repayment status | P1 | `POST /api/owners/staff-credit.php` |
| FR-FIN-009 | All finance reports shall display amounts with franchise currency symbol | P0 | -- |
| FR-FIN-010 | All finance reports shall support date range filter | P0 | -- |
| FR-FIN-011 | P&L report shall show comparison with previous period when available | P1 | -- |
| FR-FIN-012 | Cash Flow report shall categorize by operating, investing, financing activities | P1 | -- |
| FR-FIN-013 | Balance Sheet shall display as-of date (single date, not range) | P0 | -- |
| FR-FIN-014 | Debtors List shall support sorting by amount or aging | P1 | -- |
| FR-FIN-015 | All finance reports shall support share as PDF action | P1 | -- |
| FR-FIN-016 | All finance reports shall support print action | P1 | -- |
| FR-FIN-017 | All finance reports shall support export as CSV action | P2 | -- |
| FR-FIN-018 | Expense Report shall show percentage of total per category | P1 | -- |

## 6. Inventory Reports Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-INV-001 | Display Inventory Valuation report with product, qty on hand, unit cost, total value | P0 | `POST /api/owners/inventory-valuation.php` |
| FR-INV-002 | Display Stock Adjustment Log with date, product, type, qty, reason, adjusted by | P0 | `POST /api/owners/stock-adjustments.php` |
| FR-INV-003 | Display Stock Transfer Log with date, product, from branch, to branch, qty, status | P0 | `POST /api/owners/stock-transfers.php` |
| FR-INV-004 | Inventory Valuation shall show grand total at bottom | P0 | -- |
| FR-INV-005 | Inventory Valuation shall display both local price and cost price columns | P0 | -- |
| FR-INV-006 | Stock Adjustment Log shall support filter by adjustment type | P1 | -- |
| FR-INV-007 | Stock Transfer Log shall support filter by branch | P1 | -- |
| FR-INV-008 | All inventory reports shall support date range filter and export actions | P0 | -- |

## 7. HR/Payroll Reports Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-HR-001 | Display Payroll report with staff name, basic salary, allowances, deductions, net pay | P0 | `POST /api/owners/payroll-report.php` |
| FR-HR-002 | Display Salary report with staff name, position, department, gross salary | P0 | `POST /api/owners/salary-report.php` |
| FR-HR-003 | Display Leave Summary with staff name, leave type, days taken, days remaining | P0 | `POST /api/owners/leave-summary.php` |
| FR-HR-004 | Payroll report shall filter by month/year | P0 | -- |
| FR-HR-005 | Payroll report shall display grand totals for all columns | P0 | -- |
| FR-HR-006 | Salary report shall support sorting by department or salary amount | P1 | -- |
| FR-HR-007 | Leave Summary shall support filter by leave type | P1 | -- |
| FR-HR-008 | All HR reports shall support date range filter and export actions | P0 | -- |

## 8. Distributor Reports Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-DIST-001 | Display Distributor Performance report with name, personal BV, group BV, rank | P0 | `POST /api/owners/distributor-performance.php` |
| FR-DIST-002 | Display Genealogy tree view with distributor hierarchy (max depth 20) | P0 | `POST /api/owners/genealogy.php` |
| FR-DIST-003 | Display Manager Legs report with manager, number of legs, total BV per leg | P0 | `POST /api/owners/manager-legs.php` |
| FR-DIST-004 | Display Rank Report with distributor name, current rank, qualification status | P0 | `POST /api/owners/rank-report.php` |
| FR-DIST-005 | Display Distributor Directory with searchable list (name, code, phone, email) | P0 | `POST /api/owners/distributor-directory.php` |
| FR-DIST-006 | Genealogy shall support expand/collapse of downline nodes | P0 | -- |
| FR-DIST-007 | Genealogy shall display distributor code, name, and rank at each node | P0 | -- |
| FR-DIST-008 | Distributor Directory shall support search by name or distributor code | P0 | -- |
| FR-DIST-009 | Distributor Performance shall use BV from recognized paid receipts only | P0 | -- |
| FR-DIST-010 | Distributor Performance shall support sorting by personal BV, group BV, or rank | P1 | -- |
| FR-DIST-011 | Rank Report shall indicate distributors who are close to next rank threshold | P2 | -- |
| FR-DIST-012 | All distributor reports shall support date range filter and export actions | P0 | -- |

## 9. Compliance Reports Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-COMP-001 | Display User Activity log with user, action type, table name, timestamp, IP address | P0 | `POST /api/owners/user-activity.php` |
| FR-COMP-002 | Display ID Card Report with distributor name, code, photo, issue date, expiry | P1 | `POST /api/owners/id-card-report.php` |
| FR-COMP-003 | User Activity shall support filter by user and action type | P0 | -- |
| FR-COMP-004 | User Activity shall display pagination for large result sets | P0 | -- |
| FR-COMP-005 | All compliance reports shall support date range filter and export actions | P0 | -- |

## 10. Approvals Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-APPR-001 | Display list of pending expense approvals with title, amount, requester, date | P0 | `GET /api/owners/approvals/expenses.php` |
| FR-APPR-002 | Display list of pending purchase order approvals | P0 | `GET /api/owners/approvals/purchase-orders.php` |
| FR-APPR-003 | Display list of pending stock transfer approvals | P0 | `GET /api/owners/approvals/stock-transfers.php` |
| FR-APPR-004 | Display list of pending stock adjustment approvals | P0 | `GET /api/owners/approvals/stock-adjustments.php` |
| FR-APPR-005 | Display list of pending payroll approvals | P0 | `GET /api/owners/approvals/payroll.php` |
| FR-APPR-006 | Display list of pending leave request approvals | P0 | `GET /api/owners/approvals/leave.php` |
| FR-APPR-007 | Display list of pending asset depreciation approvals | P1 | `GET /api/owners/approvals/asset-depreciation.php` |
| FR-APPR-008 | Filter approval list by status (pending, approved, rejected, all) | P0 | -- |
| FR-APPR-009 | View detailed information for any approval item | P0 | -- |
| FR-APPR-010 | Approve an item with optional notes | P0 | `POST /api/owners/approvals/{type}.php` |
| FR-APPR-011 | Reject an item with mandatory rejection reason | P0 | `POST /api/owners/approvals/{type}.php` |
| FR-APPR-012 | Display confirmation dialog before approve/reject action | P0 | -- |
| FR-APPR-013 | Show optimistic UI update after approval action (immediate visual feedback) | P0 | -- |
| FR-APPR-014 | Display audit trail (who requested, when, status changes) for each approval | P0 | -- |
| FR-APPR-015 | Queue approval actions when offline and sync when connection restored | P1 | -- |
| FR-APPR-016 | Display sync status indicator for queued offline approvals | P1 | -- |
| FR-APPR-017 | Handle conflict when approval was already processed by another device | P0 | -- |
| FR-APPR-018 | Display badge count for pending approvals in bottom navigation | P0 | -- |
| FR-APPR-019 | Support batch approve/reject for multiple items of same type | P2 | `POST /api/owners/approvals/batch.php` |
| FR-APPR-020 | Sort approvals by date (newest first) or amount (highest first) | P1 | -- |

## 11. Profile Module

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-PROF-001 | Display owner profile with name, email, phone, address, city, country, photo | P0 | `GET /api/owners/profile.php` |
| FR-PROF-002 | Edit phone, address, city, and country fields | P0 | `PUT /api/owners/profile.php` |
| FR-PROF-003 | Upload or change profile photo from camera or gallery | P1 | `POST /api/owners/profile-photo.php` |
| FR-PROF-004 | Display list of owned franchises with name, country, and currency | P0 | `GET /api/owners/franchises.php?action=list` |
| FR-PROF-005 | Validate phone number format before submission | P0 | -- |
| FR-PROF-006 | Display success/error feedback after profile update | P0 | -- |

## 12. Common Report Features

| ID | Requirement | Priority | API Endpoint |
|----|-------------|----------|-------------|
| FR-RPT-001 | All reports shall require start date and end date selection | P0 | -- |
| FR-RPT-002 | Default date range shall be current month (1st to today) | P0 | -- |
| FR-RPT-003 | Provide quick date presets: Today, This Month, Last Month, This Quarter, This Year | P0 | -- |
| FR-RPT-004 | "Last Month" button shall auto-load report without additional tap | P0 | -- |
| FR-RPT-005 | Share report as PDF via Android share sheet | P1 | -- |
| FR-RPT-006 | Print report via Android print framework | P1 | -- |
| FR-RPT-007 | Export report data as CSV file | P2 | -- |
| FR-RPT-008 | Display summary totals row below report data table | P0 | -- |
| FR-RPT-009 | Franchise selector available on each report screen | P0 | -- |
| FR-RPT-010 | All reports are strictly read-only (no edit or delete actions) | P0 | -- |

---

**Total Functional Requirements:** 138

| Module | Count |
|--------|-------|
| Authentication | 16 |
| Dashboard | 12 |
| Franchise Switcher | 8 |
| Sales Reports | 15 |
| Finance Reports | 18 |
| Inventory Reports | 8 |
| HR/Payroll Reports | 8 |
| Distributor Reports | 12 |
| Compliance Reports | 5 |
| Approvals | 20 |
| Profile | 6 |
| Common Report Features | 10 |

---

*Revision History*

| Date | Version | Author | Changes |
|------|---------|--------|---------|
| 2026-02-08 | 1.0 | Claude | Initial functional requirements |
