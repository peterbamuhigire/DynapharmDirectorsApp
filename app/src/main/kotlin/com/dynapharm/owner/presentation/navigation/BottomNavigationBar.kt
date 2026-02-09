package com.dynapharm.owner.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

/**
 * Bottom navigation bar for the main app screens.
 * Displays 4 tabs: Dashboard, Reports, Finance, and Approvals.
 *
 * @param selectedRoute Currently selected route
 * @param onNavigate Callback when a tab is clicked, receives the destination route
 */
@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = androidx.compose.ui.unit.dp(3f)
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

/**
 * Data class representing a bottom navigation item
 */
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * List of bottom navigation items
 */
private val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Dashboard.route,
        label = "Dashboard",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = Screen.Reports.route,
        label = "Reports",
        icon = Icons.Default.BarChart
    ),
    BottomNavItem(
        route = Screen.Finance.route,
        label = "Finance",
        icon = Icons.Default.AccountBalance
    ),
    BottomNavItem(
        route = Screen.Approvals.route,
        label = "Approvals",
        icon = Icons.Default.CheckCircle
    )
)

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    MaterialTheme {
        BottomNavigationBar(
            selectedRoute = Screen.Dashboard.route,
            onNavigate = {}
        )
    }
}
