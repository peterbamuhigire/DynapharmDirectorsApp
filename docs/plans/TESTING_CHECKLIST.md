# Testing Checklist - Dynapharm Owner Hub

**Use this checklist to test the app systematically**

---

## 🎯 Pre-Testing Setup

### Environment Setup
- [ ] Android Studio installed (Ladybug 2024.2.1 or later)
- [ ] JDK 17 installed and configured
- [ ] Android SDK installed (API 29-35)
- [ ] At least one emulator created (Pixel 6, API 31+)

### Project Setup
- [ ] Project opened in Android Studio: `C:\Users\Peter\StudioProjects\DynapharmDirectorsApp`
- [ ] Gradle sync completed successfully (wait for "Sync successful")
- [ ] No red errors in build output
- [ ] Build variant set to **devDebug**

---

## 🚀 Quick UI Test (No Backend)

### One-Time Modification

**File**: `app/src/main/kotlin/com/dynapharm/owner/presentation/navigation/NavGraph.kt`

**Line 15**, change:
```kotlin
startDestination = Screen.Login.route
```
To:
```kotlin
startDestination = Screen.Dashboard.route
```

**Save** the file (Ctrl+S)

---

## 📱 Testing Steps

### 1. Launch App
- [ ] Click Run button (green triangle) or Shift+F10
- [ ] Wait for "BUILD SUCCESSFUL" message
- [ ] Wait for emulator to start (if not already running)
- [ ] Wait for app to install
- [ ] App launches automatically

**Expected**: Dashboard screen appears (not login screen)

---

### 2. Dashboard Screen Tests

#### Visual Elements
- [ ] Screen title shows "Dashboard"
- [ ] 5 KPI cards are visible:
  - [ ] **Sales MTD** card with dollar amount
  - [ ] **Cash Balance** card with dollar amount
  - [ ] **Inventory Value** card with dollar amount
  - [ ] **Total BV** card with number
  - [ ] **Pending Approvals** card with count
- [ ] Each card has a colored trend arrow (↑ ↓ or →)
- [ ] All cards have proper icons
- [ ] Currency values formatted with $ and commas
- [ ] Numbers formatted with thousand separators

#### Sample Values Expected
- [ ] Sales MTD: ~$1,250,000.50 with up arrow ↑
- [ ] Cash Balance: ~$450,000.00 with down arrow ↓
- [ ] Inventory Value: ~$2,300,000.00 with up arrow ↑
- [ ] Total BV: ~15,000 with neutral arrow →
- [ ] Pending Approvals: 7 with up arrow ↑

#### Interactions
- [ ] Pull down from top → Loading indicator appears
- [ ] Release → Cards reload with slight animation
- [ ] Scroll up and down → Smooth scrolling
- [ ] Cards are touchable (visual feedback on press)

#### Colors & Theme
- [ ] Primary color is Dynapharm green (#2E7D32)
- [ ] Cards have white background
- [ ] Text is legible (good contrast)
- [ ] Icons are visible and properly colored
- [ ] Status bar matches theme

---

### 3. Bottom Navigation Tests

#### Visual Elements
- [ ] Bottom navigation bar visible
- [ ] 4 tabs present:
  1. [ ] Dashboard (🏠 icon)
  2. [ ] Reports (📊 icon)
  3. [ ] Finance (💰 icon)
  4. [ ] Approvals (✓ icon)
- [ ] Dashboard tab is highlighted/selected
- [ ] Tab labels visible and readable

#### Navigation
- [ ] Click **Reports** tab:
  - [ ] Tab highlights
  - [ ] Screen shows "Reports" title
  - [ ] "Coming Soon" message displays
  - [ ] Placeholder icon visible

- [ ] Click **Finance** tab:
  - [ ] Tab highlights
  - [ ] Screen shows "Finance" title
  - [ ] "Coming Soon" message displays

- [ ] Click **Approvals** tab:
  - [ ] Tab highlights
  - [ ] Screen shows "Approvals" title
  - [ ] "Coming Soon" message displays

- [ ] Click **Dashboard** tab:
  - [ ] Tab highlights
  - [ ] Returns to dashboard
  - [ ] KPI cards still visible
  - [ ] Data persists (same values)

---

### 4. Login Screen Tests (Optional)

**Revert NavGraph.kt to test login screen:**

Change back to:
```kotlin
startDestination = Screen.Login.route
```

**Re-run app, then test:**

#### Visual Elements
- [ ] Login screen appears on launch
- [ ] App logo/title visible at top
- [ ] "Email" or "Username" input field
- [ ] "Password" input field
- [ ] Password field has eye icon (show/hide)
- [ ] "Remember me" checkbox (optional)
- [ ] "Login" button
- [ ] No bottom navigation bar visible

#### Interactions
- [ ] Click username field → Keyboard appears
- [ ] Type text → Text appears in field
- [ ] Click password field → Keyboard appears
- [ ] Type password → Shows dots (•••)
- [ ] Click eye icon → Password text visible
- [ ] Click eye icon again → Back to dots
- [ ] Login button disabled when fields empty
- [ ] Enter "test" in both fields → Button enables
- [ ] Click Login → Loading indicator appears
- [ ] Wait → Error message appears (expected - no backend)

---

### 5. Performance Tests

#### Launch Time
- [ ] App launches in under 3 seconds (cold start)
- [ ] No splash screen freeze
- [ ] Smooth transition to first screen

#### Responsiveness
- [ ] UI responds to touches immediately
- [ ] No lag when scrolling
- [ ] Pull-to-refresh is smooth
- [ ] Tab switching is instant

#### Memory
- [ ] Check Android Studio Profiler
- [ ] Memory usage reasonable (<200MB)
- [ ] No memory leaks visible

---

### 6. Rotation & Configuration Tests

#### Portrait to Landscape
- [ ] Rotate emulator to landscape (Ctrl+F11 or toolbar)
- [ ] Dashboard layout adapts
- [ ] All cards still visible
- [ ] Bottom navigation still works
- [ ] Data persists after rotation
- [ ] No crash on rotation

#### Landscape to Portrait
- [ ] Rotate back to portrait
- [ ] Layout returns to normal
- [ ] Everything still works

---

### 7. Edge Cases

#### Pull-to-Refresh Multiple Times
- [ ] Pull-to-refresh 5 times rapidly
- [ ] App doesn't crash
- [ ] Loading indicator shows each time
- [ ] Data refreshes properly

#### Rapid Tab Switching
- [ ] Click tabs rapidly back and forth
- [ ] No crashes
- [ ] Navigation stays responsive
- [ ] Screens load correctly

#### Back Button
- [ ] Press back button from Dashboard
- [ ] App minimizes/exits (expected)
- [ ] Press back from Reports tab
- [ ] Returns to Dashboard (not exit)

---

### 8. Accessibility Tests

#### TalkBack (Optional)
- [ ] Enable TalkBack in emulator settings
- [ ] Navigate with swipe gestures
- [ ] All elements have descriptions
- [ ] Can navigate entire app with TalkBack

#### Large Text
- [ ] Enable Large Text in emulator
- [ ] All text scales appropriately
- [ ] No text cutoff
- [ ] Layout remains usable

---

## ✅ Success Criteria

### Minimum Viable Test (All Must Pass)
- [x] App launches without crash
- [x] Dashboard displays with 5 KPI cards
- [x] All 4 bottom tabs visible
- [x] Can navigate between tabs
- [x] UI looks clean and professional

### Full Test (Ideal)
- [x] All visual elements render correctly
- [x] All interactions work as expected
- [x] No crashes or errors
- [x] Smooth performance
- [x] Proper theming throughout
- [x] Rotation handled correctly
- [x] Placeholder screens show correctly

---

## 🐛 Issues Found Template

Use this format to report issues:

```
Issue: [Brief description]
Steps to Reproduce:
1. ...
2. ...
Expected: ...
Actual: ...
Screenshot: [if applicable]
Priority: High/Medium/Low
```

---

## 📊 Test Results Summary

**Date**: _________
**Tester**: _________
**Device/Emulator**: _________

### Results
- Total Tests: ___
- Passed: ___
- Failed: ___
- Skipped: ___

### Critical Issues Found
1.
2.
3.

### Minor Issues Found
1.
2.
3.

### Overall Assessment
- [ ] Ready for backend integration
- [ ] Needs minor fixes
- [ ] Needs major fixes

### Notes


---

## 🎯 Next Steps After Testing

### If All Tests Pass ✅
1. Revert NavGraph.kt to `Screen.Login.route`
2. Create backend endpoints (see API specs)
3. Test with real backend
4. Continue to Phase 1 Section 9 (Unit Tests)

### If Tests Fail ⚠️
1. Document all issues
2. Fix critical bugs first
3. Re-test
4. Proceed when stable

---

**Testing completed!** Move to backend integration or continue development.
