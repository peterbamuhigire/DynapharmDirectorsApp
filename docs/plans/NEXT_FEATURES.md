# Next Features - Priority Roadmap

**Last Updated:** 2026-02-10
**Project:** Dynapharm Owner Hub (Android)

---

## ✅ Recently Completed (2026-02-10)

### Real Authentication & Login Flow ✓
**Completed:** 2026-02-10
**Effort:** 1 day
**Summary:**
- Implemented real database authentication (no mock data)
- Fixed password hash verification (`32-char-salt + bcrypt` format)
- Added username/email flexible login (backend accepts both)
- Implemented SweetAlert-style Material 3 error dialogs
- Added logout button in TopAppBar (always visible)
- Backend schema updated: `franchise_id` now nullable for super_admin/owner users
- Fixed coroutine race conditions (removed unnecessary `withContext`, `runBlocking`)
- Established MEMORY.md with critical patterns and learnings

**Key Files:**
- `C:\wamp64\www\dms_web\api\auth\mobile-login.php`
- `app/src/main/kotlin/com/dynapharm/owner/presentation/screens/auth/LoginScreen.kt`
- `app/src/main/kotlin/com/dynapharm/owner/presentation/screens/home/HomeScreen.kt`

---

## 🔴 CRITICAL PRIORITY

### 1. Dashboard Data Loading (Real KPIs)
**Why Critical:** Dashboard currently shows placeholder data. Users need real metrics immediately after login.

**Effort:** 2-3 days

**Start Point:** `app/src/main/kotlin/com/dynapharm/owner/data/repository/DashboardRepositoryImpl.kt`

**Requirements:**
- Verify `GET /api/owners/dashboard-stats.php` endpoint works
- Implement stale-while-revalidate caching with 10-minute TTL
- Add proper error handling for network failures
- Show stale data banner when cached data is old
- Pull-to-refresh functionality (already UI exists)
- Test with real franchise data from database

**Backend Dependencies:**
- `api/owners/dashboard-stats.php` endpoint (verify exists and returns correct format)
- Requires `X-Franchise-ID` header (already implemented via `FranchiseContextInterceptor`)

**Acceptance Criteria:**
- Dashboard loads real KPI data after login
- Cached data shows when offline
- Stale banner appears after 10 minutes
- Pull-to-refresh updates data
- Error states handled gracefully

---

## 🟠 HIGH PRIORITY

### 2. Franchise Switching UI
**Why High Priority:** Owners with multiple franchises need to switch context easily.

**Effort:** 2 days

**Start Point:** `app/src/main/kotlin/com/dynapharm/owner/presentation/common/ActiveFranchiseBanner.kt`

**Requirements:**
- Create `ActiveFranchiseBanner` component showing current franchise
- Add "Change" button that opens franchise selector dialog
- Implement `FranchiseSelector` dropdown component
- Store selected franchise in `FranchiseManager`
- Clear dashboard cache when franchise changes
- Auto-reload dashboard data after switch

**UI Location:** Above dashboard KPI cards, below screen title

**Design:** Subtle blue (tertiaryContainer) card with store icon, franchise name, branch count, and "Change" button

**Acceptance Criteria:**
- Banner shows active franchise name and branch count
- Clicking "Change" opens dropdown with all franchises
- Selecting new franchise:
  - Updates `FranchiseManager`
  - Clears dashboard cache
  - Reloads dashboard with new franchise data
  - Banner updates to show new franchise

### 3. App Icon and Branding
**Why High Priority:** Currently uses default Android icon. Needs professional branding.

**Effort:** 1 day

**Start Point:** `app/src/main/res/mipmap-*/` (currently has placeholders)

**Requirements:**
- Design/obtain Dynapharm Director icon (red/gold theme)
- Generate all density variants (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- Create adaptive icon with foreground/background layers
- Update `ic_launcher` and `ic_launcher_round`
- Test on various Android versions and launchers

**Resources:**
- Use existing Dynapharm branding colors
- Consider using shield/document icon metaphor for "Director" theme

---

## 🟡 MEDIUM PRIORITY

### 4. Profile Screen
**Effort:** 2 days

**Start Point:** Create `app/src/main/kotlin/com/dynapharm/owner/presentation/screens/profile/ProfileScreen.kt`

**Requirements:**
- Display user info (name, email, phone, role)
- Show app version and build info
- List assigned franchises
- Settings section (notifications, language, theme)
- Logout button (redundant with TopAppBar but good UX)
- About section (app description, terms, privacy policy)

**Acceptance Criteria:**
- Profile accessible from navigation (add 5th tab or menu)
- Shows current user data from `AuthRepository.getCurrentUser()`
- Settings persist in DataStore
- Logout works same as TopAppBar button

### 5. Reports Tab - Daily Sales Report
**Effort:** 3-4 days

**Start Point:** `app/src/main/kotlin/com/dynapharm/owner/presentation/screens/reports/ReportsScreen.kt`

**Requirements:**
- Replace "Coming Soon" placeholder
- Date range picker (Material 3 DateRangePicker)
- API integration: `GET /api/owners/reports/daily-sales.php`
- Table/chart toggle view
- Export to PDF button (use Android print framework)
- Cache reports for 30 minutes
- Offline support with stale data

**Backend Dependencies:**
- Verify `/api/owners/reports/daily-sales.php` endpoint exists
- Document expected request/response format

**Acceptance Criteria:**
- User selects date range and fetches report
- Data displays in table or chart format
- Export generates PDF
- Cached reports load instantly
- Offline shows last cached version

### 6. Approvals Tab - Expense Approvals
**Effort:** 3 days

**Start Point:** `app/src/main/kotlin/com/dynapharm/owner/presentation/screens/approvals/ApprovalsScreen.kt`

**Requirements:**
- Replace "Coming Soon" placeholder
- List pending expense approval requests
- Detail screen showing full expense info + receipt image
- Approve/Reject buttons with comment field
- Badge count on Approvals tab icon
- Push notifications for new approval requests (Phase 2.5)
- Offline approval queue (actions sync when online)

**Backend Dependencies:**
- `GET /api/owners/approvals/expense-list.php`
- `POST /api/owners/approvals/expense-approve.php`
- `POST /api/owners/approvals/expense-reject.php`

**Acceptance Criteria:**
- Pending approvals load and display
- User can view details and receipt
- Approve/Reject actions work with comments
- Badge shows pending count
- Offline actions queue and sync later

---

## 🟢 LOW PRIORITY (Future Enhancements)

### 7. Biometric Authentication (Quick Re-auth)
**Effort:** 1 day
**Why:** Convenience for returning users
**Pattern:** Use BiometricPrompt API for fingerprint/face unlock after initial login

### 8. Offline Queue System (WorkManager)
**Effort:** 2 days
**Why:** Approve requests while offline, sync when connection restored
**Pattern:** Room `sync_queue` table + WorkManager periodic sync worker

### 9. Multi-language Support
**Effort:** 3 days
**Languages:** English (default), French, Swahili, Arabic, Spanish
**Pattern:** String resources + locale switching

### 10. Dark Theme
**Effort:** 1 day
**Pattern:** Material 3 dynamic color scheme + system theme detection

---

## Recommended Next Session Plan

**Session Focus:** Dashboard Real Data Integration

**Order:**
1. Verify backend endpoint works (`dashboard-stats.php`)
2. Test API response format matches DTO expectations
3. Implement repository caching logic
4. Add error handling and offline support
5. Test with real franchise data
6. Add stale data banner logic

**Files to Touch:**
- `DashboardRepositoryImpl.kt`
- `DashboardViewModel.kt`
- `DashboardScreen.kt` (add stale banner)
- Create `StaleDataBanner.kt` component

**Estimated Time:** 2-3 hours for basic implementation, 1 hour for testing/polish

---

## Notes

- All features follow Clean Architecture (Domain → Data → Presentation)
- Material 3 design system enforced throughout
- Security: EncryptedSharedPreferences for sensitive data
- Offline-first: Stale-while-revalidate caching pattern
- No mock data allowed - always use real database queries

**Documentation References:**
- Architecture: `ARCHITECTURE.md`
- API Contracts: `docs/API.md`
- Database Schema: `docs/DATABASE.md`
- Development Patterns: `CLAUDE.md`
- Planning Docs: `docs/android-app-owner/` (PRD, SRS, SDS)
