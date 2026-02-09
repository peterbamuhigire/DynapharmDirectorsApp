# Phase 1 -- Section 10: Verification Checklist

**[Back to Phase 1 README](./README.md)** | **Package:** `com.dynapharm.owner`

---

## 1. Overview

This document is the Phase 1 acceptance gate. Every item must pass before work begins on Phase 2. The checklist covers backend API verification, Android build verification, feature verification, and non-functional requirements. A sign-off section at the end formalizes completion.

---

## 2. Backend Verification (curl Commands)

Run these against the development server (`http://localhost/DMS_web/`). Each command must return the expected result.

### 2.1 Login -- Valid Owner Credentials

```bash
curl -s -X POST http://localhost/DMS_web/api/auth/mobile-login.php \
  -H "Content-Type: application/json" \
  -d '{
    "username": "owner@example.com",
    "password": "OwnerPass123",
    "device_id": "verify-device-001",
    "device_name": "Verification Test",
    "user_type": "owner"
  }' | jq .
```

**Expected:**
- HTTP 200
- `success: true`
- `data.access_token` -- non-empty JWT string
- `data.refresh_token` -- non-empty opaque string
- `data.expires_in` -- `900` (15 minutes)
- `data.user.franchise_ids` -- array with at least 1 franchise ID
- `data.user.full_name` -- owner's display name

```
[ ] PASS  [ ] FAIL  Notes: ________________________
```

### 2.2 Dashboard Stats -- With Bearer Token

```bash
# Replace <ACCESS_TOKEN> with the token from step 2.1
curl -s -X GET "http://localhost/DMS_web/api/owners/dashboard-stats.php" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Accept: application/json" | jq .
```

**Expected:**
- HTTP 200
- `success: true`
- `data` contains: `total_sales_mtd`, `total_cash_balance`, `inventory_value`, `total_bv_mtd`, `pending_approvals`
- All numeric values are numbers (not strings)

```
[ ] PASS  [ ] FAIL  Notes: ________________________
```

### 2.3 Token Refresh -- Valid Refresh Token

```bash
# Replace <REFRESH_TOKEN> with the token from step 2.1
curl -s -X POST http://localhost/DMS_web/api/auth/mobile-refresh.php \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "<REFRESH_TOKEN>"
  }' | jq .
```

**Expected:**
- HTTP 200
- `success: true`
- `data.access_token` -- new JWT (different from original)
- `data.refresh_token` -- new refresh token (rotation)
- `data.expires_in` -- `900`

```
[ ] PASS  [ ] FAIL  Notes: ________________________
```

### 2.4 Breach Detection -- Revoked Refresh Token Reuse

```bash
# Use the OLD refresh token from step 2.1 (already consumed in step 2.3)
curl -s -X POST http://localhost/DMS_web/api/auth/mobile-refresh.php \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "<OLD_REFRESH_TOKEN>"
  }' | jq .
```

**Expected:**
- HTTP 401
- `success: false`
- `message` contains "Invalid" or "expired" or "revoked"

```
[ ] PASS  [ ] FAIL  Notes: ________________________
```

### 2.5 Invalid Token -- Expired or Tampered JWT

```bash
# Use a deliberately invalid JWT
curl -s -X GET "http://localhost/DMS_web/api/owners/dashboard-stats.php" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature" \
  -H "Accept: application/json" | jq .
```

**Expected:**
- HTTP 401
- `success: false`

```
[ ] PASS  [ ] FAIL  Notes: ________________________
```

### 2.6 Wrong User Type -- Distributor Attempting Owner Login

```bash
curl -s -X POST http://localhost/DMS_web/api/auth/mobile-login.php \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor@example.com",
    "password": "DistPass123",
    "device_id": "verify-device-002",
    "device_name": "Wrong Type Test",
    "user_type": "owner"
  }' | jq .
```

**Expected:**
- HTTP 403
- `success: false`
- `message` contains "owner" or "access denied" or "not authorized"

```
[ ] PASS  [ ] FAIL  Notes: ________________________
```

### 2.7 Logout -- Token Revocation

```bash
# Use a valid access token from step 2.3
curl -s -X POST http://localhost/DMS_web/api/auth/mobile-logout.php \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" | jq .
```

**Expected:**
- HTTP 200
- `success: true`
- Subsequent API calls with the same token return 401

**Post-logout verification:**

```bash
# This should now fail
curl -s -X GET "http://localhost/DMS_web/api/owners/dashboard-stats.php" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Accept: application/json" | jq .
```

**Expected:** HTTP 401

```
[ ] PASS  [ ] FAIL  Notes: ________________________
```

---

## 3. Android Build Verification

### 3.1 Compilation

```bash
cd /path/to/DynapharmOwner
./gradlew assembleDevDebug
```

| # | Check | Command / Action | Expected | Status |
|---|-------|-----------------|----------|--------|
| 1 | Dev debug APK builds | `./gradlew assembleDevDebug` | BUILD SUCCESSFUL | `[ ] PASS  [ ] FAIL` |
| 2 | All unit tests pass | `./gradlew testDevDebugUnitTest` | 40+ tests pass, 0 failures | `[ ] PASS  [ ] FAIL` |
| 3 | Lint clean | `./gradlew lint` | 0 errors (warnings acceptable) | `[ ] PASS  [ ] FAIL` |
| 4 | APK installs on emulator | `adb install app/build/outputs/apk/dev/debug/*.apk` | Success | `[ ] PASS  [ ] FAIL` |

### 3.2 Test Results Detail

```bash
./gradlew testDevDebugUnitTest --info 2>&1 | tail -20
```

Record the output:

- Total tests run: ___
- Passed: ___
- Failed: ___
- Skipped: ___

```
[ ] All 40+ tests pass  [ ] Some failures (list below)
Failures: ________________________________________________
```

---

## 4. Feature Verification -- Login Screen

| # | Check | Steps | Expected | Status |
|---|-------|-------|----------|--------|
| 1 | Login screen renders | Launch app on emulator | Login screen visible with logo, username field, password field, login button | `[ ] PASS  [ ] FAIL` |
| 2 | Valid login navigates to Dashboard | Enter valid owner credentials, tap Login | Loading indicator shown, then Dashboard screen appears | `[ ] PASS  [ ] FAIL` |
| 3 | Invalid credentials show error | Enter wrong password, tap Login | Error dialog/banner: "Invalid credentials" | `[ ] PASS  [ ] FAIL` |
| 4 | Empty fields show validation | Tap Login with empty fields | Validation message: fields required | `[ ] PASS  [ ] FAIL` |
| 5 | Password visibility toggle | Tap eye icon on password field | Password text toggles between hidden/visible | `[ ] PASS  [ ] FAIL` |
| 6 | Network error handling | Disconnect network, tap Login | Error message about network/connection | `[ ] PASS  [ ] FAIL` |

---

## 5. Feature Verification -- Dashboard

| # | Check | Steps | Expected | Status |
|---|-------|-------|----------|--------|
| 1 | Dashboard shows 5 KPI cards | Login successfully | 5 cards visible: Sales MTD, Cash Balance, Inventory Value, BV MTD, Pending Approvals | `[ ] PASS  [ ] FAIL` |
| 2 | KPI values match API data | Compare card values with curl response from 2.2 | All 5 values match | `[ ] PASS  [ ] FAIL` |
| 3 | Pull-to-refresh works | Pull down on Dashboard | Refresh indicator shown, data reloads | `[ ] PASS  [ ] FAIL` |
| 4 | Offline cache fallback | Kill network after loading, pull to refresh | Cached data shown, "Last updated: X" banner visible | `[ ] PASS  [ ] FAIL` |
| 5 | Auto-refresh fires | Wait 60+ seconds on Dashboard | Data silently refreshes (check logcat) | `[ ] PASS  [ ] FAIL` |

---

## 6. Feature Verification -- Navigation & Shell

| # | Check | Steps | Expected | Status |
|---|-------|-------|----------|--------|
| 1 | 5 bottom tabs visible | Login to Dashboard | Bottom bar shows: Dashboard, Reports, Approvals, Franchises, More | `[ ] PASS  [ ] FAIL` |
| 2 | Non-Dashboard tabs show placeholder | Tap Reports tab | "Coming Soon" placeholder screen with icon | `[ ] PASS  [ ] FAIL` |
| 3 | Non-Dashboard tabs show placeholder | Tap Approvals tab | "Coming Soon" placeholder screen with icon | `[ ] PASS  [ ] FAIL` |
| 4 | Non-Dashboard tabs show placeholder | Tap Franchises tab | "Coming Soon" placeholder screen with icon | `[ ] PASS  [ ] FAIL` |
| 5 | More tab shows menu | Tap More tab | "Coming Soon" placeholder or settings menu | `[ ] PASS  [ ] FAIL` |
| 6 | Tab selection persists | Tap Reports, then Dashboard, then Reports | Reports tab still shows placeholder (no re-creation) | `[ ] PASS  [ ] FAIL` |

---

## 7. Feature Verification -- Session & Logout

| # | Check | Steps | Expected | Status |
|---|-------|-------|----------|--------|
| 1 | Logout clears tokens | Tap logout (in More or menu) | Returns to Login screen | `[ ] PASS  [ ] FAIL` |
| 2 | Logout confirmation | Tap logout | Confirmation dialog before actual logout | `[ ] PASS  [ ] FAIL` |
| 3 | App kill + reopen auto-login | Login, kill app, reopen | If refresh token valid: auto-login to Dashboard (no login screen) | `[ ] PASS  [ ] FAIL` |
| 4 | Expired refresh forces re-login | Simulate expired refresh (or wait 30 days) | Login screen shown | `[ ] PASS  [ ] FAIL` |

---

## 8. Non-Functional Verification

| # | Check | Steps | Expected | Status |
|---|-------|-------|----------|--------|
| 1 | Dark mode toggle | Change device to dark mode (Settings > Display) | App switches to dark theme, all text readable | `[ ] PASS  [ ] FAIL` |
| 2 | RTL layout (Arabic) | Change device language to Arabic | Layout mirrors (RTL), Arabic text renders correctly | `[ ] PASS  [ ] FAIL` |
| 3 | App does not crash on rotation | Rotate device during Dashboard load | No crash, data persists | `[ ] PASS  [ ] FAIL` |
| 4 | Memory -- no major leaks | Run with Android Studio Profiler for 5 min | No continuous memory growth | `[ ] PASS  [ ] FAIL` |
| 5 | Cold start time | Measure with `adb shell am start-activity` | Under 2 seconds on mid-range device | `[ ] PASS  [ ] FAIL` |
| 6 | APK size | Check `app/build/outputs/apk/dev/debug/` | Under 15 MB (debug); under 10 MB (release) | `[ ] PASS  [ ] FAIL` |

---

## 9. Phase 1 to Phase 2 Gate Criteria

**All of the following must be true before Phase 2 work begins:**

| # | Gate Criterion | Required | Status |
|---|---------------|----------|--------|
| 1 | All 7 backend curl tests pass (Section 2) | Mandatory | `[ ] PASS` |
| 2 | APK compiles without errors | Mandatory | `[ ] PASS` |
| 3 | All 40+ unit tests pass with 0 failures | Mandatory | `[ ] PASS` |
| 4 | Lint reports 0 errors | Mandatory | `[ ] PASS` |
| 5 | Login screen renders and works end-to-end | Mandatory | `[ ] PASS` |
| 6 | Dashboard shows 5 KPI cards with real data | Mandatory | `[ ] PASS` |
| 7 | Pull-to-refresh updates Dashboard data | Mandatory | `[ ] PASS` |
| 8 | Offline cache fallback works | Mandatory | `[ ] PASS` |
| 9 | All 5 bottom tabs are visible | Mandatory | `[ ] PASS` |
| 10 | Logout clears tokens and returns to Login | Mandatory | `[ ] PASS` |
| 11 | App kill + reopen auto-login works | Mandatory | `[ ] PASS` |
| 12 | Dark mode does not break layout | Mandatory | `[ ] PASS` |
| 13 | RTL layout renders correctly for Arabic | Mandatory | `[ ] PASS` |
| 14 | No crash on device rotation | Mandatory | `[ ] PASS` |

**Gate result:** `[ ] ALL 14 CRITERIA MET -- PROCEED TO PHASE 2` or `[ ] BLOCKED -- See failures`

---

## 10. Known Issues and Workarounds

Document any issues discovered during verification that are not blockers but should be tracked:

| # | Issue | Severity | Workaround | Ticket |
|---|-------|----------|------------|--------|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

---

## 11. Sign-Off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Developer | | | |
| QA / Tester | | | |
| Tech Lead | | | |
| Product Owner | | | |

### Verification Environment

| Field | Value |
|-------|-------|
| Android Studio version | |
| Emulator API level | |
| Physical device (if used) | |
| Backend server URL | `http://localhost/DMS_web/` |
| Database | `dynapharm_web` (MySQL 9.1) |
| PHP version | 8.2+ |
| Test date | |
| APK version | 1.0.0 (versionCode 1) |
| Git commit hash | |
| Branch | |

---

## 12. What Phase 2 Unlocks

Once all gate criteria are met, Phase 2 work proceeds in this order:

| Phase | Feature | Depends On |
|-------|---------|-----------|
| 2a | Reports Tab | Dashboard data layer, Room caching, auth interceptor |
| 2b | Approvals Tab | Auth + API layer, push notification infra |
| 2c | Franchise Switching | Auth token with franchise_ids, dashboard reload |
| 2d | More Tab Features | Profile API, settings DataStore |

See [../phase-2-roadmap.md](../phase-2-roadmap.md) for the full Phase 2+ plan.

---

## 13. Cross-References

| Topic | Document |
|-------|----------|
| Testing details and example code | [09-testing.md](09-testing.md) |
| Core infrastructure | [03-core-infrastructure.md](03-core-infrastructure.md) |
| Authentication feature | [04-authentication-feature.md](04-authentication-feature.md) |
| Dashboard feature | [05-dashboard-feature.md](05-dashboard-feature.md) |
| API contract: Auth endpoints | [../api-contract/02-endpoints-auth.md](../api-contract/02-endpoints-auth.md) |
| API contract: Dashboard endpoint | [../api-contract/03-endpoints-dashboard-kpi.md](../api-contract/03-endpoints-dashboard-kpi.md) |
| Phase 2+ roadmap | [../phase-2-roadmap.md](../phase-2-roadmap.md) |
