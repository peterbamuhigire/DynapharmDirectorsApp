# API Contract: Auth Endpoints

**Parent:** [04_API_CONTRACT.md](../04_API_CONTRACT.md) | [All Docs](../README.md)

**Document:** 02 -- Authentication Endpoints
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft

---

## Overview

Four authentication endpoints handle the JWT lifecycle for the Owner Portal Android app. These endpoints are shared infrastructure with the Distributor app -- the `role` claim in the JWT distinguishes owner from distributor sessions.

| ID | Endpoint | Method | Auth Required |
|----|----------|--------|---------------|
| AUTH-01 | `/api/auth/mobile-login.php` | POST | No |
| AUTH-02 | `/api/auth/refresh.php` | POST | No |
| AUTH-03 | `/api/auth/logout.php` | POST | Yes |
| AUTH-04 | `/api/auth/password-reset.php` | POST | No |

---

## AUTH-01: Login

Authenticates an owner by email and password, returning a JWT token pair and user profile.

### Request

```
POST /api/auth/mobile-login.php
Content-Type: application/json
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | string | Yes | User email address |
| `password` | string | Yes | User password |
| `device_name` | string | No | Device identifier for session tracking |

```json
{
  "email": "owner@dynapharm.com",
  "password": "SecureP@ss123",
  "device_name": "Samsung Galaxy S24 Ultra"
}
```

### Success Response (200 OK)

```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g...",
    "expires_in": 900,
    "token_type": "Bearer",
    "user": {
      "id": 42,
      "email": "owner@dynapharm.com",
      "role": "owner",
      "owner_id": 7,
      "first_name": "James",
      "last_name": "Christopher",
      "photo_url": "/uploads/avatars/user_42.jpg"
    },
    "franchises": [
      {
        "id": 1,
        "name": "Dynapharm Uganda",
        "country": "Uganda",
        "currency": "UGX",
        "timezone": "Africa/Kampala",
        "is_primary": true
      },
      {
        "id": 3,
        "name": "Dynapharm Kenya",
        "country": "Kenya",
        "currency": "KES",
        "timezone": "Africa/Nairobi",
        "is_primary": false
      }
    ],
    "default_franchise_id": 1
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Error: Invalid Credentials (401)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password.",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Error: Account Locked (423)

Returned after 5 consecutive failed login attempts. Account auto-unlocks after 15 minutes.

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
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Error: Not Owner Role (403)

Returned when the user exists but does not have the `owner` role. This app is exclusively for franchise owners.

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
      "email": ["Email is required.", "Must be a valid email address."],
      "password": ["Password is required."]
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Rate Limit

5 requests per minute per IP address.

### Implementation Notes

- The server checks `tbl_users` for the email, verifies password hash via `password_verify()`
- If the user's role is not `owner`, the server returns 403 `NOT_OWNER_ROLE`
- On success, the server queries `tbl_owner_franchises` to build the franchises list
- The `default_franchise_id` is the owner's primary franchise
- Failed login attempts are tracked in `tbl_login_attempts`

---

## AUTH-02: Refresh Token

Exchanges a valid refresh token for a new access token. The refresh token itself is not rotated (single-use rotation may be added in v2).

### Request

```
POST /api/auth/refresh.php
Content-Type: application/json
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `refresh_token` | string | Yes | The refresh token received during login |

```json
{
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

### Success Response (200 OK)

```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "expires_in": 900,
    "token_type": "Bearer"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Error: Invalid Refresh Token (401)

Returned when the refresh token is malformed, tampered, or has been revoked.

```json
{
  "success": false,
  "error": {
    "code": "INVALID_REFRESH_TOKEN",
    "message": "Invalid refresh token. Please log in again.",
    "details": null
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Error: Refresh Token Expired (401)

Returned when the refresh token has expired (after 30 days).

```json
{
  "success": false,
  "error": {
    "code": "REFRESH_TOKEN_EXPIRED",
    "message": "Session expired. Please log in again.",
    "details": {
      "expired_at": "2026-03-10T10:30:00Z"
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Rate Limit

10 requests per minute per user.

### Implementation Notes

- The server decodes the refresh token, checks the `type` claim is `refresh`
- The server verifies the token has not been revoked in `tbl_refresh_tokens`
- A new access token is issued with the same claims but a fresh `iat` and `exp`
- The `fid` claim in the new access token reflects the user's current active franchise
- On failure, the Android OkHttp Authenticator triggers a logout and redirects to login

---

## AUTH-03: Logout

Invalidates the refresh token on the server side. The access token naturally expires after its TTL.

### Request

```
POST /api/auth/logout.php
Authorization: Bearer {access_token}
Content-Type: application/json
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `refresh_token` | string | No | Refresh token to revoke. If omitted, all refresh tokens for this user/device are revoked. |

```json
{
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

### Success Response (200 OK)

```json
{
  "success": true,
  "data": null,
  "message": "Logged out successfully.",
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Behavior

- This endpoint always returns 200, even if the token is already invalid or expired
- This is intentional to prevent information leakage and ensure the client can always cleanly log out
- The server marks the refresh token as revoked in `tbl_refresh_tokens`

### Android Client Behavior

On logout, the Android app must:
1. Call this endpoint (fire-and-forget, do not block on response)
2. Clear tokens from EncryptedSharedPreferences
3. Clear any cached data in Room database
4. Navigate to the login screen
5. Clear the back stack

---

## AUTH-04: Password Reset

Initiates a password reset by sending a reset link to the user's email address.

### Request

```
POST /api/auth/password-reset.php
Content-Type: application/json
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | string | Yes | Email address associated with the account |

```json
{
  "email": "owner@dynapharm.com"
}
```

### Success Response (200 OK)

This endpoint always returns 200 regardless of whether the email exists. This prevents email enumeration attacks.

```json
{
  "success": true,
  "data": null,
  "message": "If an account with that email exists, a password reset link has been sent.",
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
      "email": ["Email is required.", "Must be a valid email address."]
    }
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z"
  }
}
```

### Rate Limit

3 requests per minute per IP address.

### Implementation Notes

- The server generates a time-limited reset token (valid for 1 hour)
- The reset token is stored in `tbl_password_resets` with a hashed value
- The email contains a link to the web-based password reset form
- After successful reset, all existing refresh tokens for the user are revoked
- The Android app shows a "Check your email" screen with a "Back to Login" button

---

## Security Considerations

### Token Security

| Aspect | Implementation |
|--------|---------------|
| Signing Algorithm | HS256 (HMAC-SHA256) |
| Secret Key | Stored in server environment variable, never in code |
| Token Storage (Android) | EncryptedSharedPreferences with AES-256 |
| Token Transmission | HTTPS only (TLS 1.2+) |
| Refresh Token Revocation | Server-side blacklist in `tbl_refresh_tokens` |

### Brute Force Protection

| Protection | Detail |
|------------|--------|
| Login Rate Limit | 5 attempts per minute per IP |
| Account Lockout | After 5 consecutive failures, lock for 15 minutes |
| Lockout Reset | Counter resets on successful login |
| IP Tracking | Failed attempts logged with IP address |

### Session Management

- Each device gets its own refresh token (supports multi-device login)
- Logout from one device does not affect other devices
- Owner can revoke all sessions from the web portal
- Refresh tokens are stored hashed in the database

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial auth endpoints specification |
