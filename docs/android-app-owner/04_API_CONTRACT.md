# API Contract -- Dynapharm Owner Portal App

**Document:** API Contract Index
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft
**Owner:** Dynapharm International

---

## Overview

This document defines the complete API contract between the Dynapharm Owner Portal Android App and the DMS_web PHP backend. The owner portal is **read-heavy**: the vast majority of endpoints are GET requests returning report data, dashboard KPIs, and approval queues. The only mutation endpoints are approval actions (approve/reject) and profile updates.

The backend already has **9 owner API endpoints** in `api/owners/`. This contract builds upon those and defines additional endpoints needed for the full report suite and approval workflows.

---

## Sub-Documents

This API Contract is split into six focused sub-files for maintainability.

| # | Document | Path | Summary |
|---|----------|------|---------|
| 1 | [Overview and Conventions](api-contract/01-overview.md) | `api-contract/01-overview.md` | Base URLs, authentication flow, request/response conventions, pagination, rate limiting, versioning strategy |
| 2 | [Auth Endpoints](api-contract/02-endpoints-auth.md) | `api-contract/02-endpoints-auth.md` | Login, token refresh, logout, password change -- shared with distributor app JWT infrastructure |
| 3 | [Dashboard and KPI Endpoints](api-contract/03-endpoints-dashboard-kpi.md) | `api-contract/03-endpoints-dashboard-kpi.md` | Dashboard stats, franchise list, franchise switch, KPI trend data |
| 4 | [Report Endpoints](api-contract/04-endpoints-reports.md) | `api-contract/04-endpoints-reports.md` | All 23 report endpoints across 6 categories (Sales, Finance, Inventory, HR, Distributors, Compliance) |
| 5 | [Approval Endpoints](api-contract/05-endpoints-approvals.md) | `api-contract/05-endpoints-approvals.md` | Approval queue listing, detail view, approve/reject actions for all 7 workflows |
| 6 | [Error Codes](api-contract/06-error-codes.md) | `api-contract/06-error-codes.md` | Complete error code catalog, HTTP status mapping, error response examples, retry guidance |

---

## Quick Navigation

- **Previous:** [03_SDS.md](03_SDS.md) -- Software Design Specification
- **Next:** [05_USER_JOURNEYS.md](05_USER_JOURNEYS.md) -- User Journeys
- **All Docs:** [README.md](README.md)

---

## Base URL Configuration

| Environment | Base URL | Usage |
|-------------|---------|-------|
| Development | `http://dynapharm.peter/` | Android emulator -> localhost |
| Staging | `https://erp.dynapharmafrica.com/` | QA and UAT testing |
| Production | `https://coulderp.dynapharmafrica.com/` | Live users |

All endpoints are relative to the base URL. Example: `{base_url}api/owners/dashboard-stats.php`

---

## Standard Response Envelope

Every API response follows this consistent JSON structure.

### Success Response

```json
{
  "success": true,
  "data": {
    "...payload specific to endpoint..."
  },
  "meta": {
    "timestamp": "2026-02-08T12:00:00Z",
    "franchise_id": 1,
    "franchise_name": "Dynapharm Uganda"
  }
}
```

### Success Response with Pagination

```json
{
  "success": true,
  "data": [ "...array of items..." ],
  "pagination": {
    "current_page": 1,
    "per_page": 25,
    "total_items": 342,
    "total_pages": 14,
    "has_next": true,
    "has_prev": false
  },
  "meta": {
    "timestamp": "2026-02-08T12:00:00Z",
    "franchise_id": 1
  }
}
```

### Error Response

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Start date must be before end date",
    "field": "start_date",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T12:00:00Z",
    "request_id": "req_abc123"
  }
}
```

---

## Authentication Summary

| Aspect | Detail |
|--------|--------|
| Method | JWT Bearer Token |
| Access Token TTL | 15 minutes |
| Refresh Token TTL | 30 days |
| Header | `Authorization: Bearer {access_token}` |
| Franchise Header | `X-Franchise-ID: {franchise_id}` |
| Token Storage | EncryptedSharedPreferences |
| Refresh Flow | Auto-refresh via OkHttp Authenticator |

### Auth Flow

```
1. POST /api/auth/login.php      --> access_token + refresh_token + franchises[]
2. Every request includes:
   - Authorization: Bearer {access_token}
   - X-Franchise-ID: {selected_franchise_id}
3. On 401: POST /api/auth/refresh.php --> new access_token
4. On refresh failure: redirect to login
```

---

## Endpoint Count Summary

| Category | Endpoints | Method Mix | Status |
|----------|-----------|-----------|--------|
| Auth | 4 | POST (3), DELETE (1) | Shared with distributor app |
| Dashboard/KPI | 3 | GET (3) | 2 ready, 1 new |
| Franchise | 2 | GET (2) | Ready |
| Sales Reports | 7 | GET (7) | 3 ready, 4 new |
| Finance Reports | 8 | GET (8) | 2 ready, 6 new |
| Inventory Reports | 3 | GET (3) | 1 ready, 2 new |
| HR/Payroll Reports | 3 | GET (3) | 1 ready, 2 new |
| Distributor Reports | 5 | GET (5) | 2 ready, 3 new |
| Compliance Reports | 2 | GET (2) | 0 ready, 2 new |
| Approvals | 9 | GET (3), POST (6) | All new |
| Profile | 3 | GET (1), PUT (1), POST (1) | Ready |
| **Total** | **~49** | **GET: 37, POST: 10, PUT: 1, DELETE: 1** | **~13 ready, ~36 new** |

### Existing Endpoints (Ready to Use)

| # | Endpoint | File | Method |
|---|----------|------|--------|
| 1 | `/api/owners/dashboard-stats.php` | `dashboard-stats.php` | GET |
| 2 | `/api/owners/franchises.php` | `franchises.php` | GET |
| 3 | `/api/owners/daily-sales.php` | `daily-sales.php` | GET |
| 4 | `/api/owners/cash-flow.php` | `cash-flow.php` | GET |
| 5 | `/api/owners/inventory-valuation.php` | `inventory-valuation.php` | GET |
| 6 | `/api/owners/distributor-performance.php` | `distributor-performance.php` | GET |
| 7 | `/api/owners/manager-legs.php` | `manager-legs.php` | GET |
| 8 | `/api/owners/profile.php` | `profile.php` | GET/PUT |
| 9 | `/api/owners/user-activity.php` | `user-activity.php` | GET |

### Common Query Parameters (All Report Endpoints)

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `start_date` | string (YYYY-MM-DD) | No | First day of current month | Report start date |
| `end_date` | string (YYYY-MM-DD) | No | Today | Report end date |
| `branch_id` | int | No | All branches | Filter by branch |
| `page` | int | No | 1 | Pagination page number |
| `per_page` | int | No | 25 | Items per page (max 100) |

---

## How to Read This Contract

1. **Start with this index** to understand conventions and authentication
2. **Read `api-contract/01-overview.md`** for detailed conventions, pagination, and versioning
3. **Read `api-contract/02-endpoints-auth.md`** for the JWT auth flow
4. **Read endpoint files (03-05)** for the specific endpoints your module needs
5. **Reference `api-contract/06-error-codes.md`** for error handling implementation

Each endpoint document includes: HTTP method, URL, request parameters, request body (for POST/PUT), success response JSON, error response JSON, and implementation notes.

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial API contract index |
