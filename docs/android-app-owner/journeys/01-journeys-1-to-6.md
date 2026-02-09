# User Journeys 1 to 6

**Parent:** [05_USER_JOURNEYS.md](../05_USER_JOURNEYS.md) | [All Docs](../README.md)
**Next:** [Journeys 7-12](02-journeys-7-to-12.md)
**Version:** 1.0
**Last Updated:** 2026-02-08

---

## Journey 1: First-Time Setup / Onboarding

**Persona:** Nalongo (new owner, 29, Nairobi -- single franchise, mid-range Android, 4G)
**Preconditions:** Owner has received login credentials via email. App installed from Google Play Store. Device has active internet connection. No prior app data exists on device.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Launch  │───>│  Welcome │───>│  Login   │───>│ Language │───>│Dashboard │
│   App    │    │  Screen  │    │  Screen  │    │  Select  │    │          │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
                                     │
                                     ▼ (error)
                                ┌──────────┐
                                │  Error   │
                                │  Toast   │
                                └──────────┘
```

### Steps

1. **Launch Screen** -- App shows splash screen with Dynapharm logo and tagline "Your Franchise Command Center". Splash displays for 2 seconds while the app initializes dependencies (Hilt, Room, DataStore).

2. **Welcome Screen** -- Three-slide onboarding carousel with swipe navigation and skip button:
   - Slide 1: "Your Franchise Dashboard" -- illustration of KPI cards
   - Slide 2: "Reports at Your Fingertips" -- illustration of report categories
   - Slide 3: "Approve Anywhere" -- illustration of approval workflow
   - "Get Started" button on the final slide advances to login.

3. **Login Screen** -- Email and password fields with:
   - Email field: keyboard type `textEmailAddress`, auto-lowercase
   - Password field: show/hide toggle (eye icon), keyboard type `textPassword`
   - "Forgot Password?" link below password field
   - "Log In" button (disabled until both fields non-empty)
   - Loading indicator replaces button text during API call

4. **Language Selection** -- After successful login, a bottom sheet presents 5 language options:
   - English (en) -- default highlighted
   - Francais (fr)
   - Arabic (ar) -- RTL layout
   - Kiswahili (sw)
   - Espanol (es)
   - "Continue" button saves preference to DataStore and applies locale

5. **Dashboard** -- Lands on main dashboard with 5 KPI cards loading for the owner's primary franchise. Cards show shimmer placeholders while API data loads. Franchise name and currency appear in top bar.

### Alternative Paths

- **Forgot Password:** Tap "Forgot Password?" link --> enter email on reset screen --> API call to `/api/auth/forgot-password.php` --> "Reset link sent to your email" success message --> return to login screen. If email not found, show "No account found with that email."

- **Invalid Credentials:** API returns 401 --> show inline error "Invalid email or password" below password field --> clear password field --> focus on password field for retry. After 5 consecutive failures, show "Too many attempts. Try again in 15 minutes."

- **Network Error:** API call times out or no connectivity detected --> show Snackbar "No internet connection. Check your network and try again." with "Retry" action button. Login button re-enables.

- **Not Owner Role:** API returns 403 with `role_mismatch` error code --> show dialog "This app is for franchise owners only. Please use the Distributor app or contact your administrator." with "OK" button that returns to login screen and clears fields.

- **Single Franchise:** If owner has exactly one franchise, skip franchise selection and load dashboard directly for that franchise.

- **Multiple Franchises:** If owner has multiple franchises, show franchise picker bottom sheet after language selection. Each franchise card shows: name, country flag, currency code. Tap to select primary franchise.

### Success Criteria

- User reaches dashboard within 30 seconds of entering valid credentials
- Language preference persists across app restarts
- JWT access token (15 min) and refresh token (30 days) stored in EncryptedSharedPreferences

---

## Journey 2: Login (Returning User)

**Persona:** James (multi-franchise owner, 52, Kampala -- 3 franchises, flagship Android, WiFi/5G)
**Preconditions:** Previously logged in successfully. JWT refresh token still valid (within 30-day window). App data exists on device (cached KPIs, franchise context).

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  Launch  │───>│ Biometric│───>│Dashboard │
│   App    │    │  Prompt  │    │ (cached) │
└──────────┘    └──────────┘    └──────────┘
                     │
                     ▼ (fail)
                ┌──────────┐
                │  Login   │
                │  Screen  │
                └──────────┘
```

### Steps

1. **Launch** -- App starts, shows splash for 1 second. System checks EncryptedSharedPreferences for stored JWT. If a valid refresh token exists (not expired), proceed to biometric prompt. If no token or expired, redirect to login screen.

2. **Biometric Prompt** -- If biometric authentication is enrolled on the device, show `BiometricPrompt` dialog:
   - Title: "Unlock Dynapharm"
   - Subtitle: "Use fingerprint or face to continue"
   - Negative button: "Use Password"
   - On success: proceed directly to dashboard
   - On failure (3 attempts): fall back to password login screen

3. **Dashboard** -- Show cached KPIs immediately from Room database (instant render, no loading spinner). Display last franchise context (e.g., "Dynapharm Uganda"). Trigger background refresh: access token refreshed silently via `/api/auth/refresh.php`, then KPI data refreshed via `/api/owners/dashboard-stats.php`. Updated values animate in when API responds.

### Alternative Paths

- **Biometric Not Enrolled:** If device has no biometric hardware or user has not enrolled fingerprint/face, skip biometric prompt entirely. JWT is still valid, so go straight to dashboard with cached data.

- **JWT Access Token Expired, Refresh Valid:** Transparent token refresh in background. Access token renewed via refresh endpoint. User sees no interruption -- dashboard loads from cache while token refreshes.

- **JWT Refresh Token Expired (30-day session):** Redirect to login screen with Snackbar message "Session expired. Please log in again." Email field pre-filled from DataStore. Clear all cached tokens.

- **Biometric Prompt Cancelled:** User taps "Use Password" --> navigate to login screen with email pre-filled.

- **Offline with Valid Session:** Show cached dashboard with persistent banner: "You are offline. Showing data from [date/time]." Pull-to-refresh shows "No internet connection" toast. All navigation works with cached data only.

- **App Killed and Restarted:** Same flow -- token checked on cold start, biometric prompted, cached data displayed.

### Success Criteria

- Time from app launch to dashboard visible: under 2 seconds (cached data)
- Background refresh completes within 5 seconds on stable connection
- Biometric authentication adds no more than 1 second to flow

---

## Journey 3: Dashboard KPI Review

**Persona:** James (checks KPIs between meetings, quick 30-second session)
**Preconditions:** Logged in with valid session. Franchise selected (currently viewing Dynapharm Uganda). Dashboard has cached data from previous session.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│Dashboard │───>│ Tap KPI  │───>│  Report  │
│  Load    │    │   Card   │    │  Screen  │
└──────────┘    └──────────┘    └──────────┘
      │
      ├───> Tap Quick Report Button ───> Report
      │
      └───> Tap Approval Badge ───> Approval Queue
```

### Steps

1. **Dashboard Loads** -- Five KPI cards render in a 2-column grid (last card full-width):
   - **Sales MTD:** Total sales for current month in local currency (e.g., "UGX 45,230,000"). Green up arrow or red down arrow with percentage vs. last month (e.g., "+12.3%").
   - **Cash Balance:** Aggregate cash/bank balance across all payment accounts. Trend indicator vs. previous month.
   - **Inventory Value:** Total inventory at local selling price. Trend indicator.
   - **Total BV:** Total Business Volume recognized this month (joined with `tbl_paid_receipts`). Trend indicator.
   - **Pending Approvals:** Count of pending items across all approval workflows (e.g., "7"). Red badge if count > 0. No trend -- just current count.

2. **Review Trends** -- Each KPI card shows a small trend indicator:
   - Green up-arrow with "+X.X%" if current > previous month
   - Red down-arrow with "-X.X%" if current < previous month
   - Gray dash with "0.0%" if unchanged
   - Trend calculated server-side in `dashboard-stats.php`

3. **Tap KPI Card** -- Each card is tappable and navigates to its detailed report:
   - Sales MTD --> Sales Summary report (Journey 5)
   - Cash Balance --> Cash Flow report (Journey 6)
   - Inventory Value --> Inventory Valuation report
   - Total BV --> Commission Report
   - Pending Approvals --> Approval Queue (Journey 7)

4. **Quick Report Buttons** -- Below KPI cards, a horizontally scrollable row of 6 quick-access buttons:
   - Daily Sales | Cash Flow | P&L | Inventory | Distributor Perf. | Expenses
   - Each button: icon + label, tap navigates to corresponding report screen

5. **Approval Summary Card** -- Below quick reports, a card summarizing pending approvals by type:
   - "Expenses: 3 | POs: 2 | Stock Transfers: 1 | Leave: 1"
   - Tap card --> navigates to approval queue with all categories

6. **Pull-to-Refresh** -- Swipe down on dashboard triggers:
   - Loading indicator appears at top
   - All 5 KPIs re-fetched from API
   - Cards animate updated values (CountUp animation for numbers)
   - "Updated just now" timestamp refreshes

### Alternative Paths

- **Offline:** Show cached KPIs with amber banner: "Offline -- Last updated 15 minutes ago." Pull-to-refresh shows "No connection. Showing cached data." toast. All KPI cards still tappable (navigate to cached reports).

- **API Error on Refresh:** Show cached data with Snackbar: "Could not refresh data. Showing last known values." Retry available via pull-to-refresh.

- **Zero Data (New Franchise):** KPI cards show "0" or "--" with informational text: "No sales data yet for this month."

### Success Criteria

- All 5 KPI cards render within 1 second (cached) or 3 seconds (API fetch)
- Pull-to-refresh completes within 5 seconds
- KPI tap-to-report navigation under 300ms

---

## Journey 4: Franchise Switching

**Persona:** James (owns 3 franchises: Dynapharm Uganda, Dynapharm Kenya, Dynapharm Tanzania)
**Preconditions:** Logged in. Currently viewing Dynapharm Uganda (UGX). All three franchises returned by `/api/owners/franchises.php`.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Tap Top  │───>│ Franchise│───>│  Select  │───>│Dashboard │
│ Bar Name │    │  Sheet   │    │  Kenya   │    │ Reloads  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### Steps

1. **Top Bar Display** -- The app bar shows the current franchise context: "Dynapharm Uganda (UGX)" with a dropdown chevron icon to the right. This is visible on every screen, not just the dashboard.

2. **Tap Franchise Name** -- Tapping the franchise name/chevron opens a modal bottom sheet that slides up from the bottom of the screen.

3. **Franchise List** -- The bottom sheet displays all franchises the owner has access to. Each row contains:
   - Country flag emoji (e.g., flag for Uganda, flag for Kenya, flag for Tanzania)
   - Franchise name (e.g., "Dynapharm Uganda")
   - Currency code (e.g., "UGX", "KES", "TZS")
   - Green checkmark on the currently active franchise
   - Rows are sorted alphabetically by franchise name

4. **Select Kenya** -- Owner taps "Dynapharm Kenya (KES)". Bottom sheet closes with slide-down animation. A full-screen loading overlay appears briefly with text "Switching to Dynapharm Kenya..."

5. **Dashboard Reloads** -- All data refreshes for the selected franchise:
   - Top bar updates to "Dynapharm Kenya (KES)"
   - JWT context updated (franchise_id in API headers)
   - Room cache invalidated for previous franchise scope
   - KPI cards show shimmer loading, then populate with Kenya data
   - All monetary values now display in KES

6. **Reports and Approvals Scoped** -- After switching, all subsequent navigation (reports, approvals, profile) operates within the Kenya franchise context. Report date filters reset to defaults. Approval queue shows Kenya-only pending items.

### Alternative Paths

- **Offline:** Franchise list loads from Room cache (populated on last sync). Tapping a different franchise shows dialog: "Switching franchises requires an internet connection to load fresh data." with "OK" button. User remains on current franchise.

- **API Error During Switch:** If the dashboard-stats API fails after switching, show error Snackbar: "Could not load data for Dynapharm Kenya. Please try again." Offer "Retry" action. If retry fails, offer to switch back to previous franchise.

- **Single Franchise Owner:** Top bar shows franchise name without dropdown chevron. Tapping does nothing (no bottom sheet). Esi (single franchise, Accra) never sees the switcher.

- **Franchise Access Revoked:** If API returns 403 for a franchise that was previously accessible, remove it from the local franchise list, show dialog: "Access to Dynapharm Tanzania has been revoked. Contact your administrator." Switch to first remaining franchise.

### Success Criteria

- Franchise list bottom sheet opens within 200ms
- Dashboard fully reloaded within 3 seconds of selection
- Currency formatting correct for switched franchise
- No data leakage between franchise contexts

---

## Journey 5: Viewing a Sales Report

**Persona:** Esi (single franchise owner, 38, Accra -- weekly performance reviews, mid-range Android, 4G)
**Preconditions:** Logged in. Franchise selected (Dynapharm Ghana, GHS). At least one month of sales data exists.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Reports  │───>│  Sales   │───>│  Daily   │───>│  Report  │
│   Tab    │    │ Category │    │  Sales   │    │  Data    │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                                      │
                                                      ├───> Share PDF
                                                      └───> Change dates
```

### Steps

1. **Reports Tab** -- Tap "Reports" in the bottom navigation bar (icon: bar chart). The reports screen displays 6 category cards in a vertical list:
   - Sales (7 reports)
   - Finance (8 reports)
   - Inventory (3 reports)
   - HR / Payroll (3 reports)
   - Distributors (5 reports)
   - Compliance (2 reports)
   - Each card shows category name, icon, and report count badge.

2. **Sales Category** -- Tap "Sales" card. Screen transitions to show 7 report options in a list:
   - Daily Sales
   - Sales Summary
   - Sales Trends
   - Sales by Product
   - Top Sellers
   - Product Performance
   - Commission Report

3. **Daily Sales** -- Tap "Daily Sales". Report screen loads with:
   - Title bar: "Daily Sales" with back arrow and share icon
   - Date filter row at top
   - Summary totals section
   - Data table below

4. **Date Filter** -- Default date range: first day of current month to today. Quick preset buttons displayed horizontally:
   - **Today** | **This Week** | **This Month** (selected by default) | **Last Month** | **This Quarter** | **This Year**
   - Custom date range: tap "Custom" to show start/end date pickers
   - Changing any preset triggers immediate data reload with loading indicator

5. **Data Loads** -- Report content appears in two sections:
   - **Summary Row:** Total Sales (GHS), Total Qty, Total BV, Invoice Count -- displayed in 4 mini-cards at top
   - **Data Table:** Scrollable table with columns: Date, Product, Qty, Unit Price, Total, BV. Sortable by any column header tap. Alternating row colors for readability.

6. **Quick Date Switch** -- Esi taps "Last Month" preset button. Button highlights, current month deselects. Loading spinner appears in table area. Last month's data replaces current view. Summary totals update.

7. **Share/Export** -- Tap share icon (top-right). Bottom sheet offers:
   - "Share as PDF" -- generates PDF via API, opens Android share sheet (WhatsApp, email, etc.)
   - "Copy Link" -- copies deep link to report (future feature)
   - PDF includes: franchise header, report title, date range, summary, full table, generated timestamp

### Alternative Paths

- **Offline:** If cached report data exists for the requested date range, show it with banner: "Offline -- Showing cached data from [timestamp]." If no cached data, show empty state: "No cached data available. Connect to the internet to load this report." with illustration.

- **No Data for Period:** API returns empty dataset. Show friendly empty state: "No sales data for the selected period." with illustration of empty chart. Suggest: "Try a different date range."

- **Large Dataset:** For date ranges spanning months, API returns paginated results (50 rows per page). "Load More" button at bottom of table. Scroll-to-top FAB appears after scrolling down 3+ pages.

- **API Timeout:** Show error state: "Report took too long to load. This may happen with large date ranges." with "Retry" and "Try Smaller Range" buttons.

### Success Criteria

- Report data loads within 3 seconds for a single month
- Date preset switch refreshes data within 2 seconds
- PDF generation and share sheet open within 5 seconds
- Table scrolls at 60fps with 200+ rows

---

## Journey 6: Viewing a Finance Report (P&L)

**Persona:** Esi (quarterly financial review, preparing for board meeting)
**Preconditions:** Logged in. Franchise selected (Dynapharm Ghana, GHS). Multiple months of financial data exist including sales revenue, cost of goods, and operating expenses.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Reports  │───>│ Finance  │───>│   P&L    │───>│  Report  │
│   Tab    │    │ Category │    │  Report  │    │  Data    │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### Steps

1. **Reports Tab** -- Tap "Reports" in bottom navigation. The 6 report categories display. Finance card shows "(8 reports)" badge.

2. **Finance Category** -- Tap "Finance" card. Eight report options appear:
   - Profit & Loss
   - Cash Flow Statement
   - Balance Sheet
   - Expense Report
   - Account Reconciliation
   - Employee Debts
   - Debtors Report
   - Inventory Valuation

3. **P&L Report** -- Tap "Profit & Loss". Report screen loads with:
   - Title: "Profit & Loss Statement"
   - Date range filter (same preset buttons as Journey 5)
   - Hierarchical financial data below

4. **Select "This Quarter"** -- Esi taps "This Quarter" preset. Loading indicator appears. API fetches P&L data for current quarter. Data renders in structured sections.

5. **Review P&L Structure** -- Report displays in collapsible sections with indentation:
   - **Revenue**
     - Total Sales (with breakdown by product category if expandable)
     - Other Income
     - **Total Revenue** (bold, highlighted row)
   - **Cost of Goods Sold**
     - Product Costs
     - **Total COGS** (bold row)
   - **Gross Profit** (calculated: Revenue - COGS, highlighted in green if positive)
   - **Operating Expenses**
     - Salaries & Wages
     - Rent & Utilities
     - Marketing
     - Transport
     - Other Expenses
     - **Total Operating Expenses** (bold row)
   - **Net Profit / Loss** (final row, large font, green if profit, red if loss)

6. **Compare Periods** -- Esi taps "Last Quarter" preset to compare. Data reloads with last quarter's figures. She mentally compares, or (future feature) side-by-side comparison toggle.

7. **Export for Board Meeting** -- Esi taps share icon --> "Share as PDF". PDF generated with professional formatting: franchise letterhead, P&L title, date range, all sections with proper indentation and totals, page break before footer. Shares via email to board members.

### Alternative Paths

- **Balance Sheet Report:** Different from P&L. Uses single `as_of_date` parameter instead of date range. Sections: Assets (NBV of active assets, inventory at local price, receivables, cash/bank), Liabilities (unpaid expenses, inventory at cost), Equity (assets minus liabilities). Date display: `d M Y` format per CLAUDE.md standard.

- **Cash Flow Report:** Similar to P&L but with account selector dropdown. Owner can filter by specific payment account (e.g., "Main Bank Account", "Petty Cash") or view "All Accounts" aggregate. Shows: Opening Balance, Inflows (sales receipts, other income), Outflows (expenses, purchases), Closing Balance.

- **Offline:** Show cached P&L if previously viewed for same period. Banner: "Offline -- Data from [cached timestamp]." If no cache, show offline empty state.

- **No Financial Data:** "No financial data available for the selected period. Ensure transactions have been recorded in the web portal."

- **Expense Report:** Navigates to a table view: Date, Reference, Category, Vendor, Amount, Status. Filterable by expense category and status (All, Pending, Approved, Paid).

### Success Criteria

- P&L loads within 3 seconds for a single quarter
- Section collapse/expand animates at 60fps
- PDF export matches web portal P&L format
- Currency formatting consistent with franchise locale (GHS for Ghana)

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial journeys 1-6 |
