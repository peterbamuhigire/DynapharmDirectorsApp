# User Journeys 7 to 12

**Parent:** [05_USER_JOURNEYS.md](../05_USER_JOURNEYS.md) | [All Docs](../README.md)
**Previous:** [Journeys 1-6](01-journeys-1-to-6.md)
**Version:** 1.0 | **Last Updated:** 2026-02-08

---

## Journey 7: Expense Approval Workflow

**Persona:** James (multi-franchise owner, 52, Kampala -- approving expenses while traveling)
**Preconditions:** Logged in. Franchise selected (Dynapharm Uganda). Pending expenses exist. Has `APPROVE_EXPENSES` permission.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│Approvals │───>│ Expenses │───>│  Detail  │───>│ Approve  │───>│ Updated  │
│   Tab    │    │   List   │    │   View   │    │  Dialog  │    │   List   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
                                                      │
                                                      ▼ (reject)
                                                ┌──────────┐
                                                │  Reject  │
                                                │  Dialog  │
                                                │ (notes   │
                                                │ required)│
                                                └──────────┘
```

### Steps

1. **Approvals Tab** -- Tap "Approvals" in bottom nav (icon: clipboard-check). Red badge shows total pending count (e.g., "7"). Screen shows category cards: Expenses (3), Purchase Orders (2), Stock Transfers (1), Stock Adjustments (0), Payroll (0), Leave Requests (1), Asset Depreciation (0). Only categories with pending items show badges.

2. **Expense List** -- Tap "Expenses (3 Pending)". List loads with status filter chips: **Pending** (default) | Approved | Rejected | All. Optional date range filter collapsed by default. Cards sorted newest first.

3. **Expense Card** -- Each card shows: reference (e.g., "EXP-2026-0045"), title, amount formatted with franchise currency (e.g., "UGX 1,250,000"), requester name, date submitted, category chip. Left border color: amber=pending, green=approved, red=rejected.

4. **Detail View** -- Tap card for full details: header (reference, status, date), summary (title, amount in large font, category, vendor), description text, line items table (item, qty, unit price, subtotal), attachment thumbnails (tap for full-size with pinch-to-zoom), requester info (name, role, branch). Two action buttons pinned at bottom: green "Approve" and red "Reject".

5. **Approve** -- Tap "Approve". Confirmation dialog: title "Approve Expense?", body showing reference and amount, optional notes text field, "Cancel" and "Approve" buttons. On confirm: API call `PUT /api/owners/approvals/expenses.php` with `{action: "approve", notes: "..."}`. Success toast with checkmark. Return to list. Badge decreases.

6. **Reject** -- Tap "Reject". Dialog requires rejection notes (min 10 characters). "Reject" button disabled until notes entered. On confirm: API call with `{action: "reject", notes: "..."}`. Success toast. Return to list.

7. **List Updates** -- Optimistic UI removes item from pending list immediately. Badge count decreases. Empty list shows: "All caught up! No pending expenses." Pull-to-refresh syncs with server.

### Alternative Paths

- **Offline:** Approval/rejection queued locally in Room. Snackbar: "Action queued. Will sync when back online." WorkManager syncs on reconnect. If 409 conflict on sync, notification: "Expense was already processed."
- **Already Processed (409):** Dialog: "This expense was already approved by [other user]." Refreshes list.
- **Swipe Actions:** Swipe right to quick-approve (with confirmation), swipe left to quick-reject (opens notes dialog). Colored background with icon during swipe.
- **Large Amount Warning:** Amounts exceeding franchise threshold trigger amber warning before approval.

### Success Criteria

- List loads within 2 seconds; approve/reject API within 3 seconds
- Optimistic UI update under 100ms; offline sync within 30 seconds of reconnect

---

## Journey 8: Purchase Order Approval

**Persona:** Omar (regional director, 45, Casablanca -- reviews POs across 5 franchises, tablet, WiFi)
**Preconditions:** Logged in. Viewing Dynapharm Morocco. Pending POs exist. Has `APPROVE_PURCHASE_ORDERS` permission.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Switch   │───>│Approvals │───>│ PO List  │───>│ PO Detail│───>│ Approve/ │
│Franchise │    │   Tab    │    │          │    │+ Items   │    │  Reject  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### Steps

1. **Franchise Context** -- Omar starts on Dynapharm Morocco (MAD). He reviews POs for each franchise sequentially.

2. **PO List** -- From Approvals tab, tap "Purchase Orders (4 Pending)". Each PO card shows: PO number (e.g., "PO-2026-0012"), supplier name, total amount (formatted with currency), expected delivery date, line item count, requester name. Sorted by submission date.

3. **PO Detail** -- Tap card. Detail screen shows: header (PO number, supplier with contact info, dates), line items table (Product, SKU, Qty, Unit Price, Line Total) with grand total in bold footer, procurement notes, requester info. If total exceeds budget threshold, amber warning banner at top.

4. **Approve PO** -- Tap "Approve PO". Confirmation dialog shows PO number, total amount, and item count. Optional notes. API call on confirm. Success toast: "Purchase order approved."

5. **Reject PO** -- Same pattern as expense rejection: mandatory notes (min 10 chars). Common reasons: quantities too high, price not competitive, budget exceeded.

6. **Next Franchise** -- After clearing Morocco's queue, Omar switches to "Dynapharm Senegal (XOF)" via top bar and repeats.

### Alternative Paths

- **Offline:** Same offline queue pattern as Journey 7.
- **Partial Approval (v2.0):** Future feature to approve/reject individual line items.
- **Supplier Detail:** Tap supplier name for contact bottom sheet. Long-press PO card for PDF preview.

### Success Criteria

- PO detail with 20+ line items loads within 3 seconds at 60fps scroll
- Omar can clear 5 franchises' PO queues in under 10 minutes

---

## Journey 9: Viewing Distributor Performance Report

**Persona:** Esi (single franchise owner, 38, Accra -- tracking top distributors monthly)
**Preconditions:** Logged in. Franchise selected (Dynapharm Ghana, GHS). Distributor sales data exists.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Reports  │───>│Distributors│──>│  Dist.   │───>│  Report  │
│   Tab    │    │ Category  │   │  Perf.   │    │   Data   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### Steps

1. **Navigate** -- Reports tab --> Distributors category (5 reports) --> tap "Distributor Performance".

2. **Report Screen** -- Loads with: title, date range filter (default: This Month), search field for name/code filtering. API call: `GET /api/owners/distributor-performance.php?start_date=...&end_date=...&franchise_id=X`.

3. **Data Table** -- Columns: Rank (#), Distributor Name (full_name), Code, Sales Amount (GHS formatted), Total BV (joined with `tbl_paid_receipts`), Invoice Count, Last Purchase date. Sorted by Sales Amount descending. Top 3 rows highlighted with gold/silver/bronze accent.

4. **Sort** -- Tap column header: first tap descending, second ascending, third default. Smooth animation.

5. **Search** -- Client-side filtering for datasets under 500 rows (instant). Larger datasets use debounced API call (300ms). Filter by distributor name or code.

6. **Row Tap (Future)** -- Reserved for v1.1 drill-down: purchase history, downline summary, rank progression.

### Alternative Paths

- **Offline:** Cached data with stale indicator. Sorting/search still work on cached dataset.
- **No Data:** Empty state: "No distributor activity for the selected period."
- **Large Dataset (500+):** Server-side pagination, top 50 initially, "Load More" button. Count shown: "Showing 50 of 342."
- **Export:** Share icon generates PDF with ranked table for distributor meetings.

### Success Criteria

- Loads within 3 seconds for 200 distributors; sort under 200ms; search under 100ms

---

## Journey 10: Viewing Genealogy Tree

**Persona:** James (multi-franchise owner, 52, Kampala -- reviewing MLM network structure)
**Preconditions:** Logged in. Franchise selected (Dynapharm Uganda). Distributor network has 3+ levels.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│ Reports  │───>│ Genealogy│───>│  Tree    │
│   Tab    │    │  Report  │    │  View    │
└──────────┘    └──────────┘    └──────────┘
```

### Steps

1. **Navigate** -- Reports --> Distributors --> "Genealogy Tree".

2. **Search** -- Search field at top (placeholder: "Enter distributor code or name"). "View Full Tree" button starts from franchise root. Recent searches shown as chips.

3. **Select Distributor** -- James types "UG-001". Autocomplete dropdown shows matches. Tap to select.

4. **Tree Renders** -- Root node at top (name, code, rank badge). Level 1 direct downline as child nodes. Level 2 expandable. Max depth: 20 levels (per CLAUDE.md constraint). Initially shows 2 levels expanded, deeper levels collapsed.

5. **Node Display** -- Each node shows: distributor name (full_name), code, rank badge (color-coded), direct BV this month, expand/collapse chevron, direct recruit count (e.g., "(5 direct)").

6. **Expand Nodes** -- Tap collapsed node to lazy-load children (spinner on node during fetch). Connecting lines to children. Tree supports vertical and horizontal scroll.

7. **Zoom/Pan** -- Wide trees require horizontal scroll. Pinch-to-zoom for overview of large networks.

### Alternative Paths

- **Large Tree (100+ visible):** Virtualized rendering -- only visible nodes rendered, off-screen recycled. 60fps even with thousands of nodes.
- **Offline:** Cached tree shown if previously viewed. Expand actions disabled offline.
- **No Downline:** "This distributor has no direct recruits yet." with single root node.
- **Deep Tree:** Banner at 10+ levels: "Showing up to 20 levels. Deep trees may take longer."
- **Node Detail (Future):** Long-press for bottom sheet with contact info, join date, BV totals, rank history.

### Success Criteria

- Initial tree (2 levels) within 3 seconds; node expansion within 1 second; 60fps scroll

---

## Journey 11: Profile Management

**Persona:** Nalongo (new owner, 29, Nairobi -- updating contact info after office move)
**Preconditions:** Logged in. Profile loaded from `/api/owners/profile.php`. Camera/gallery permissions available.

### Flow Diagram

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│ Profile  │───>│  Edit    │───>│  Saved   │
│   Tab    │    │  Fields  │    │  Toast   │
└──────────┘    └──────────┘    └──────────┘
```

### Steps

1. **Profile View** -- Tap profile icon in bottom nav. Displays: circular avatar (120dp, initials fallback), name (read-only), email (read-only), phone (editable), address (editable), city (editable), country (dropdown), language with "Change" link. "Edit Profile" button at bottom.

2. **Edit Mode** -- Tap "Edit Profile": editable fields become outlined text fields, name/email grayed with lock icon. "Save" (green) and "Cancel" (gray) buttons appear. Keyboard focuses first editable field.

3. **Change Photo** -- Tap avatar. Bottom sheet: "Take Photo" | "Choose from Gallery" | "Remove Photo" (if exists). Camera/gallery opens, then crop overlay (circular). Compressed client-side (max 500KB per photo-management skill). Progress ring on avatar during upload.

4. **Save** -- Validates phone format and required fields. API: `PUT /api/owners/profile.php`. Loading indicator on button. Success toast: "Profile updated successfully." Fields return to display mode. Data cached in Room.

5. **Language** -- Tap "Change": bottom sheet with 5 options (en, fr, ar, sw, es). Selection applies immediately. Arabic triggers RTL layout. Preference saved to DataStore.

### Alternative Paths

- **Photo Fail:** Error toast, reverts to previous photo. Retry by tapping avatar.
- **Validation Errors:** Inline field errors. Save disabled until valid.
- **Offline Save:** Changes queued in Room, optimistic update shown. WorkManager syncs on reconnect. Conflict notification if sync fails.
- **Cancel:** Confirmation dialog if changes made: "Discard changes?" Otherwise immediate return.
- **Session Expired:** Transparent token refresh. If fails, login screen. Unsaved changes preserved as draft.

### Success Criteria

- Profile loads within 2 seconds; photo upload within 5 seconds on 4G
- Language switch within 500ms (no restart); RTL renders correctly for Arabic

---

## Journey 12: Error Recovery

**Persona:** All personas
**Preconditions:** Various error states during normal usage. Covers 5 scenarios.

### 12a: Network Loss Mid-Report

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Report  │───>│ Network  │───>│  Banner  │───>│  Auto-   │
│ Loading  │    │   Lost   │    │ "Offline"│    │  Retry   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

1. Network drops mid-request. API fails with timeout or `UnknownHostException`.
2. Amber banner: "You are offline. Showing cached data."
3. Cached data displayed with stale timestamp, or empty state with "Retry" if no cache.
4. `ConnectivityManager` callback detects reconnection. Banner turns green: "Back online. Refreshing..."
5. Auto-retry fetches fresh data. On failure: "Connection unstable. Pull down to retry."

### 12b: Session Expired

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Any     │───>│  401     │───>│  Token   │───>│ Continue │
│  Screen  │    │ Response │    │ Refresh  │    │ Normally │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                     │ (fail)
                                ┌──────────┐
                                │  Login   │
                                │  Screen  │
                                └──────────┘
```

1. API returns 401. OkHttp `Authenticator` attempts refresh via `/api/auth/refresh.php`.
2. **Success:** Request retried transparently. New tokens stored in EncryptedSharedPreferences.
3. **Fail (30+ days):** Pending requests cancelled. Login screen with "Session expired." Email pre-filled. Cache preserved until new login. Dashboard restored after re-login.

### 12c: Server Error (5xx)

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  API     │───>│  Error   │───>│  Retry   │
│  Call    │    │  Screen  │    │ (backoff)│
└──────────┘    └──────────┘    └──────────┘
```

1. Server returns 500/502/503. Friendly error screen: "Something went wrong. Our servers are having a moment."
2. Retry with exponential backoff: 2s, 4s, 8s delays (3 attempts max).
3. All retries fail: "Still having trouble. Please try again later." with "Go to Dashboard" fallback.
4. Errors logged via Timber locally, reported to Firebase Crashlytics in production.

### 12d: Franchise Access Revoked

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  Any     │───>│   403    │───>│  Switch  │
│  Action  │    │ Response │    │Franchise │
└──────────┘    └──────────┘    └──────────┘
```

1. API returns 403 with `franchise_access_revoked`. Dialog: "Access to [franchise] has been revoked."
2. Franchise removed from local Room list.
3. Other franchises remain: navigate to franchise switcher.
4. No franchises remain: "No Access" screen with "Log Out" button. Clears all local data.

### 12e: App Crash and Recovery

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  Crash   │───>│Crashlytics│──>│  Restore │
│  Occurs  │    │  Report  │    │  State   │
└──────────┘    └──────────┘    └──────────┘
```

1. Crashlytics captures: stack trace, device info, app version, franchise context, last screen.
2. On restart: JWT checked, franchise context restored from DataStore, Room cache available.
3. Dashboard loads (not crashed screen, to avoid crash loop). Background refresh triggers.
4. Repeated crashes (3 in 60s): "Clear Cache" option offered. Clears Room/DataStore but not tokens.

### Cross-Scenario Patterns

- **Stacked errors:** Offline banner takes priority over server errors (network is root cause).
- **Error during approval:** Action queued offline (Journey 7 pattern). No data loss.
- **Slow connection:** After 10 seconds: "Still loading... Your connection may be slow."
- **Rate limiting (429):** "Too many requests." with countdown timer.

### Success Criteria

- Network loss detected within 2 seconds; token refresh transparent to user
- Server retry completes within 15 seconds; crash recovery within 3 seconds
- No data loss during any error scenario

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial journeys 7-12 |
