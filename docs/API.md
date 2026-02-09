# API Integration Guide - Dynapharm Owner Hub

**Version:** 1.0
**Last Updated:** 2026-02-09
**Backend:** DMS_web (PHP)
**App:** Dynapharm Owner Hub (Android)

---

## Purpose

This document provides a developer-friendly guide for integrating with the DMS_web backend API. It covers authentication flow, endpoint patterns, error handling, and Retrofit implementation examples.

For comprehensive API specifications, see `docs/android-app-owner/04_API_CONTRACT.md`.

---

## Table of Contents

1. [Base URLs and Environment Configuration](#base-urls-and-environment-configuration)
2. [Authentication Flow (JWT)](#authentication-flow-jwt)
3. [Franchise Context Headers](#franchise-context-headers)
4. [Endpoint Categories](#endpoint-categories)
5. [Request/Response Patterns](#requestresponse-patterns)
6. [Error Handling](#error-handling)
7. [Retrofit Service Examples](#retrofit-service-examples)
8. [Pagination](#pagination)
9. [Caching Strategy](#caching-strategy)
10. [Rate Limiting](#rate-limiting)

---

## Base URLs and Environment Configuration

### Build Variant Configuration

| Environment | Base URL | Build Variant | Usage |
|-------------|----------|---------------|-------|
| Development | `http://dynapharm.peter/` | `debug` | Local WAMP server via emulator |
| Staging | `https://erp.dynapharmafrica.com/` | `staging` | QA and UAT testing |
| Production | `https://coulderp.dynapharmafrica.com/` | `release` | Live production users |

### Gradle Configuration

```kotlin
// build.gradle.kts (app module)
android {
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://dynapharm.peter/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("String", "CERTIFICATE_PINS", "\"\"")
        }
        create("staging") {
            buildConfigField("String", "API_BASE_URL", "\"https://erp.dynapharmafrica.com/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("String", "CERTIFICATE_PINS", "\"sha256/STAGING_PIN_HASH\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://coulderp.dynapharmafrica.com/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("String", "CERTIFICATE_PINS", "\"sha256/PRIMARY_PIN;sha256/BACKUP_PIN\"")
        }
    }
}
```

### Retrofit Base URL Setup

```kotlin
@Provides @Singleton
fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
    Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
```

---

## Authentication Flow (JWT)

### Overview

The app uses JWT (JSON Web Token) authentication with a two-token system:

- **Access Token**: Short-lived (15 minutes), sent with every authenticated request
- **Refresh Token**: Long-lived (30 days), used to obtain new access tokens

### Token Lifecycle

```
┌─────────────┐
│ 1. Login    │  POST /api/auth/mobile-login.php
│             │  { email, password }
└──────┬──────┘
       │
       v
┌─────────────────────────────────────────────┐
│ Server Response                             │
│ {                                           │
│   access_token: "eyJ...",                   │
│   refresh_token: "eyJ...",                  │
│   user: { id, name, email, role },          │
│   franchises: [{ id, name, currency }]      │
│ }                                           │
└──────┬──────────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────────┐
│ 2. Store in EncryptedSharedPreferences      │
│    - access_token                           │
│    - refresh_token                          │
│    - active_franchise_id                    │
│    - owner_id                               │
└──────┬──────────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────────┐
│ 3. All Authenticated Requests               │
│    Headers:                                 │
│      Authorization: Bearer {access_token}   │
│      X-Franchise-ID: {active_franchise_id}  │
└──────┬──────────────────────────────────────┘
       │
       v  (15 minutes later)
┌─────────────────────────────────────────────┐
│ 4. Access Token Expired (401 Response)      │
└──────┬──────────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────────┐
│ 5. Auto-Refresh via Authenticator           │
│    POST /api/auth/refresh.php               │
│    { refresh_token: "eyJ..." }              │
│                                             │
│    Response: { access_token: "eyJ..." }     │
└──────┬──────────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────────┐
│ 6. Retry Original Request with New Token    │
│    (Transparent to UI layer)                │
└─────────────────────────────────────────────┘
```

### Auth Endpoints

#### POST /api/auth/mobile-login.php

**Purpose:** Authenticate user and receive token pair

**Request:**
```json
{
  "email": "owner@dynapharm.com",
  "password": "SecurePassword123!"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 900,
    "user": {
      "id": 42,
      "name": "John Kamau",
      "email": "owner@dynapharm.com",
      "role": "owner"
    },
    "franchises": [
      {
        "id": 1,
        "name": "Dynapharm Uganda",
        "country": "Uganda",
        "currency": "UGX"
      },
      {
        "id": 3,
        "name": "Dynapharm Kenya",
        "country": "Kenya",
        "currency": "KES"
      }
    ]
  },
  "message": "Login successful"
}
```

**Error Responses:**
- 401: Invalid credentials
- 423: Account locked (too many failed attempts)
- 422: Validation error (missing email/password)

#### POST /api/auth/refresh.php

**Purpose:** Exchange refresh token for new access token

**Request:**
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 900
  }
}
```

**Error Response:**
- 401: Refresh token expired or invalid → Force logout

#### DELETE /api/auth/logout.php

**Purpose:** Invalidate refresh token on server

**Headers:**
```
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

### TokenManager Implementation

```kotlin
class TokenManager @Inject constructor(
    private val prefs: SharedPreferences
) {
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_ACTIVE_FRANCHISE_ID)
            remove(KEY_OWNER_ID)
        }
    }

    fun setActiveFranchiseId(franchiseId: Long) {
        prefs.edit { putLong(KEY_ACTIVE_FRANCHISE_ID, franchiseId) }
    }

    fun getActiveFranchiseId(): Long? {
        val id = prefs.getLong(KEY_ACTIVE_FRANCHISE_ID, -1)
        return if (id == -1L) null else id
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ACTIVE_FRANCHISE_ID = "active_franchise_id"
        private const val KEY_OWNER_ID = "owner_id"
        private const val KEY_LAST_ACTIVITY = "last_activity"
    }
}
```

---

## Franchise Context Headers

### X-Franchise-ID Header

Every authenticated request MUST include the `X-Franchise-ID` header to identify which franchise's data to access.

**Critical Security Rule:** The server validates that the requesting owner has access to the specified franchise. Unauthorized access returns HTTP 403.

### TenantInterceptor Implementation

```kotlin
@Singleton
class TenantInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = tokenManager.getActiveFranchiseId()?.let { franchiseId ->
            chain.request().newBuilder()
                .addHeader("X-Franchise-ID", franchiseId.toString())
                .build()
        } ?: chain.request()
        return chain.proceed(request)
    }
}
```

### Franchise Switching

When a user switches franchises:

1. Update `TokenManager.setActiveFranchiseId(newFranchiseId)`
2. Clear cached data for the previous franchise
3. Fetch fresh data for the new franchise

**All subsequent API calls automatically include the new franchise ID.**

---

## Endpoint Categories

### 1. Authentication (4 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/auth/mobile-login.php` | POST | Initial login |
| `/api/auth/refresh.php` | POST | Refresh access token |
| `/api/auth/logout.php` | DELETE | Invalidate refresh token |
| `/api/auth/password-change.php` | POST | Change password |

### 2. Dashboard & KPI (3 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/owners/dashboard-stats.php` | GET | KPIs and summary stats |
| `/api/owners/franchises.php` | GET | List of franchises |
| `/api/owners/switch-franchise.php` | POST | Switch active franchise |

### 3. Reports - Sales (7 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/owners/reports/daily-sales.php` | GET | Daily sales breakdown |
| `/api/owners/reports/sales-summary.php` | POST | Sales summary report |
| `/api/owners/reports/sales-trends.php` | POST | Sales trend analysis |
| `/api/owners/reports/sales-by-product.php` | POST | Product-level sales |
| `/api/owners/reports/top-sellers.php` | POST | Top-selling products |
| `/api/owners/reports/product-performance.php` | POST | Product performance metrics |
| `/api/owners/reports/commission-report.php` | POST | Commission calculations |

### 4. Reports - Finance (8 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/owners/reports/profit-loss.php` | POST | P&L statement |
| `/api/owners/reports/cash-flow.php` | GET | Cash flow analysis |
| `/api/owners/reports/balance-sheet.php` | POST | Balance sheet |
| `/api/owners/reports/expense-report.php` | POST | Expense breakdown |
| `/api/owners/reports/account-reconciliation.php` | POST | Account reconciliation |
| `/api/owners/reports/employee-debts.php` | POST | Employee debt tracking |
| `/api/owners/reports/debtors.php` | POST | Debtor accounts |
| `/api/owners/reports/inventory-valuation.php` | GET | Inventory value |

### 5. Reports - Inventory (3 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/owners/reports/stock-transfer-log.php` | GET | Stock transfer history |
| `/api/owners/reports/stock-adjustment-log.php` | POST | Stock adjustment history |
| `/api/owners/reports/inventory-valuation.php` | GET | Current inventory value |

### 6. Reports - HR/Payroll (3 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/owners/reports/payroll-summary.php` | POST | Payroll breakdown |
| `/api/owners/reports/leave-report.php` | POST | Employee leave tracking |
| `/api/owners/reports/user-activity.php` | GET | Staff activity log |

### 7. Reports - Distributors (5 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/owners/reports/distributor-directory.php` | POST | Distributor list |
| `/api/owners/reports/distributor-performance.php` | GET | Performance metrics |
| `/api/owners/reports/distributor-genealogy.php` | POST | Network tree |
| `/api/owners/reports/manager-legs.php` | GET | Manager leg breakdown |
| `/api/owners/reports/rank-report.php` | POST | Rank progression |

### 8. Approvals (9 endpoints)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/owners/approvals/queue.php` | GET | Pending approvals queue |
| `/api/owners/approvals/expenses/list.php` | GET | Expense approvals |
| `/api/owners/approvals/expenses/{id}.php` | GET | Expense detail |
| `/api/owners/approvals/expenses/{id}/approve.php` | POST | Approve expense |
| `/api/owners/approvals/expenses/{id}/reject.php` | POST | Reject expense |
| `/api/owners/approvals/purchases/list.php` | GET | Purchase approvals |
| `/api/owners/approvals/discounts/list.php` | GET | Discount approvals |
| `/api/owners/approvals/writeoffs/list.php` | GET | Write-off approvals |
| `/api/owners/approvals/returns/list.php` | GET | Return approvals |

---

## Request/Response Patterns

### Standard Response Envelope

All API responses follow this consistent structure:

#### Success Response

```json
{
  "success": true,
  "data": { /* payload specific to endpoint */ },
  "message": "Operation successful",
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "request_id": "req_abc123",
    "franchise_id": 1,
    "franchise_name": "Dynapharm Uganda",
    "currency": "UGX"
  }
}
```

#### Error Response

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
    "timestamp": "2026-02-09T10:30:00Z",
    "request_id": "req_def456"
  }
}
```

### Common Query Parameters (Reports)

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `start_date` | string (YYYY-MM-DD) | No | First day of month | Report start date |
| `end_date` | string (YYYY-MM-DD) | No | Today | Report end date |
| `branch_id` | int | No | All branches | Filter by branch |
| `page` | int | No | 1 | Page number |
| `per_page` | int | No | 25 | Items per page (max 100) |

### Date Format

All dates use ISO 8601 format: `YYYY-MM-DD`

**Example:** `2026-02-09`

The server automatically appends `23:59:59` to `end_date` for inclusive end-of-day filtering.

---

## Error Handling

### HTTP Status Code Mapping

| Status | Error Code | Meaning | Client Action |
|--------|-----------|---------|---------------|
| 400 | `BAD_REQUEST` | Malformed JSON or headers | Show error, don't retry |
| 401 | `UNAUTHORIZED` | Invalid/expired token | Auto-refresh token, retry |
| 403 | `FORBIDDEN` | Franchise not owned | Show error, force logout |
| 404 | `NOT_FOUND` | Resource not found | Show "Not found" message |
| 409 | `CONFLICT` | Already processed | Refresh data, show warning |
| 422 | `VALIDATION_ERROR` | Invalid input | Show field errors |
| 423 | `ACCOUNT_LOCKED` | Too many login attempts | Show lockout message |
| 429 | `RATE_LIMIT_EXCEEDED` | Too many requests | Wait, then retry |
| 500 | `SERVER_ERROR` | Internal server error | Show generic error, retry |
| 503 | `SERVICE_UNAVAILABLE` | Maintenance mode | Show maintenance message |

### Error Code Reference

Common error codes returned in `error.code` field:

- `VALIDATION_ERROR`: Input validation failed
- `UNAUTHORIZED`: Missing or invalid token
- `FRANCHISE_NOT_OWNED`: User doesn't own the requested franchise
- `APPROVAL_ALREADY_PROCESSED`: Approval already approved/rejected
- `INSUFFICIENT_PERMISSIONS`: User lacks required permissions
- `NETWORK_ERROR`: Client-side network error
- `PARSE_ERROR`: Failed to parse API response

### Repository Error Handling Pattern

```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): Resource<T> {
    return try {
        val response = apiCall()
        if (response.success && response.data != null) {
            Resource.Success(response.data)
        } else {
            Resource.Error(
                message = response.error?.message ?: "Unknown error",
                errorCode = response.error?.code
            )
        }
    } catch (e: IOException) {
        Resource.Error("Network error. Please check your connection.", "NETWORK_ERROR")
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> Resource.Error("Session expired. Please login again.", "UNAUTHORIZED")
            403 -> Resource.Error("Access denied.", "FORBIDDEN")
            404 -> Resource.Error("Resource not found.", "NOT_FOUND")
            429 -> Resource.Error("Too many requests. Please try again later.", "RATE_LIMIT")
            else -> Resource.Error("Server error. Please try again.", "SERVER_ERROR")
        }
    } catch (e: Exception) {
        Resource.Error("Unexpected error: ${e.message}", "UNKNOWN_ERROR")
    }
}
```

---

## Retrofit Service Examples

### AuthApiService

```kotlin
interface AuthApiService {

    @POST("api/auth/mobile-login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>

    @POST("api/auth/refresh.php")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<RefreshTokenResponse>

    @DELETE("api/auth/logout.php")
    suspend fun logout(): ApiResponse<Unit>

    @POST("api/auth/password-change.php")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): ApiResponse<Unit>
}
```

### DashboardApiService

```kotlin
interface DashboardApiService {

    @GET("api/owners/dashboard-stats.php")
    suspend fun getDashboardStats(): ApiResponse<DashboardStatsDto>

    @GET("api/owners/franchises.php")
    suspend fun getFranchises(): ApiResponse<List<FranchiseDto>>
}
```

### ReportApiService

```kotlin
interface ReportApiService {

    @GET("api/owners/reports/daily-sales.php")
    suspend fun getDailySales(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("branch_id") branchId: Int? = null
    ): ApiResponse<DailySalesDto>

    @POST("api/owners/reports/sales-summary.php")
    suspend fun getSalesSummary(
        @Body request: ReportRequestDto
    ): ApiResponse<SalesSummaryDto>

    @GET("api/owners/reports/cash-flow.php")
    suspend fun getCashFlow(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): ApiResponse<CashFlowDto>

    @POST("api/owners/reports/distributor-directory.php")
    suspend fun getDistributorDirectory(
        @Body request: ReportRequestDto,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 25
    ): ApiResponse<DistributorDirectoryDto>
}
```

### ApprovalApiService

```kotlin
interface ApprovalApiService {

    @GET("api/owners/approvals/queue.php")
    suspend fun getApprovalQueue(): ApiResponse<ApprovalQueueDto>

    @GET("api/owners/approvals/expenses/list.php")
    suspend fun getExpenseApprovals(
        @Query("status") status: String = "pending",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 25
    ): ApiResponse<List<ExpenseApprovalDto>>

    @GET("api/owners/approvals/expenses/{id}.php")
    suspend fun getExpenseDetail(
        @Path("id") approvalId: Long
    ): ApiResponse<ExpenseDetailDto>

    @POST("api/owners/approvals/expenses/{id}/approve.php")
    suspend fun approveExpense(
        @Path("id") approvalId: Long,
        @Body request: ApprovalActionRequest
    ): ApiResponse<Unit>

    @POST("api/owners/approvals/expenses/{id}/reject.php")
    suspend fun rejectExpense(
        @Path("id") approvalId: Long,
        @Body request: ApprovalActionRequest
    ): ApiResponse<Unit>
}
```

---

## Pagination

### Request Parameters

```kotlin
data class PaginationParams(
    val page: Int = 1,          // 1-indexed
    val perPage: Int = 25       // Max 100
)
```

### Response Meta

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

### Pagination DTO

```kotlin
@Serializable
data class PaginationMeta(
    val page: Int,
    @SerialName("per_page") val perPage: Int,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("has_next") val hasNext: Boolean,
    @SerialName("has_prev") val hasPrev: Boolean
)
```

---

## Caching Strategy

### Cache TTL Values

| Data Type | Cache TTL | Rationale |
|-----------|-----------|-----------|
| Dashboard KPIs | 10 minutes | Time-sensitive, changes frequently |
| Report data | 30 minutes | Historical data, rarely changes for a given date range |
| Franchise list | 24 hours | Rarely changes |
| Approval list | 5 minutes | Time-sensitive, frequently updated |
| Profile data | 24 hours | Rarely changes |

### Stale-While-Revalidate Pattern

1. Check cache with TTL validation
2. If fresh cache exists, return immediately (fast path)
3. If stale cache exists, return it while fetching fresh data in background
4. If no cache exists, show loading indicator while fetching
5. On error with stale cache, show warning but keep stale data visible
6. On error without cache, show error state with retry

### Cache Invalidation Triggers

- **Manual refresh**: Pull-to-refresh bypasses cache
- **Franchise switch**: Clear all cached data for previous franchise
- **Approval action**: Invalidate approval list cache
- **Logout**: Clear all cached data

---

## Rate Limiting

### Limits

| Endpoint Category | Limit | Window |
|-------------------|-------|--------|
| Login | 5 requests | Per minute |
| Token refresh | 10 requests | Per minute |
| Password reset | 3 requests | Per minute |
| All authenticated | 60 requests | Per minute |

### Rate Limit Headers

```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 42
X-RateLimit-Reset: 1707351300
Retry-After: 18
```

### Handling Rate Limits

When receiving HTTP 429:

1. Read `Retry-After` header (seconds)
2. Show user-friendly message
3. Disable retry button for `Retry-After` duration
4. Automatically retry after cooldown

```kotlin
if (httpException.code() == 429) {
    val retryAfter = response.headers()["Retry-After"]?.toIntOrNull() ?: 60
    emit(Resource.Error(
        "Too many requests. Please wait $retryAfter seconds.",
        errorCode = "RATE_LIMIT_EXCEEDED",
        retryAfterSeconds = retryAfter
    ))
}
```

---

## Additional Resources

### Comprehensive API Specs
- `docs/android-app-owner/04_API_CONTRACT.md` - Complete API contract
- `docs/android-app-owner/api-contract/01-overview.md` - API conventions
- `docs/android-app-owner/api-contract/02-endpoints-auth.md` - Auth endpoints
- `docs/android-app-owner/api-contract/03-endpoints-dashboard-kpi.md` - Dashboard endpoints
- `docs/android-app-owner/api-contract/04-endpoints-reports.md` - All report endpoints
- `docs/android-app-owner/api-contract/05-endpoints-approvals.md` - Approval endpoints
- `docs/android-app-owner/api-contract/06-error-codes.md` - Complete error catalog

### Related Docs
- `CLAUDE.md` - AI development patterns
- `docs/DATABASE.md` - Room database schema
- `ARCHITECTURE.md` - Architecture overview

---

**Last Updated:** 2026-02-09
**Maintainer:** Development Team
