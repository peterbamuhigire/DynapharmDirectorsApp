package com.dynapharm.owner.presentation.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dynapharm.owner.presentation.navigation.BottomNavigationBar
import com.dynapharm.owner.presentation.navigation.Screen
import com.dynapharm.owner.presentation.screens.auth.LoginViewModel
import com.dynapharm.owner.presentation.screens.dashboard.DashboardScreen
import com.dynapharm.owner.presentation.screens.placeholder.PlaceholderScreen
import kotlinx.coroutines.launch

/**
 * HomeScreen - Container for the main app screens with bottom navigation.
 * Displays dashboard, reports, finance, and approvals tabs.
 *
 * This screen is shown after successful login and manages navigation
 * between the main app features using bottom tabs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dyna Director",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                loginViewModel.logout()
                                onLogout()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
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
                DashboardScreen()
            }

            composable(Screen.Reports.route) {
                PlaceholderScreen(
                    title = "Reports",
                    message = "Coming Soon",
                    icon = Icons.Default.Assessment
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
