# API Contract: Dashboard and KPI Endpoints

**Parent:** [04_API_CONTRACT.md](../04_API_CONTRACT.md) | [All Docs](../README.md)

**Document:** 03 -- Dashboard, KPI, and Franchise Endpoints
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft

---

## Overview

Three endpoints support the executive dashboard and franchise management features. The dashboard endpoint is the first API call after login and drives the home screen KPI cards. The franchise endpoints allow multi-franchise owners to list and switch between their franchises.

| ID | Endpoint | Method | Auth | Status |
|----|----------|--------|------|--------|
| DASH-01 | `/api/owners/dashboard-stats.php` | GET | Yes | Exists |
| FRAN-01 | `/api/owners/franchises.php?action=list` | GET | Yes | Exists |
| FRAN-02 | `/api/owners/franchises.php?action=switch` | POST | Yes | Exists |

---

## DASH-01: Get Dashboard Stats

Returns executive KPI data for the owner's currently active franchise. This powers the 5 KPI cards on the home screen plus the pending approvals badge.

### Request

```
GET /api/owners/dashboard-stats.php
Authorization: Bearer {access_token}
X-Franchise-ID: 1
```

No request body or query parameters required. The server uses the current month-to-date period automatically.

### Success Response (200 OK)

```json
{
  "success": true,
  "data": {
    "sales_mtd": 12500000.00,
    "sales_mtd_trend": 8.5,
    "cash_balance": 45000000.00,
    "cash_balance_trend": -2.1,
    "inventory_value": 78000000.00,
    "inventory_value_trend": 3.2,
    "total_bv": 15420.00,
    "total_bv_trend": 12.0,
    "pending_approvals": 7,
    "pending_by_type": {
      "expenses": 3,
      "purchase_orders": 1,
      "stock_transfers": 2,
      "stock_adjustments": 0,
      "payroll": 1,
      "leave": 0,
      "asset_depreciation": 0
    },
    "currency": "UGX",
    "franchise_name": "Dynapharm Uganda",
    "period": {
      "start": "2026-02-01",
      "end": "2026-02-08"
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "franchise_id": 1,
    "franchise_name": "Dynapharm Uganda",
    "currency": "UGX"
  }
}
```

### Response Field Details

| Field | Type | Description |
|-------|------|-------------|
| `sales_mtd` | decimal | Total sales amount for the current month to date |
| `sales_mtd_trend` | decimal | Percentage change compared to same period last month |
| `cash_balance` | decimal | Current cash and bank balance across all accounts |
| `cash_balance_trend` | decimal | Percentage change from previous day |
| `inventory_value` | decimal | Total inventory value at local selling price |
| `inventory_value_trend` | decimal | Percentage change from start of month |
| `total_bv` | decimal | Total Business Volume for the current month |
| `total_bv_trend` | decimal | Percentage change compared to same period last month |
| `pending_approvals` | int | Total count of pending approval items |
| `pending_by_type` | object | Breakdown of pending approvals by workflow type |
| `currency` | string | ISO 4217 currency code for the franchise |
| `franchise_name` | string | Display name of the active franchise |
| `period.start` | string | Start date of the current reporting period |
| `period.end` | string | End date (today) of the current reporting period |

### Trend Values

Trend values are percentage changes expressed as decimals:
- Positive value (e.g., `8.5`) means an increase of 8.5%
- Negative value (e.g., `-2.1`) means a decrease of 2.1%
- `0.0` means no change
- `null` means trend data is not available (e.g., first month of operation)

The Android app displays these as green (positive) or red (negative) trend indicators with arrow icons.

### Error: Unauthorized (401)

```json
{
  "success": false,
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "Access token has expired.",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Error: Franchise Not Owned (403)

```json
{
  "success": false,
  "error": {
    "code": "FRANCHISE_NOT_OWNED",
    "message": "You do not have access to this franchise.",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Caching Strategy

| Aspect | Value |
|--------|-------|
| Cache TTL | 5 minutes |
| Stale-while-revalidate | Yes, show cached data while fetching fresh |
| Room cache key | `dashboard_stats_{franchise_id}` |
| Invalidation | On franchise switch, manual pull-to-refresh |

### Implementation Notes

- The existing `api/owners/dashboard-stats.php` endpoint is used directly
- Sales MTD is calculated from `tbl_invoices` joined with `tbl_paid_receipts`
- Cash balance is the dynamic sum from `tbl_payment_transactions`
- Inventory value uses the current effective price from `tbl_product_prices`
- BV calculation follows the standard pattern (join with `tbl_paid_receipts`)
- Pending approvals counts are aggregated from all 7 approval tables

---

## FRAN-01: List Franchises

Returns the list of franchises owned by the authenticated user. Used to populate the franchise switcher dropdown.

### Request

```
GET /api/owners/franchises.php?action=list
Authorization: Bearer {access_token}
X-Franchise-ID: 1
```

### Success Response (200 OK)

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Dynapharm Uganda",
      "country": "Uganda",
      "currency": "UGX",
      "timezone": "Africa/Kampala",
      "is_primary": true,
      "is_active": true,
      "logo_url": "/uploads/franchise/ug_logo.png",
      "branches_count": 3,
      "staff_count": 24,
      "distributors_count": 1250
    },
    {
      "id": 3,
      "name": "Dynapharm Kenya",
      "country": "Kenya",
      "currency": "KES",
      "timezone": "Africa/Nairobi",
      "is_primary": false,
      "is_active": false,
      "logo_url": "/uploads/franchise/ke_logo.png",
      "branches_count": 2,
      "staff_count": 18,
      "distributors_count": 890
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "total_franchises": 2,
    "active_franchise_id": 1
  }
}
```

### Response Field Details

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Franchise ID |
| `name` | string | Franchise display name |
| `country` | string | Country name |
| `currency` | string | ISO 4217 currency code |
| `timezone` | string | IANA timezone identifier |
| `is_primary` | bool | Whether this is the owner's primary/default franchise |
| `is_active` | bool | Whether this is the currently selected franchise |
| `logo_url` | string | Relative path to franchise logo image |
| `branches_count` | int | Number of branches in the franchise |
| `staff_count` | int | Number of active staff members |
| `distributors_count` | int | Number of active distributors |

### Implementation Notes

- The existing `api/owners/franchises.php` endpoint is used
- The `is_active` field reflects the currently selected franchise in the session/JWT
- The franchise list is cached in Room with a 1-hour TTL
- The Android app shows the franchise switcher in the top app bar

---

## FRAN-02: Switch Franchise

Switches the owner's active franchise context. All subsequent API calls will return data for the newly selected franchise.

### Request

```
POST /api/owners/franchises.php?action=switch
Authorization: Bearer {access_token}
Content-Type: application/json
X-Franchise-ID: 1
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `franchise_id` | int | Yes | ID of the franchise to switch to |

```json
{
  "franchise_id": 3
}
```

### Success Response (200 OK)

```json
{
  "success": true,
  "data": {
    "franchise_id": 3,
    "franchise_name": "Dynapharm Kenya",
    "currency": "KES",
    "timezone": "Africa/Nairobi",
    "access_token": "eyJhbGciOiJIUzI1NiIs..."
  },
  "message": "Switched to Dynapharm Kenya.",
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

A new access token is issued with the updated `fid` claim. The client must replace its stored access token.

### Error: Franchise Not Owned (403)

```json
{
  "success": false,
  "error": {
    "code": "FRANCHISE_NOT_OWNED",
    "message": "You do not have access to this franchise.",
    "details": {
      "requested_franchise_id": 5
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Error: Validation Error (422)

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The given data was invalid.",
    "details": {
      "franchise_id": ["Franchise ID is required.", "Must be a positive integer."]
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Android Client Behavior on Switch

When the user switches franchises, the Android app must:

1. Call `FRAN-02` with the new franchise ID
2. Store the new access token from the response
3. Update the `X-Franchise-ID` header for all subsequent requests
4. Clear all cached report data in Room (franchise-scoped)
5. Refresh the dashboard (call `DASH-01`)
6. Update the franchise name and currency in the top app bar
7. Reset any active report filters to defaults

### Implementation Notes

- The existing `api/owners/franchises.php` with `action=switch` is used
- The server validates ownership via `tbl_owner_franchises`
- A new JWT is issued with the updated `fid` claim
- The session's `franchise_id` is also updated for web compatibility

---

## PROF-01: Get Owner Profile

Returns the authenticated owner's profile information.

### Request

```
GET /api/owners/profile.php
Authorization: Bearer {access_token}
X-Franchise-ID: 1
```

### Success Response (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 42,
    "owner_id": 7,
    "first_name": "James",
    "last_name": "Christopher",
    "email": "owner@dynapharm.com",
    "phone": "+256700123456",
    "photo_url": "/uploads/avatars/user_42.jpg",
    "role": "owner",
    "language": "en",
    "created_at": "2024-01-15T09:00:00Z",
    "last_login": "2026-02-08T08:15:00Z",
    "franchises_owned": 2
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

---

## PROF-02: Update Owner Profile

Updates the owner's profile information (contact details, language, photo).

### Request

```
PUT /api/owners/profile.php
Authorization: Bearer {access_token}
Content-Type: application/json
X-Franchise-ID: 1
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `first_name` | string | No | First name |
| `last_name` | string | No | Last name |
| `phone` | string | No | Phone number |
| `language` | string | No | Preferred language (`en`, `fr`, `ar`, `sw`, `es`) |

```json
{
  "first_name": "James",
  "last_name": "Christopher",
  "phone": "+256700123456",
  "language": "en"
}
```

### Success Response (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 42,
    "first_name": "James",
    "last_name": "Christopher",
    "phone": "+256700123456",
    "language": "en"
  },
  "message": "Profile updated successfully.",
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial dashboard, franchise, and profile endpoints |
