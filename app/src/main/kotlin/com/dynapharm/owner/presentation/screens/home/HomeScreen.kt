package com.dynapharm.owner.presentation.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dynapharm.owner.presentation.navigation.BottomNavigationBar
import com.dynapharm.owner.presentation.navigation.Screen
import com.dynapharm.owner.presentation.screens.placeholder.PlaceholderScreen

/**
 * HomeScreen - Container for the main app screens with bottom navigation.
 * Displays dashboard, reports, finance, and approvals tabs.
 *
 * This screen is shown after successful login and manages navigation
 * between the main app features using bottom tabs.
 */
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = currentRoute,
                onNavigate = { route ->
                    // Navigate to the selected route, avoiding multiple copies on the back stack
                    navController.navigate(route) {
                        // Pop up to the dashboard to avoid building up a large back stack
                        popUpTo(Screen.Dashboard.route) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                PlaceholderScreen(
                    title = "Dashboard",
                    message = "Coming Soon"
                )
            }

            composable(Screen.Reports.route) {
                PlaceholderScreen(
                    title = "Reports",
                    message = "Coming Soon",
                    icon = Icons.Default.BarChart
                )
            }

            composable(Screen.Finance.route) {
                PlaceholderScreen(
                    title = "Finance",
                    message = "Coming Soon",
                    icon = Icons.Default.AccountBalance
                )
            }

            composable(Screen.Approvals.route) {
                PlaceholderScreen(
                    title = "Approvals",
                    message = "Coming Soon",
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}
