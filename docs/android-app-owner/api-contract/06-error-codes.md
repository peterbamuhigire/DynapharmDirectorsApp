# API Contract: Error Codes Reference

**Parent:** [04_API_CONTRACT.md](../04_API_CONTRACT.md) | [All Docs](../README.md)

**Document:** 06 -- Error Codes and Error Handling
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft

---

## Overview

This document provides a complete catalog of error codes returned by the Dynapharm Owner Portal API, along with the corresponding HTTP status codes, user-facing messages, and retry guidance for the Android client.

---

## Complete Error Code Reference

| HTTP Status | Error Code | Description | User Message | Retry Policy |
|-------------|-----------|-------------|--------------|--------------|
| 400 | `BAD_REQUEST` | Malformed JSON or missing Content-Type | "Invalid request format." | No retry |
| 401 | `INVALID_CREDENTIALS` | Wrong email or password | "Invalid email or password." | No retry |
| 401 | `TOKEN_EXPIRED` | Access token has expired | (transparent -- auto refresh) | Auto-refresh |
| 401 | `INVALID_TOKEN` | Malformed or tampered token | "Session invalid. Please log in." | No retry, logout |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh token malformed or revoked | "Session invalid. Please log in again." | No retry, logout |
| 401 | `REFRESH_TOKEN_EXPIRED` | Refresh token expired (30 days) | "Session expired. Please log in again." | No retry, logout |
| 403 | `FORBIDDEN` | User lacks required permission | "Access denied." | No retry |
| 403 | `FRANCHISE_NOT_OWNED` | User does not own this franchise | "You don't have access to this franchise." | No retry |
| 403 | `NOT_OWNER_ROLE` | User is not a franchise owner | "This app is for franchise owners only." | No retry |
| 404 | `NOT_FOUND` | Generic resource not found | "The requested item was not found." | No retry |
| 404 | `APPROVAL_NOT_FOUND` | Approval item not found | "This approval item was not found." | No retry |
| 404 | `REPORT_NOT_AVAILABLE` | No data for the requested period | "No data available for this period." | No retry |
| 409 | `ALREADY_PROCESSED` | Approval already approved/rejected | "This item has already been processed." | No retry, refresh |
| 409 | `CONFLICT` | Generic conflict | "A conflict occurred. Please refresh." | Refresh then retry |
| 422 | `VALIDATION_ERROR` | Field validation failed | Show field-specific errors | No retry, fix input |
| 423 | `ACCOUNT_LOCKED` | Too many failed login attempts | "Account locked. Try again in 15 minutes." | Retry after 15 min |
| 429 | `RATE_LIMITED` | Too many requests | "Too many requests. Please wait." | Retry after `Retry-After` header |
| 500 | `SERVER_ERROR` | Internal server error | "Something went wrong. Please try again." | Retry 1x |
| 503 | `SERVICE_UNAVAILABLE` | Server maintenance | "Service temporarily unavailable." | Retry with backoff |

---

## Error Response Format

All error responses follow this structure:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable description of the error.",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_abc123"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `success` | bool | Always `false` for error responses |
| `error.code` | string | Machine-readable error code (use for switch/when logic) |
| `error.message` | string | Human-readable description |
| `error.details` | object/null | Additional context, varies by error type |
| `meta.timestamp` | string | Server timestamp of the error |
| `meta.request_id` | string | Unique request ID for debugging |

---

## Detailed Error Response Examples

### 400 Bad Request

```json
{
  "success": false,
  "error": {
    "code": "BAD_REQUEST",
    "message": "Request body must be valid JSON.",
    "details": {
      "parse_error": "Unexpected token at position 42"
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_bad001"
  }
}
```

### 401 Invalid Credentials

```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password.",
    "details": {
      "attempts_remaining": 3
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_auth001"
  }
}
```

### 401 Token Expired

```json
{
  "success": false,
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "Access token has expired.",
    "details": {
      "expired_at": "2026-02-08T10:15:00Z"
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_tok001"
  }
}
```

### 401 Invalid Token

```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "The provided token is invalid or has been tampered with.",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_tok002"
  }
}
```

### 403 Franchise Not Owned

```json
{
  "success": false,
  "error": {
    "code": "FRANCHISE_NOT_OWNED",
    "message": "You do not have access to this franchise.",
    "details": {
      "requested_franchise_id": 5,
      "owned_franchise_ids": [1, 3]
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_fran001"
  }
}
```

### 403 Not Owner Role

```json
{
  "success": false,
  "error": {
    "code": "NOT_OWNER_ROLE",
    "message": "This app is for franchise owners only. Please use the staff or distributor app.",
    "details": {
      "user_role": "staff"
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_role001"
  }
}
```

### 404 Approval Not Found

```json
{
  "success": false,
  "error": {
    "code": "APPROVAL_NOT_FOUND",
    "message": "The requested approval item was not found.",
    "details": {
      "type": "expenses",
      "id": 999
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_appr001"
  }
}
```

### 409 Already Processed

```json
{
  "success": false,
  "error": {
    "code": "ALREADY_PROCESSED",
    "message": "This expense has already been approved.",
    "details": {
      "current_status": "approved",
      "processed_by": "James Christopher",
      "processed_at": "2026-02-08T10:45:00Z"
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:46:00Z",
    "request_id": "req_conf001"
  }
}
```

### 422 Validation Error

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The given data was invalid.",
    "details": {
      "date_from": ["Required field."],
      "date_to": ["Must be after date_from.", "Date format must be YYYY-MM-DD."],
      "branch_id": ["Must be a positive integer."]
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_val001"
  }
}
```

### 423 Account Locked

```json
{
  "success": false,
  "error": {
    "code": "ACCOUNT_LOCKED",
    "message": "Account locked due to too many failed attempts. Try again in 15 minutes.",
    "details": {
      "locked_until": "2026-02-08T10:45:00Z",
      "retry_after_seconds": 900
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_lock001"
  }
}
```

### 429 Rate Limited

```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many requests. Please wait before trying again.",
    "details": {
      "limit": 60,
      "window_seconds": 60,
      "retry_after_seconds": 18
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_rate001"
  }
}
```

Response headers:
```
HTTP/1.1 429 Too Many Requests
Retry-After: 18
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1707351318
```

### 500 Server Error

```json
{
  "success": false,
  "error": {
    "code": "SERVER_ERROR",
    "message": "An unexpected error occurred. Please try again.",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_srv001"
  }
}
```

Note: In production, `details` is always `null` for 500 errors to prevent leaking internal information. In development/staging, the error message may include more context.

### 503 Service Unavailable

```json
{
  "success": false,
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "The service is temporarily unavailable for maintenance. Please try again later.",
    "details": {
      "estimated_recovery": "2026-02-08T11:00:00Z"
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "request_id": "req_mnt001"
  }
}
```

Response headers:
```
HTTP/1.1 503 Service Unavailable
Retry-After: 1800
```

---

## Error Handling Flow (Android Client)

```
API Response Received
    |
    +-- 200-299 (Success)
    |   +-- Parse response.data
    |   +-- Update UI / Room cache
    |   +-- Done
    |
    +-- 401 (Unauthorized)
    |   +-- Is error.code == "TOKEN_EXPIRED"?
    |   |   +-- Yes --> OkHttp Authenticator handles automatically:
    |   |   |           1. Call POST /api/auth/refresh.php
    |   |   |           2. Store new access_token
    |   |   |           3. Retry original request
    |   |   |           4. If refresh fails --> Logout flow
    |   |   +-- No --> Force logout:
    |   |               1. Clear EncryptedSharedPreferences
    |   |               2. Clear Room database
    |   |               3. Navigate to LoginScreen
    |   |               4. Show "Session expired" message
    |
    +-- 403 (Forbidden)
    |   +-- Show Snackbar with error.message
    |   +-- For FRANCHISE_NOT_OWNED: reset franchise switcher
    |   +-- For NOT_OWNER_ROLE: logout and show message
    |
    +-- 404 (Not Found)
    |   +-- Show "not found" state in UI
    |   +-- For REPORT_NOT_AVAILABLE: show empty state with date range
    |
    +-- 409 (Conflict)
    |   +-- For ALREADY_PROCESSED:
    |   |   1. Show info message
    |   |   2. Refresh approval list
    |   |   3. Update local cache
    |   +-- For CONFLICT:
    |       1. Show conflict message
    |       2. Refresh data
    |
    +-- 422 (Validation Error)
    |   +-- Parse error.details (field -> messages map)
    |   +-- Show inline error messages on form fields
    |   +-- Highlight invalid fields
    |
    +-- 423 (Account Locked)
    |   +-- Show lockout message with timer
    |   +-- Disable login button until retry_after_seconds
    |
    +-- 429 (Rate Limited)
    |   +-- Read Retry-After header
    |   +-- Wait silently
    |   +-- Auto-retry after delay
    |   +-- Show Snackbar only if delay > 5 seconds
    |
    +-- 500 (Server Error)
    |   +-- Retry once automatically after 2-second delay
    |   +-- If retry fails: show error state with retry button
    |   +-- Log to Timber for crash reporting
    |
    +-- 503 (Service Unavailable)
        +-- Read Retry-After header
        +-- Show maintenance screen
        +-- Auto-retry with exponential backoff:
            1st: 5 seconds
            2nd: 15 seconds
            3rd: 45 seconds
            Max: 5 minutes
```

---

## Android Kotlin Error Handling Pattern

Use a sealed `ApiResult<T>` class with `Success` and `Error` variants. Define error code constants in an `ErrorCodes` object matching the codes in the reference table above. In ViewModels, use `when` on `result.code` to handle specific errors (e.g., `VALIDATION_ERROR` -> show field errors, `ALREADY_PROCESSED` -> refresh list, `REPORT_NOT_AVAILABLE` -> show empty state).

---

## Retry Policy Summary

| Error Code | Auto-Retry | Max Retries | Backoff | User Action |
|-----------|------------|-------------|---------|-------------|
| `TOKEN_EXPIRED` | Yes (OkHttp) | 1 | None | None (transparent) |
| `RATE_LIMITED` | Yes | 1 | Wait `Retry-After` | None if < 5s, Snackbar if > 5s |
| `SERVER_ERROR` | Yes | 1 | 2 seconds | Retry button if auto-retry fails |
| `SERVICE_UNAVAILABLE` | Yes | 3 | Exponential (5s, 15s, 45s) | Maintenance screen |
| All others | No | 0 | N/A | Show error, user corrects |

---

## Debugging and Logging

### Request ID

Every response includes `meta.request_id`. The Android app should:
1. Log the request ID with Timber for every failed request
2. Include the request ID in crash reports
3. Display the request ID on error screens (small text) for support tickets

### Development Mode

In debug builds, the Android app shows additional error details:
- Full error JSON in a collapsible panel
- Request/response headers
- Network timing information
- A "Copy Error Details" button

In release builds, only the user-friendly message is shown.

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial error codes reference |
