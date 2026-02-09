# SRS 03 -- Error Handling & Traceability Matrix

**Parent:** [02_SRS.md](../02_SRS.md) | [All Docs](../README.md)

---

## 1. Error Categories & Handling Strategy

| Category | HTTP Status | Error Example | Retry Policy | User Message |
|----------|-------------|---------------|--------------|--------------|
| Network Timeout | -- | No response within 30s | Auto-retry 3x with exponential backoff (1s, 2s, 4s) | "Connection timed out. Retrying..." |
| No Connection | -- | Device has no internet | No retry; show cached data if available | "No internet connection. Showing cached data." |
| Auth Token Expired | 401 | Access token expired | Auto-refresh token, then retry original request | (transparent to user) |
| Auth Revoked | 401 | Refresh token invalid or expired | No retry; redirect to login | "Session expired. Please log in again." |
| Forbidden | 403 | User does not have owner role | No retry | "You don't have access to this franchise." |
| Validation Error | 422 | Invalid date range or missing field | No retry; highlight invalid fields | Show field-specific validation messages |
| Not Found | 404 | Report data not available | No retry | "Report data not available for this period." |
| Business Conflict | 409 | Approval already processed | No retry; refresh local state | "This item has already been approved/rejected." |
| Rate Limited | 429 | Too many requests in window | Auto-retry after `Retry-After` header value | "Please wait a moment before trying again." |
| Server Error | 500 | Internal server error | Auto-retry 1x after 2s delay | "Something went wrong. Please try again." |
| Sync Conflict | 409 | Offline approval conflicts with server state | Server wins; refresh local data | "This approval was already processed on another device." |
| Parse Error | -- | Malformed JSON response | No retry | "Unexpected response from server. Please try again." |
| SSL/TLS Error | -- | Certificate pinning failure | No retry | "Secure connection failed. Please update the app." |

### 1.1 Error Response Format (API Contract)

All API errors return this JSON structure:

```json
{
    "success": false,
    "message": "Human-readable error message",
    "error_code": "VALIDATION_ERROR",
    "errors": {
        "start_date": "Start date is required",
        "end_date": "End date must be after start date"
    }
}
```

### 1.2 Error Code Mapping

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `AUTH_INVALID_CREDENTIALS` | 401 | Email or password is incorrect |
| `AUTH_TOKEN_EXPIRED` | 401 | Access token has expired |
| `AUTH_REFRESH_INVALID` | 401 | Refresh token is invalid or revoked |
| `AUTH_ACCOUNT_LOCKED` | 423 | Account locked after failed attempts |
| `AUTH_NOT_OWNER` | 403 | User does not have owner role |
| `FRANCHISE_NOT_FOUND` | 404 | Franchise does not exist or user has no access |
| `FRANCHISE_ACCESS_DENIED` | 403 | User does not own this franchise |
| `VALIDATION_ERROR` | 422 | Request body failed validation |
| `APPROVAL_ALREADY_PROCESSED` | 409 | Approval was already approved or rejected |
| `APPROVAL_NOT_FOUND` | 404 | Approval item does not exist |
| `REPORT_NO_DATA` | 200 | Report returned empty results (not an error) |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

### 1.3 Retry Strategy Implementation

```
Attempt 1: Immediate request
  -> On failure (timeout/5xx):
Attempt 2: Wait 1 second, retry
  -> On failure:
Attempt 3: Wait 2 seconds, retry
  -> On failure:
Attempt 4: Wait 4 seconds, retry
  -> On failure:
Display error to user with "Try Again" button
```

Exceptions to retry:
- 401, 403, 404, 409, 422 -- never retry (not transient)
- 429 -- retry after `Retry-After` header (not exponential)

### 1.4 Offline Approval Queue Error Handling

| Scenario | Behavior |
|----------|----------|
| Action queued while offline | Show "Pending sync" indicator on the item |
| Connection restored | WorkManager triggers sync, processes queue in FIFO order |
| Sync succeeds | Update local status to "synced", remove from queue |
| Sync fails with 409 (conflict) | Mark as "failed", refresh server state, notify user |
| Sync fails with 5xx | Increment retry count, re-queue with backoff |
| Max retries exceeded (3) | Mark as "failed", show error badge, allow manual retry |

---

## 2. Traceability Matrix -- Functional Requirements to Implementation

### 2.1 Authentication Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-AUTH-001 | Authenticate via email + password | -- | `POST /api/auth/mobile-login.php` | LoginScreen |
| FR-AUTH-002 | Store access token securely | -- (EncryptedSharedPreferences) | -- | -- |
| FR-AUTH-003 | Store refresh token securely | -- (EncryptedSharedPreferences) | -- | -- |
| FR-AUTH-004 | Auto-refresh expired tokens | -- | `POST /api/auth/refresh.php` | -- (Interceptor) |
| FR-AUTH-005 | Redirect on refresh failure | -- | -- | LoginScreen |
| FR-AUTH-006 | Biometric authentication | -- | -- | BiometricPrompt |
| FR-AUTH-007 | Validate owner role from JWT | -- | -- | LoginScreen |
| FR-AUTH-008 | Logout and clear tokens | -- (clear all) | `POST /api/auth/logout.php` | LoginScreen |
| FR-AUTH-009 | Password reset via email | -- | `POST /api/auth/password-reset.php` | ForgotPasswordScreen |
| FR-AUTH-010 | Enforce password requirements | -- | -- | LoginScreen |
| FR-AUTH-011 | Display login errors | -- | -- | LoginScreen |
| FR-AUTH-012 | Persist session 30 days | -- (EncryptedSharedPreferences) | -- | -- |
| FR-AUTH-013 | Lock after 5 failed attempts | -- | -- | LoginScreen |
| FR-AUTH-014 | Show/hide password toggle | -- | -- | LoginScreen |
| FR-AUTH-015 | Display app version | -- | -- | LoginScreen |
| FR-AUTH-016 | Remember email checkbox | -- (DataStore) | -- | LoginScreen |

### 2.2 Dashboard Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-DASH-001 | Sales MTD KPI card | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-002 | Cash Balance KPI card | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-003 | Inventory Value KPI card | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-004 | Total BV KPI card | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-005 | Pending Approvals badge | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-006 | Trend indicators | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-007 | Quick report buttons | -- | -- | DashboardScreen |
| FR-DASH-008 | Navigate to report | -- | -- | DashboardScreen |
| FR-DASH-009 | Approval summary card | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-010 | Navigate to approvals | -- | -- | DashboardScreen |
| FR-DASH-011 | Pull-to-refresh | DashboardKpiEntity | `GET /api/owners/dashboard-stats.php` | DashboardScreen |
| FR-DASH-012 | Franchise name in header | FranchiseEntity | -- | DashboardScreen |

### 2.3 Franchise Switcher Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-FRAN-001 | List owned franchises | FranchiseEntity | `GET /api/owners/franchises.php?action=list` | FranchiseSwitcherSheet |
| FR-FRAN-002 | Show name, country, currency | FranchiseEntity | -- | FranchiseSwitcherSheet |
| FR-FRAN-003 | Switch active franchise | FranchiseEntity | `POST /api/owners/franchises.php?action=switch` | FranchiseSwitcherSheet |
| FR-FRAN-004 | Reload data after switch | DashboardKpiEntity, ReportCacheEntity | -- | All screens |
| FR-FRAN-005 | Persist last active franchise | -- (DataStore) | -- | -- |
| FR-FRAN-006 | Default to primary franchise | FranchiseEntity | -- | -- |
| FR-FRAN-007 | Active franchise in top bar | FranchiseEntity | -- | TopAppBar |
| FR-FRAN-008 | Accessible from any screen | -- | -- | TopAppBar |

### 2.4 Sales Reports Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-SALE-001 | Sales Summary report | ReportCacheEntity | `POST /api/owners/sales-summary.php` | SalesSummaryScreen |
| FR-SALE-002 | Daily Sales report | ReportCacheEntity | `POST /api/owners/daily-sales.php` | DailySalesScreen |
| FR-SALE-003 | Sales Trends chart | ReportCacheEntity | `POST /api/owners/sales-trends.php` | SalesTrendsScreen |
| FR-SALE-004 | Sales by Product report | ReportCacheEntity | `POST /api/owners/sales-by-product.php` | SalesByProductScreen |
| FR-SALE-005 | Product Performance report | ReportCacheEntity | `POST /api/owners/product-performance.php` | ProductPerfScreen |
| FR-SALE-006 | Top Sellers report | ReportCacheEntity | `POST /api/owners/top-sellers.php` | TopSellersScreen |
| FR-SALE-007 | Commission Report | ReportCacheEntity | `POST /api/owners/commission-report.php` | CommissionScreen |

### 2.5 Finance Reports Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-FIN-001 | Profit & Loss report | ReportCacheEntity | `POST /api/owners/profit-loss.php` | ProfitLossScreen |
| FR-FIN-002 | Cash Flow report | ReportCacheEntity | `POST /api/owners/cash-flow.php` | CashFlowScreen |
| FR-FIN-003 | Balance Sheet | ReportCacheEntity | `POST /api/owners/balance-sheet.php` | BalanceSheetScreen |
| FR-FIN-004 | Account Reconciliation | ReportCacheEntity | `POST /api/owners/account-reconciliation.php` | AcctReconScreen |
| FR-FIN-005 | Expense Report | ReportCacheEntity | `POST /api/owners/expense-report.php` | ExpenseReportScreen |
| FR-FIN-006 | Tax Report | ReportCacheEntity | `POST /api/owners/tax-report.php` | TaxReportScreen |
| FR-FIN-007 | Debtors List | ReportCacheEntity | `POST /api/owners/debtors-list.php` | DebtorsListScreen |
| FR-FIN-008 | Staff Credit report | ReportCacheEntity | `POST /api/owners/staff-credit.php` | StaffCreditScreen |

### 2.6 Inventory Reports Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-INV-001 | Inventory Valuation | ReportCacheEntity | `POST /api/owners/inventory-valuation.php` | InvValuationScreen |
| FR-INV-002 | Stock Adjustment Log | ReportCacheEntity | `POST /api/owners/stock-adjustments.php` | StockAdjScreen |
| FR-INV-003 | Stock Transfer Log | ReportCacheEntity | `POST /api/owners/stock-transfers.php` | StockTransferScreen |

### 2.7 HR/Payroll Reports Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-HR-001 | Payroll report | ReportCacheEntity | `POST /api/owners/payroll-report.php` | PayrollScreen |
| FR-HR-002 | Salary report | ReportCacheEntity | `POST /api/owners/salary-report.php` | SalaryScreen |
| FR-HR-003 | Leave Summary | ReportCacheEntity | `POST /api/owners/leave-summary.php` | LeaveSummaryScreen |

### 2.8 Distributor Reports Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-DIST-001 | Distributor Performance | ReportCacheEntity | `POST /api/owners/distributor-performance.php` | DistPerfScreen |
| FR-DIST-002 | Genealogy tree view | ReportCacheEntity | `POST /api/owners/genealogy.php` | GenealogyScreen |
| FR-DIST-003 | Manager Legs report | ReportCacheEntity | `POST /api/owners/manager-legs.php` | ManagerLegsScreen |
| FR-DIST-004 | Rank Report | ReportCacheEntity | `POST /api/owners/rank-report.php` | RankReportScreen |
| FR-DIST-005 | Distributor Directory | ReportCacheEntity | `POST /api/owners/distributor-directory.php` | DistDirectoryScreen |

### 2.9 Compliance Reports Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-COMP-001 | User Activity log | ReportCacheEntity | `POST /api/owners/user-activity.php` | UserActivityScreen |
| FR-COMP-002 | ID Card Report | ReportCacheEntity | `POST /api/owners/id-card-report.php` | IdCardScreen |

### 2.10 Approvals Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-APPR-001 | Pending expenses list | ApprovalEntity | `GET /api/owners/approvals/expenses.php` | ExpenseApprovalsScreen |
| FR-APPR-002 | Pending purchase orders | ApprovalEntity | `GET /api/owners/approvals/purchase-orders.php` | POApprovalsScreen |
| FR-APPR-003 | Pending stock transfers | ApprovalEntity | `GET /api/owners/approvals/stock-transfers.php` | TransferApprovalsScreen |
| FR-APPR-004 | Pending stock adjustments | ApprovalEntity | `GET /api/owners/approvals/stock-adjustments.php` | AdjustApprovalsScreen |
| FR-APPR-005 | Pending payroll | ApprovalEntity | `GET /api/owners/approvals/payroll.php` | PayrollApprovalsScreen |
| FR-APPR-006 | Pending leave requests | ApprovalEntity | `GET /api/owners/approvals/leave.php` | LeaveApprovalsScreen |
| FR-APPR-007 | Pending asset depreciation | ApprovalEntity | `GET /api/owners/approvals/asset-depreciation.php` | AssetApprovalsScreen |
| FR-APPR-010 | Approve with notes | ApprovalEntity, SyncQueueEntity | `POST /api/owners/approvals/{type}.php` | ApprovalDetailScreen |
| FR-APPR-011 | Reject with reason | ApprovalEntity, SyncQueueEntity | `POST /api/owners/approvals/{type}.php` | ApprovalDetailScreen |
| FR-APPR-015 | Queue offline approvals | SyncQueueEntity | -- | -- (WorkManager) |
| FR-APPR-019 | Batch approve/reject | ApprovalEntity, SyncQueueEntity | `POST /api/owners/approvals/batch.php` | ApprovalListScreen |

### 2.11 Profile Module

| FR ID | Requirement | Room Entity | API Endpoint | Screen |
|-------|-------------|-------------|--------------|--------|
| FR-PROF-001 | View owner profile | ProfileEntity | `GET /api/owners/profile.php` | ProfileScreen |
| FR-PROF-002 | Edit phone/address/city/country | ProfileEntity | `PUT /api/owners/profile.php` | EditProfileScreen |
| FR-PROF-003 | Upload profile photo | ProfileEntity | `POST /api/owners/profile-photo.php` | EditProfileScreen |
| FR-PROF-004 | View owned franchises | FranchiseEntity | `GET /api/owners/franchises.php?action=list` | ProfileScreen |

---

## 3. Android Version Support

| Parameter | Value |
|-----------|-------|
| Minimum SDK | API 29 (Android 10) |
| Target SDK | API 31 (Android 12) |
| Compile SDK | API 34 (Android 14) |
| Kotlin JVM Target | 17 |
| Java Compatibility | 17 |
| Compose Compiler | Compatible with Kotlin 2.0+ |

---

## 4. Acceptance Criteria Summary

| Module | Total FRs | P0 | P1 | P2 | MVP Scope |
|--------|-----------|----|----|----|----|
| Authentication | 16 | 10 | 4 | 2 | P0 only |
| Dashboard | 12 | 10 | 2 | 0 | P0 only |
| Franchise Switcher | 8 | 8 | 0 | 0 | All |
| Sales Reports | 15 | 6 | 6 | 3 | P0 + P1 |
| Finance Reports | 18 | 8 | 8 | 2 | P0 only |
| Inventory Reports | 8 | 5 | 3 | 0 | P0 only |
| HR/Payroll Reports | 8 | 5 | 3 | 0 | P0 only |
| Distributor Reports | 12 | 7 | 3 | 2 | P0 only |
| Compliance Reports | 5 | 4 | 1 | 0 | P0 only |
| Approvals | 20 | 13 | 5 | 2 | P0 only |
| Profile | 6 | 4 | 1 | 1 | P0 only |
| Common Report Features | 10 | 7 | 2 | 1 | P0 only |
| **Total** | **138** | **87** | **38** | **13** | -- |

---

*Revision History*

| Date | Version | Author | Changes |
|------|---------|--------|---------|
| 2026-02-08 | 1.0 | Claude | Initial error handling and traceability matrix |
