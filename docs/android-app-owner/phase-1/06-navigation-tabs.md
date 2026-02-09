# Phase 1 -- Section 06: Navigation & Tabs

**Parent:** [Phase 1 README](README.md) | [All Docs](../README.md)

**Scope:** Routes, bottom tabs, nav graph, scaffold, top bar, placeholders, deep link stubs.

---

## 1. Screen.kt

`presentation/navigation/Screen.kt` -- all routes (Phase 1 real + Phase 2+ placeholders).

```kotlin
package com.dynapharm.owner.presentation.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")
    // Phase 1 bottom tabs
    data object Dashboard : Screen("dashboard")
    data object Reports : Screen("reports")
    data object Approvals : Screen("approvals")
    data object Franchises : Screen("franchises")
    data object More : Screen("more")
    // More sub-screens
    data object Settings : Screen("more/settings")
    data object Profile : Screen("more/profile")
    // Phase 2+: Report detail
    data class ReportDetail(val reportType: String = "{reportType}") : Screen("reports/{reportType}") {
        companion object {
            const val ROUTE = "reports/{reportType}?dateFrom={dateFrom}&dateTo={dateTo}"
            fun create(type: String, from: String? = null, to: String? = null) =
                "reports/$type?dateFrom=${from.orEmpty()}&dateTo=${to.orEmpty()}"
        }
    }
    // Phase 2+: Approval detail
    data class ApprovalDetail(val id: Long = 0L, val type: String = "") : Screen("approvals/{approvalType}/{approvalId}") {
        companion object {
            const val ROUTE = "approvals/{approvalType}/{approvalId}"
            fun create(type: String, id: Long) = "approvals/$type/$id"
        }
    }
    // Phase 2+: Franchise detail
    data class FranchiseDetail(val id: Long = 0L) : Screen("franchises/{franchiseId}") {
        companion object {
            const val ROUTE = "franchises/{franchiseId}"
            fun create(id: Long) = "franchises/$id"
        }
    }
}
```

## 2. BottomNavItem.kt

`presentation/navigation/BottomNavItem.kt`

```kotlin
package com.dynapharm.owner.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.dynapharm.owner.R

enum class BottomNavItem(val route: String, val icon: ImageVector, val labelResId: Int) {
    DASHBOARD(Screen.Dashboard.route, Icons.Filled.Dashboard, R.string.tab_dashboard),
    REPORTS(Screen.Reports.route, Icons.Filled.Assessment, R.string.tab_reports),
    APPROVALS(Screen.Approvals.route, Icons.Filled.TaskAlt, R.string.tab_approvals),
    FRANCHISES(Screen.Franchises.route, Icons.Filled.Business, R.string.tab_franchises),
    MORE(Screen.More.route, Icons.Filled.MoreHoriz, R.string.tab_more);
}
```

## 3. NavGraph.kt

`presentation/navigation/NavGraph.kt`

```kotlin
package com.dynapharm.owner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dynapharm.owner.presentation.dashboard.DashboardScreen

@Composable
fun NavGraph(
    navController: NavHostController, franchiseId: Long,
    ownerFirstName: String, franchiseName: String,
    onLogout: () -> Unit, modifier: Modifier = Modifier
) {
    NavHost(navController, Screen.Dashboard.route, modifier) {
        // Tab 1: Dashboard (real)
        composable(Screen.Dashboard.route) {
            DashboardScreen(franchiseId, ownerFirstName, onNavigateToApprovals = {
                navController.navigate(Screen.Approvals.route) {
                    popUpTo(Screen.Dashboard.route) { saveState = true }; launchSingleTop = true; restoreState = true
                }
            })
        }
        // Tab 2: Reports (placeholder)
        composable(Screen.Reports.route) {
            PlaceholderScreen("Reports", "23 reports coming in Phase 2", BottomNavItem.REPORTS.icon)
        }
        // Tab 3: Approvals (placeholder)
        composable(Screen.Approvals.route) {
            PlaceholderScreen("Approvals", "7 approval workflows coming in Phase 2", BottomNavItem.APPROVALS.icon)
        }
        // Tab 4: Franchises (placeholder)
        composable(Screen.Franchises.route) {
            PlaceholderScreen("Franchises", "Multi-franchise switching coming in Phase 2", BottomNavItem.FRANCHISES.icon)
        }
        // Tab 5: More
        composable(Screen.More.route) {
            MoreScreen(franchiseName, onLogout,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) })
        }
        // More sub-screens
        composable(Screen.Settings.route) { PlaceholderScreen("Settings", "App settings coming in Phase 2") }
        composable(Screen.Profile.route) { PlaceholderScreen("Profile", "Profile management coming in Phase 2") }
    }
}
```

## 4. MainScreen.kt

`presentation/MainScreen.kt` -- Scaffold with TopBar + BottomBar + NavHost.

```kotlin
package com.dynapharm.owner.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dynapharm.owner.presentation.navigation.*

@Composable
fun MainScreen(franchiseId: Long, franchiseName: String, ownerFirstName: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = BottomNavItem.entries
    val tabRoutes = tabs.map { it.route }
    // Sync tab index when route changes externally
    val matched = tabRoutes.indexOf(currentRoute)
    if (matched >= 0 && matched != selectedTab) selectedTab = matched
    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        topBar = { DynapharmTopBar(franchiseName, showBack = !showBottomBar, onBackClick = { navController.popBackStack() }) },
        bottomBar = { if (showBottomBar) DynapharmBottomNavBar(selectedTab) { i ->
            selectedTab = i
            navController.navigate(tabs[i].route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true
            }
        }}
    ) { padding ->
        NavGraph(navController, franchiseId, ownerFirstName, franchiseName, onLogout, Modifier.padding(padding))
    }
}
```

## 5. PlaceholderScreen.kt

`presentation/navigation/PlaceholderScreen.kt` -- reusable "Coming Soon" screen.

```kotlin
package com.dynapharm.owner.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.R

@Composable
fun PlaceholderScreen(title: String, description: String, icon: ImageVector = Icons.Filled.Construction) {
    Column(Modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
        Icon(icon, null, Modifier.size(72.dp), MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.coming_soon), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
    }
}
```

## 6. MoreScreen.kt

`presentation/navigation/MoreScreen.kt` -- Logout, Settings, Profile.

```kotlin
package com.dynapharm.owner.presentation.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.R

@Composable
fun MoreScreen(franchiseName: String, onLogout: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToProfile: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.more_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(franchiseName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
            MenuItem(Icons.Filled.Person, stringResource(R.string.more_profile), onNavigateToProfile)
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            MenuItem(Icons.Filled.Settings, stringResource(R.string.more_settings), onNavigateToSettings)
        }
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(Modifier.fillMaxWidth().clickable { onLogout() }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(24.dp), MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.more_logout), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable private fun MenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(24.dp), MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}
```

## 7. DynapharmBottomNavBar.kt

`presentation/navigation/DynapharmBottomNavBar.kt`

```kotlin
package com.dynapharm.owner.presentation.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DynapharmBottomNavBar(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        BottomNavItem.entries.forEachIndexed { i, item ->
            NavigationBarItem(
                selected = i == selectedIndex,
                onClick = { onTabSelected(i) },
                icon = { Icon(item.icon, stringResource(item.labelResId)) },
                label = { Text(stringResource(item.labelResId)) },
                alwaysShowLabel = true
            )
        }
    }
}
```

## 8. DynapharmTopBar.kt

`presentation/navigation/DynapharmTopBar.kt` -- franchise name + notification bell placeholder.

```kotlin
package com.dynapharm.owner.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dynapharm.owner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynapharmTopBar(franchiseName: String, showBack: Boolean = false, onBackClick: () -> Unit = {}, onNotificationClick: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = { Text(franchiseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        navigationIcon = { if (showBack) IconButton(onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back)) } },
        actions = { IconButton(onNotificationClick) { Icon(Icons.Filled.Notifications, stringResource(R.string.notifications)) } },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
```

## 9. String Resources

Add to `res/values/strings.xml`:

```xml
<string name="tab_dashboard">Dashboard</string>
<string name="tab_reports">Reports</string>
<string name="tab_approvals">Approvals</string>
<string name="tab_franchises">Franchises</string>
<string name="tab_more">More</string>
<string name="navigate_back">Go back</string>
<string name="notifications">Notifications</string>
<string name="more_title">More</string>
<string name="more_profile">Profile</string>
<string name="more_settings">Settings</string>
<string name="more_logout">Log Out</string>
<string name="coming_soon">Coming Soon</string>
```

## 10. Navigation State Management

Tab selection uses `rememberSaveable` in `MainScreen` so it survives configuration changes and process death. Each `navigate()` call uses `popUpTo(startDestination) { saveState = true }` + `launchSingleTop = true` + `restoreState = true` to prevent deep back stacks and preserve per-tab state.

**Back press behavior:** On non-Dashboard tabs, back returns to Dashboard. On Dashboard, back exits the app.

## 11. Deep Link Handling (Placeholder)

Deep links will be wired in Phase 2 alongside FCM push notifications:

```kotlin
composable(
    route = Screen.ApprovalDetail.ROUTE,
    deepLinks = listOf(navDeepLink { uriPattern = "dynapharm://approvals/{approvalType}/{approvalId}" })
) { /* parse args, render ApprovalDetailScreen */ }
```

The `AndroidManifest.xml` intent filter and FCM pending intent construction are deferred to Phase 2.

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial navigation and tab architecture |
