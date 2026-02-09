package com.dynapharm.owner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dynapharm.owner.presentation.screens.auth.LoginScreen
import com.dynapharm.owner.presentation.screens.home.HomeScreen

/**
 * Main navigation graph for the app.
 * Handles navigation between Login and the main app (HomeScreen with tabs).
 *
 * @param navController NavHostController for managing navigation
 * @param startDestination Starting destination (defaults to Login)
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route // Changed to bypass login for testing
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login screen - no bottom navigation bar
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to home screen after successful login
                    // Clear back stack so user can't go back to login
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Home screen - contains bottom navigation and all main app screens
        composable(Screen.Dashboard.route) {
            HomeScreen()
        }

        // Note: Reports, Finance, and Approvals are handled within HomeScreen
        // They are not separate top-level destinations in this NavGraph
    }
}
