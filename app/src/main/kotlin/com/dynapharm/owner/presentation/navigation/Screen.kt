package com.dynapharm.owner.presentation.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 * Each screen has a route string used by NavHost for navigation.
 */
sealed class Screen(val route: String) {

    /**
     * Login screen - Entry point for authentication
     */
    data object Login : Screen("login")

    /**
     * Dashboard screen - Main home screen showing KPIs and overview
     */
    data object Dashboard : Screen("dashboard")

    /**
     * Reports screen - Business intelligence and analytics
     */
    data object Reports : Screen("reports")

    /**
     * Finance screen - Financial data and transactions
     */
    data object Finance : Screen("finance")

    /**
     * Approvals screen - Pending approvals and requests
     */
    data object Approvals : Screen("approvals")
}
