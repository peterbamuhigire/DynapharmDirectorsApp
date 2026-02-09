package com.dynapharm.owner.presentation.screens.dashboard

import com.dynapharm.owner.domain.model.DashboardStats

/**
 * UI state for the Dashboard screen.
 * Represents all possible states of the dashboard view.
 */
data class DashboardUiState(
    /**
     * Dashboard statistics data.
     * Null if not yet loaded or error occurred.
     */
    val stats: DashboardStats? = null,

    /**
     * True during initial load or when no cached data exists.
     */
    val isLoading: Boolean = false,

    /**
     * True during pull-to-refresh when cached data is displayed.
     */
    val isRefreshing: Boolean = false,

    /**
     * Error message if data fetch failed and no cached data available.
     */
    val error: String? = null,

    /**
     * True if displaying stale cached data (older than TTL).
     * Shows a banner prompting user to refresh.
     */
    val isStale: Boolean = false,

    /**
     * Timestamp of when the data was last updated.
     * Used to display "Last updated X minutes ago" text.
     */
    val lastUpdatedTimestamp: Long? = null
) {
    /**
     * True if we have data to display (even if stale).
     */
    val hasData: Boolean
        get() = stats != null

    /**
     * True if in error state with no cached data.
     */
    val hasError: Boolean
        get() = error != null && !hasData

    /**
     * True if showing empty state (no data, no error, not loading).
     */
    val isEmpty: Boolean
        get() = !hasData && !isLoading && error == null
}
