# Backend Phase 1: Required Endpoints for Login & Dashboard

**Status:** Implementation Guide
**App:** Dyna Director (Android)
**Phase:** 1 - Login and Basic Info Display
**Last Updated:** 2026-02-09

---

## Overview

This document specifies the **minimum backend endpoints** required to complete Phase 1 of the Dyna Director Android app. The app is now fully configured and ready to connect to these endpoints.

**Base URL:** `http://192.168.1.5/dms_web/`
**API Path:** `api/`

---

## Required Endpoints (Priority Order)

### 1. ✅ LOGIN ENDPOINT (CRITICAL)

**File:** `dms_web/api/auth/mobile-login.php`

**Method:** `POST`

**Request Headers:**
```
Content-Type: application/json
Host: dynapharm.peter
```

**Request Body:**
```json
{
  "email": "director@example.com",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "director@example.com",
      "role": "owner"
    },
    "franchises": [
      {
        "id": 1,
        "name": "Kampala Central",
        "branch_count": 3
      },
      {
        "id": 2,
        "name": "Nairobi Branch",
        "branch_count": 5
      }
    ]
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password",
    "details": null
  }
}
```

**Implementation Notes:**
- Check user exists with email and password (hashed)
- Verify user has `role = 'owner'` or similar director role
- Generate JWT access token (15 min expiry)
- Generate refresh token (30 days expiry)
- Fetch user's franchises from database
- Return franchise list with branch counts

---

### 2. ✅ DASHBOARD STATS ENDPOINT (CRITICAL)

**File:** `dms_web/api/owners/dashboard-stats.php`

**Method:** `GET`

**Request Headers:**
```
Authorization: Bearer {access_token}
X-Franchise-ID: 1
Host: dynapharm.peter
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "sales_mtd": 45320.50,
    "cash_balance": 12500.75,
    "inventory_value": 87650.00,
    "total_bv": 1234.56,
    "pending_approvals": 8,
    "sales_trend": "up",
    "cash_trend": "neutral",
    "inventory_trend": "down",
    "bv_trend": "up",
    "approvals_trend": "neutral"
  },
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "franchise_id": 1,
    "franchise_name": "Kampala Central"
  }
}
```

**Implementation Notes:**
- Verify JWT token and extract user ID
- Get franchise_id from `X-Franchise-ID` header
- Verify user owns/has access to this franchise
- Calculate/fetch dashboard KPIs from database:
  - `sales_mtd`: Month-to-date sales total
  - `cash_balance`: Current cash on hand
  - `inventory_value`: Total inventory value
  - `total_bv`: Total Business Value points
  - `pending_approvals`: Count of pending approval requests
- Calculate trends (compare to previous period):
  - "up": Increased by >5%
  - "down": Decreased by >5%
  - "neutral": Changed by <5%

---

### 3. TOKEN REFRESH ENDPOINT (IMPORTANT)

**File:** `dms_web/api/auth/refresh.php`

**Method:** `POST`

**Request Body:**
```json
{
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Implementation Notes:**
- Verify refresh token is valid and not expired
- Generate new access token (15 min expiry)
- Do NOT generate new refresh token (keep existing)

---

### 4. LOGOUT ENDPOINT (OPTIONAL)

**File:** `dms_web/api/auth/logout.php`

**Method:** `POST`

**Request Headers:**
```
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": null
}
```

**Implementation Notes:**
- Invalidate both access and refresh tokens
- Add tokens to blacklist table if using token blacklisting
- Clear user session data

---

## Database Schema Requirements

### Users Table
```sql
- id (INT, PRIMARY KEY)
- email (VARCHAR, UNIQUE)
- password_hash (VARCHAR) -- bcrypt or similar
- name (VARCHAR)
- role (ENUM: 'owner', 'staff', 'distributor', etc.)
- created_at (TIMESTAMP)
```

### Franchises Table
```sql
- id (INT, PRIMARY KEY)
- name (VARCHAR)
- owner_id (INT, FOREIGN KEY to users.id)
- branch_count (INT) -- or calculate from branches table
- created_at (TIMESTAMP)
```

### Dashboard Stats (Calculated from existing tables)
```sql
-- Sales MTD: SELECT SUM(total) FROM sales WHERE MONTH(date) = MONTH(NOW())
-- Cash Balance: SELECT SUM(amount) FROM cash_transactions
-- Inventory Value: SELECT SUM(quantity * unit_price) FROM inventory
-- Total BV: SELECT SUM(bv_points) FROM distributor_orders
-- Pending Approvals: SELECT COUNT(*) FROM approvals WHERE status = 'pending'
```

---

## Testing Checklist

### Manual Testing Steps:

1. **Test Login Endpoint:**
   ```bash
   curl -X POST http://192.168.1.5/dms_web/api/auth/mobile-login.php \
     -H "Content-Type: application/json" \
     -H "Host: dynapharm.peter" \
     -d '{"email":"director@example.com","password":"password123"}'
   ```

2. **Test Dashboard Endpoint:**
   ```bash
   curl -X GET http://192.168.1.5/dms_web/api/owners/dashboard-stats.php \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "X-Franchise-ID: 1" \
     -H "Host: dynapharm.peter"
   ```

3. **Test from Android App:**
   - Launch "Dyna Director" app
   - Enter credentials
   - Tap "Sign In"
   - Verify franchise selection appears (if multiple franchises)
   - Verify dashboard loads with real data
   - Check that franchise banner shows correct franchise name

---

## Quick Start: Minimal Implementation

If you need to get the app working ASAP, here's a minimal mock implementation:

### `api/auth/mobile-login.php` (Mock):
```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$input = json_decode(file_get_contents('php://input'), true);

// Mock authentication (REPLACE WITH REAL AUTH)
if ($input['email'] === 'demo@dynapharm.com' && $input['password'] === 'demo123') {
    echo json_encode([
        'success' => true,
        'data' => [
            'access_token' => 'mock_access_token_12345',
            'refresh_token' => 'mock_refresh_token_67890',
            'user' => [
                'id' => 1,
                'name' => 'Demo Director',
                'email' => 'demo@dynapharm.com',
                'role' => 'owner'
            ],
            'franchises' => [
                ['id' => 1, 'name' => 'Kampala Central', 'branch_count' => 3],
                ['id' => 2, 'name' => 'Nairobi Branch', 'branch_count' => 5]
            ]
        ]
    ]);
} else {
    http_response_code(401);
    echo json_encode([
        'success' => false,
        'error' => [
            'code' => 'INVALID_CREDENTIALS',
            'message' => 'Invalid email or password'
        ]
    ]);
}
```

### `api/owners/dashboard-stats.php` (Mock):
```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$franchiseId = $_SERVER['HTTP_X_FRANCHISE_ID'] ?? 1;

echo json_encode([
    'success' => true,
    'data' => [
        'sales_mtd' => 45320.50,
        'cash_balance' => 12500.75,
        'inventory_value' => 87650.00,
        'total_bv' => 1234.56,
        'pending_approvals' => 8,
        'sales_trend' => 'up',
        'cash_trend' => 'neutral',
        'inventory_trend' => 'down',
        'bv_trend' => 'up',
        'approvals_trend' => 'neutral'
    ],
    'meta' => [
        'timestamp' => date('c'),
        'franchise_id' => (int)$franchiseId,
        'franchise_name' => $franchiseId == 1 ? 'Kampala Central' : 'Nairobi Branch'
    ]
]);
```

---

## Next Steps

1. ✅ **Create the two PHP files** above in your `dms_web/api/` directory
2. ✅ **Test login** with demo credentials: `demo@dynapharm.com` / `demo123`
3. ✅ **Verify** dashboard loads with mock data
4. 🔄 **Replace mock logic** with real database queries
5. 🔄 **Implement JWT** token generation and validation
6. 🔄 **Add security**: CSRF protection, rate limiting, SQL injection prevention

---

## Contact & Support

If you encounter issues, check:
- WAMP server is running
- Virtual host `dynapharm.peter` is configured
- Logo file exists at: `dms_web/dist/img/icons/DynaLogo.png`
- API files have correct permissions (readable by web server)

**App Status:** ✅ Ready and waiting for backend endpoints!
