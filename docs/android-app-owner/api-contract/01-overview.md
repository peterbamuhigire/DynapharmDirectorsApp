# API Contract: Overview and Conventions

**Parent:** [04_API_CONTRACT.md](../04_API_CONTRACT.md) | [All Docs](../README.md)

**Document:** 01 -- Overview and Conventions
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft

---

## 1. Base URLs

| Environment | Base URL | Notes |
|-------------|----------|-------|
| Development | `http://10.0.2.2/DMS_web/` | Android emulator -> localhost WAMP |
| Staging | `https://staging.dynapharm-dms.com/` | QA and UAT |
| Production | `https://app.dynapharm-dms.com/` | Live users |

All endpoint paths in this contract are relative to the base URL. Example:

```
{base_url}api/auth/mobile-login.php
{base_url}api/owners/dashboard-stats.php
```

---

## 2. Authentication

### 2.1 Method

JWT (JSON Web Token) Bearer Token authentication. The mobile app authenticates once via email/password and receives a token pair.

| Token | Lifetime | Purpose |
|-------|----------|---------|
| Access Token | 15 minutes | Sent with every authenticated request |
| Refresh Token | 30 days | Exchanged for a new access token when the current one expires |

### 2.2 JWT Payload Structure

```json
{
  "iss": "dynapharm-dms",
  "sub": 42,
  "fid": 1,
  "role": "owner",
  "oid": 7,
  "iat": 1707350400,
  "exp": 1707351300,
  "type": "access"
}
```

| Claim | Type | Description |
|-------|------|-------------|
| `iss` | string | Issuer -- always `dynapharm-dms` |
| `sub` | int | User ID (`tbl_users.id`) |
| `fid` | int | Active franchise ID (`tbl_franchises.id`) |
| `role` | string | User role -- `owner` for this app |
| `oid` | int | Owner record ID (`tbl_owners.id`) |
| `iat` | int | Issued-at Unix timestamp |
| `exp` | int | Expiry Unix timestamp |
| `type` | string | `access` or `refresh` |

### 2.3 Auth Flow Diagram

```
+-----------+   POST /api/auth/mobile-login.php   +-----------+
|           | ----------------------------------> |           |
|   App     |                                     |  Server   |
|           | <---------------------------------- |           |
|           |   { access_token,                   |           |
|           |     refresh_token,                  |           |
|           |     user, franchises }              |           |
|           |                                     |           |
|  Store    |   GET /api/owners/dashboard-stats   |           |
|  tokens   | ----------------------------------> |  Verify   |
|  in ESP   |   Authorization: Bearer <token>     |  JWT      |
|           |   X-Franchise-ID: 1                 |           |
|           | <---------------------------------- |           |
|           |   { success: true, data: {...} }    |           |
|           |                                     |           |
|  Got 401? |   POST /api/auth/refresh.php        |           |
|           | ----------------------------------> |  Issue    |
|           |   { refresh_token: "..." }          |  new      |
|           | <---------------------------------- |  access   |
|           |   { access_token, expires_in }      |  token    |
|           |                                     |           |
|  Refresh  |   Force logout                      |           |
|  failed?  | --> Clear tokens --> Login screen    |           |
+-----------+                                     +-----------+
```

### 2.4 Token Storage

Tokens are stored in Android `EncryptedSharedPreferences` (AES-256). The app never stores tokens in plain SharedPreferences, databases, or logs.

### 2.5 Auto-Refresh via OkHttp Authenticator

The Android app implements an OkHttp `Authenticator` that transparently intercepts 401 responses, calls the refresh endpoint, and retries the original request with the new access token. This is invisible to the UI layer.

---

## 3. Standard Request Format

### 3.1 Common Headers

| Header | Direction | Required | Value |
|--------|-----------|----------|-------|
| `Authorization` | Request | Yes (authenticated endpoints) | `Bearer {access_token}` |
| `Content-Type` | Both | Yes (POST/PUT) | `application/json` |
| `Accept` | Request | Recommended | `application/json` |
| `X-Franchise-ID` | Request | Yes (authenticated endpoints) | Active franchise ID (int) |
| `Accept-Language` | Request | Optional | `en`, `fr`, `ar`, `sw`, `es` |
| `X-Request-ID` | Request | Optional | Client-generated UUID for tracing |

### 3.2 Date Parameters

All date parameters use ISO 8601 format: `YYYY-MM-DD`.

```
date_from=2026-02-01
date_to=2026-02-08
```

The server automatically appends `23:59:59` to `date_to` values for inclusive end-of-day filtering.

---

## 4. Standard Response Envelope

### 4.1 Success Response

```json
{
  "success": true,
  "data": {
    "sales_mtd": 12500000.00,
    "cash_balance": 45000000.00
  },
  "message": "Dashboard stats retrieved successfully.",
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_abc123",
    "franchise_id": 1,
    "franchise_name": "Dynapharm Uganda",
    "currency": "UGX"
  }
}
```

### 4.2 Paginated Success Response

```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Product A" },
    { "id": 2, "name": "Product B" }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_def456",
    "page": 1,
    "per_page": 50,
    "total": 150,
    "total_pages": 3,
    "has_next": true,
    "has_prev": false
  }
}
```

### 4.3 Standard Error Response

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The given data was invalid.",
    "details": {
      "date_from": ["Required field."],
      "date_to": ["Must be after date_from."]
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_ghi789"
  }
}
```

---

## 5. HTTP Status Codes

| Status | Meaning | When Used |
|--------|---------|-----------|
| 200 | OK | Successful GET, successful approval action |
| 201 | Created | Resource created (rare in owner app) |
| 400 | Bad Request | Malformed JSON, missing `Content-Type` header |
| 401 | Unauthorized | Missing, expired, or invalid token |
| 403 | Forbidden | Not owner role, franchise not owned by user |
| 404 | Not Found | Report or approval record not found |
| 409 | Conflict | Approval already processed |
| 422 | Unprocessable Entity | Validation errors on request body |
| 423 | Locked | Account locked after too many failed login attempts |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Unexpected server failure |
| 503 | Service Unavailable | Server under maintenance |

---

## 6. Pagination

### 6.1 Model

Offset-based pagination using query parameters.

```
?page=1&per_page=50
```

| Parameter | Type | Default | Max | Description |
|-----------|------|---------|-----|-------------|
| `page` | int | 1 | -- | Page number (1-indexed) |
| `per_page` | int | 25 | 100 | Items per page |

### 6.2 Response Meta Fields

| Field | Type | Description |
|-------|------|-------------|
| `meta.page` | int | Current page number |
| `meta.per_page` | int | Items per page |
| `meta.total` | int | Total number of items |
| `meta.total_pages` | int | Total number of pages |
| `meta.has_next` | bool | Whether a next page exists |
| `meta.has_prev` | bool | Whether a previous page exists |

### 6.3 Example

Request: `GET /api/owners/reports/distributor-directory.php?page=2&per_page=25`

Response meta:
```json
{
  "meta": {
    "page": 2,
    "per_page": 25,
    "total": 342,
    "total_pages": 14,
    "has_next": true,
    "has_prev": true
  }
}
```

---

## 7. Rate Limiting

| Endpoint Category | Limit | Window |
|-------------------|-------|--------|
| `/api/auth/mobile-login.php` | 5 requests | Per minute |
| `/api/auth/refresh.php` | 10 requests | Per minute |
| `/api/auth/password-reset.php` | 3 requests | Per minute |
| All authenticated endpoints | 60 requests | Per minute |

### 7.1 Rate Limit Headers

```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 42
X-RateLimit-Reset: 1707351300
Retry-After: 18
```

When rate limited (HTTP 429), the client should wait for the `Retry-After` value (in seconds) before retrying.

---

## 8. Versioning Strategy

### 8.1 Current Approach

No URL-based versioning for v1. All endpoints use the current format.

### 8.2 Future Versioning

When breaking changes are needed, the API will use URL prefix versioning:

```
/api/v2/owners/dashboard-stats.php
```

Non-breaking changes (adding optional fields, new endpoints) will be made to the current version without bumping the version number.

### 8.3 Deprecation Policy

When an endpoint is deprecated:
1. A `X-Deprecated: true` header is added to responses
2. A `sunset` date is communicated via `X-Sunset: 2026-12-31` header
3. The old endpoint continues working for at least 90 days
4. The Android app shows an upgrade prompt when detecting deprecated endpoints

---

## 9. Multi-Tenant Isolation

Every authenticated request must include the `X-Franchise-ID` header. The server validates that:

1. The franchise ID exists in the JWT payload's allowed franchises
2. The requesting user (owner) owns that franchise
3. All database queries are scoped to that franchise ID

If the franchise ID is missing or unauthorized, the server returns 403 `FRANCHISE_NOT_OWNED`.

---

## 10. Data Format Conventions

| Data Type | Format | Example |
|-----------|--------|---------|
| Dates | ISO 8601 (`YYYY-MM-DD`) | `2026-02-08` |
| Timestamps | ISO 8601 with timezone | `2026-02-08T10:30:00Z` |
| Money | Decimal with 2 places | `12500000.00` |
| Percentages | Decimal with 1-2 places | `8.5`, `12.00` |
| Boolean | JSON `true`/`false` | `true` |
| IDs | Integer | `42` |
| Null values | JSON `null` | `null` |

### 10.1 Currency

Monetary values are always raw numbers without formatting. The response `meta` includes `currency` (ISO 4217 code) so the client can format appropriately.

```json
{
  "amount": 12500000.00,
  "meta": { "currency": "UGX" }
}
```

The Android app formats this as `UGX 12,500,000.00` using the franchise locale.

---

## 11. Endpoint Summary

| Category | Count | Methods | Sub-Document |
|----------|-------|---------|--------------|
| Authentication | 4 | POST | [02-endpoints-auth.md](02-endpoints-auth.md) |
| Dashboard / KPI | 1 | GET | [03-endpoints-dashboard-kpi.md](03-endpoints-dashboard-kpi.md) |
| Franchise | 2 | GET, POST | [03-endpoints-dashboard-kpi.md](03-endpoints-dashboard-kpi.md) |
| Reports (Sales) | 7 | POST | [04-endpoints-reports.md](04-endpoints-reports.md) |
| Reports (Finance) | 8 | POST, GET | [04-endpoints-reports.md](04-endpoints-reports.md) |
| Reports (Inventory) | 3 | GET, POST | [04-endpoints-reports.md](04-endpoints-reports.md) |
| Reports (HR/Payroll) | 3 | POST | [04-endpoints-reports.md](04-endpoints-reports.md) |
| Reports (Distributors) | 5 | POST, GET | [04-endpoints-reports.md](04-endpoints-reports.md) |
| Reports (Compliance) | 2 | POST | [04-endpoints-reports.md](04-endpoints-reports.md) |
| Approvals | 7 workflows | GET, POST | [05-endpoints-approvals.md](05-endpoints-approvals.md) |
| Error Reference | -- | -- | [06-error-codes.md](06-error-codes.md) |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial overview and conventions |
