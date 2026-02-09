# Testing Guide - Dynapharm Owner Hub

**Quick guide to test the app in Android Studio**

---

## 🚀 Quick Start (5 Steps)

### Step 1: Open Project in Android Studio

```
1. Launch Android Studio
2. Click "Open"
3. Navigate to: C:\Users\Peter\StudioProjects\DynapharmDirectorsApp
4. Click "OK"
```

**Wait for**: Gradle sync to complete (first time takes 5-10 minutes)

---

### Step 2: Select Build Variant

```
1. Click "Build Variants" tab (bottom-left corner)
   OR View > Tool Windows > Build Variants
2. Select "devDebug" for Module: app
```

---

### Step 3: Create/Start Emulator

**Option A: Use Existing Emulator**
```
1. Click Device Manager (phone icon in toolbar)
2. Click Play button on any existing emulator
```

**Option B: Create New Emulator**
```
1. Click Device Manager
2. Click "Create Device"
3. Select "Phone" > "Pixel 6" > Next
4. Select System Image: "S" (API 31) or "Tiramisu" (API 33)
5. Click "Download" if not already downloaded
6. Click "Next" > "Finish"
7. Click Play button to start
```

---

### Step 4: Run the App

**Simple Method:**
```
Click the green "Run" button (or press Shift + F10)
```

**OR Command Line:**
```bash
./gradlew installDevDebug
```

---

### Step 5: Test the UI

The app will launch on the emulator. You'll see the **Login Screen**.

---

## 🧪 Testing Without Backend

Since the backend isn't ready yet, here are 3 ways to test the UI:

### Option 1: Test Login Screen UI Only

**What to test:**
- [ ] Username field accepts input
- [ ] Password field accepts input
- [ ] Password visibility toggle works
- [ ] Login button is disabled when fields are empty
- [ ] Login button is enabled when both fields have text
- [ ] Enter "test" / "test" and click Login
- [ ] See loading indicator briefly
- [ ] See error message: "Network error" or "Connection failed"

**Expected**: Login will fail (no backend), but you can see the UI working.

---

### Option 2: Skip Login (View Dashboard Directly)

**Temporarily bypass login to test Dashboard UI:**

1. Open: `app/src/main/kotlin/com/dynapharm/owner/presentation/navigation/NavGraph.kt`

2. Find line ~15:
   ```kotlin
   startDestination = Screen.Login.route
   ```

3. Change to:
   ```kotlin
   startDestination = Screen.Dashboard.route
   ```

4. Re-run the app

**Result**: App goes straight to Dashboard with placeholder data.

---

### Option 3: Use Mock Data (Recommended for Testing)

**Create a debug-only mock repository:**

1. Open: `app/src/main/kotlin/com/dynapharm/owner/di/RepositoryModule.kt`

2. **Temporarily** comment out the real repository and add mock:

```kotlin
// Comment out the real binding
// @Binds
// abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

// Add mock implementation
@Provides
@Singleton
fun provideDashboardRepository(): DashboardRepository {
    return object : DashboardRepository {
        override fun observeDashboardStats(): Flow<Result<DashboardStats>> = flow {
            emit(Result.Loading)
            delay(1000) // Simulate network delay
            emit(Result.Success(MockData.sampleDashboardStats))
        }

        override suspend fun getDashboardStats(forceRefresh: Boolean): Result<DashboardStats> {
            delay(500)
            return Result.Success(MockData.sampleDashboardStats)
        }

        override suspend fun refreshDashboardStats(): Result<DashboardStats> {
            delay(1000)
            return Result.Success(MockData.sampleDashboardStats)
        }

        override suspend fun clearCache() {}
    }
}
```

3. Do the same for AuthRepository (skip login, go to dashboard)

4. Re-run the app

**Result**: Dashboard shows with realistic mock data, all interactions work!

---

## 🎯 What to Test

### Login Screen Tests

- [ ] UI renders correctly
- [ ] Material 3 theme (Dynapharm green)
- [ ] Fields accept input
- [ ] Password can be shown/hidden
- [ ] Remember me checkbox
- [ ] Button states (enabled/disabled)
- [ ] Keyboard shows/hides properly
- [ ] Loading state appears during login
- [ ] Error messages display

### Dashboard Screen Tests

- [ ] 5 KPI cards display
- [ ] Trend arrows show (up/down/neutral)
- [ ] Pull-to-refresh works
- [ ] Stale data banner appears (after 5+ minutes)
- [ ] Loading indicator
- [ ] Error state with retry button
- [ ] Currency formatting (Sales, Cash, Inventory)
- [ ] Number formatting (BV, Approvals)
- [ ] Smooth scrolling

### Navigation Tests

- [ ] Bottom navigation shows 4 tabs
- [ ] Tab icons and labels correct
- [ ] Selected tab is highlighted
- [ ] Dashboard tab works
- [ ] Reports tab shows "Coming Soon"
- [ ] Finance tab shows "Coming Soon"
- [ ] Approvals tab shows "Coming Soon"
- [ ] Can switch between tabs
- [ ] No bottom bar on login screen

### Theme Tests

- [ ] Dynapharm green primary color
- [ ] Material 3 components
- [ ] Proper spacing
- [ ] Card elevations
- [ ] Typography consistency
- [ ] Icon colors

---

## 🔧 Common Issues & Solutions

### Issue: Gradle Sync Failed

**Solution:**
```
1. File > Invalidate Caches > Invalidate and Restart
2. Wait for restart
3. File > Sync Project with Gradle Files
```

### Issue: Emulator Won't Start

**Solution:**
```
1. Close Android Studio
2. Open Task Manager (Ctrl+Shift+Esc)
3. End all "qemu" and "adb" processes
4. Restart Android Studio
5. Try starting emulator again
```

### Issue: App Crashes on Launch

**Check Logcat:**
```
1. View > Tool Windows > Logcat
2. Look for red error messages
3. Filter by "com.dynapharm.owner"
```

**Common fixes:**
- Clean build: Build > Clean Project, then Build > Rebuild Project
- Invalidate caches: File > Invalidate Caches
- Check for missing dependencies in Logcat

### Issue: "Cannot resolve symbol" errors

**Solution:**
```
1. File > Sync Project with Gradle Files
2. Build > Clean Project
3. Build > Rebuild Project
```

### Issue: Network Error on Login

**Expected!** Backend isn't running. Use one of the testing options above.

---

## 📱 Testing Checklist

### Before Running
- [ ] Android Studio installed
- [ ] Project opened and Gradle synced
- [ ] Build variant set to "devDebug"
- [ ] Emulator created/started

### UI Testing
- [ ] Login screen displays
- [ ] All UI elements render correctly
- [ ] Material 3 theme applied
- [ ] Navigation works
- [ ] Dashboard loads (with mock data)

### Component Testing
- [ ] KPI cards display data
- [ ] Trend indicators work
- [ ] Pull-to-refresh functional
- [ ] Loading states show
- [ ] Error states work
- [ ] Bottom navigation responsive

### Bonus Testing
- [ ] Rotate device (landscape mode)
- [ ] Dark theme (if device supports)
- [ ] Different screen sizes
- [ ] Accessibility features

---

## 🎬 Video Testing Flow

1. **Launch app** → See login screen
2. **Enter credentials** → Button enables
3. **Click login** → See loading
4. **See error** → Expected (no backend)
5. **Switch to mock** → Skip to dashboard
6. **View dashboard** → See 5 KPI cards
7. **Pull to refresh** → Loading indicator
8. **Scroll cards** → Smooth scrolling
9. **Click Reports tab** → See "Coming Soon"
10. **Click Finance tab** → See "Coming Soon"
11. **Click Approvals tab** → See "Coming Soon"
12. **Click Dashboard tab** → Back to dashboard

---

## 📊 Expected Results

### With Real Backend
✅ Login works → Dashboard loads → Real data displays

### Without Backend (Mock Data)
✅ Login shows UI → Mock dashboard loads → Sample data displays

### Without Backend (No Mock)
⚠️ Login shows UI → Network error → Can't proceed

---

## 🎯 Success Criteria

If you can see:
1. ✅ Login screen with Dynapharm branding
2. ✅ Material 3 components (cards, buttons, etc.)
3. ✅ Dashboard with 5 KPI cards (even with mock data)
4. ✅ Bottom navigation with 4 tabs
5. ✅ Smooth navigation between tabs

**Then the app is working correctly!** 🎉

---

## 🔜 Next Steps After Testing

1. **UI looks good?** → Create backend endpoints
2. **Found bugs?** → Note them for fixing
3. **Want more features?** → Continue to Phase 2
4. **Ready for testing?** → Write unit tests (Section 9)

---

## 🆘 Need Help?

**Logcat not showing anything?**
- Make sure filter is set to "Debug" or "Verbose"
- Filter by package: `com.dynapharm.owner`

**App won't install?**
- Uninstall any existing version
- Clean build and try again

**Still stuck?**
- Check `docs/plans/sections-1-6-completion.md` for details
- Review error messages in Logcat carefully

---

**Ready to test!** Open Android Studio and follow Step 1! 🚀
