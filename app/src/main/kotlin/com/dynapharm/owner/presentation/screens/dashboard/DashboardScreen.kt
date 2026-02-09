package com.dynapharm.owner.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.model.Trend
import com.dynapharm.owner.presentation.common.*
import com.dynapharm.owner.presentation.theme.OwnerHubTheme
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

/**
 * Dashboard screen displaying key performance indicators.
 * Shows sales, cash balance, inventory, BV, and pending approvals.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when {
            // Loading state (no data yet)
            uiState.isLoading && !uiState.hasData -> {
                LoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Error state (no cached data)
            uiState.hasError -> {
                ErrorState(
                    message = uiState.error ?: "Failed to load dashboard data",
                    errorType = ErrorType.NETWORK,
                    onRetry = { viewModel.loadDashboardStats() }
                )
            }

            // Empty state
            uiState.isEmpty -> {
                EmptyState(
                    message = "No dashboard data available",
                    description = "Pull down to refresh",
                    emptyStateType = EmptyStateType.NO_DATA
                )
            }

            // Data state
            uiState.hasData -> {
                DashboardContent(
                    stats = uiState.stats!!,
                    isStale = uiState.isStale,
                    lastUpdated = uiState.lastUpdatedTimestamp,
                    onRefresh = { viewModel.refresh() }
                )
            }
        }

        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * Dashboard content showing all KPIs.
 */
@Composable
private fun DashboardContent(
    stats: DashboardStats,
    isStale: Boolean,
    lastUpdated: Long?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Stale data banner
        if (isStale) {
            StaleDataBanner(
                message = "Data may be outdated. ${formatLastUpdated(lastUpdated)}",
                bannerType = StaleBannerType.STALE_DATA,
                onRefresh = onRefresh
            )
        }

        // Sales MTD KPI
        KpiCard(
            title = "Sales (MTD)",
            value = formatCurrency(stats.salesMtd),
            trend = mapTrendToDirection(stats.salesTrend),
            modifier = Modifier.fillMaxWidth()
        )

        // Cash Balance KPI
        KpiCard(
            title = "Cash Balance",
            value = formatCurrency(stats.cashBalance),
            trend = mapTrendToDirection(stats.cashTrend),
            modifier = Modifier.fillMaxWidth()
        )

        // Inventory Value KPI
        KpiCard(
            title = "Inventory Value",
            value = formatCurrency(stats.inventoryValue),
            trend = mapTrendToDirection(stats.inventoryTrend),
            modifier = Modifier.fillMaxWidth()
        )

        // Total BV KPI
        KpiCard(
            title = "Total BV",
            value = formatNumber(stats.totalBv),
            trend = mapTrendToDirection(stats.bvTrend),
            modifier = Modifier.fillMaxWidth()
        )

        // Pending Approvals KPI
        KpiCard(
            title = "Pending Approvals",
            value = stats.pendingApprovals.toString(),
            trend = mapTrendToDirection(stats.approvalsTrend),
            modifier = Modifier.fillMaxWidth()
        )

        // Last updated footer
        if (lastUpdated != null) {
            Text(
                text = "Last updated: ${formatLastUpdated(lastUpdated)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

/**
 * Maps domain Trend to UI TrendDirection.
 */
private fun mapTrendToDirection(trend: Trend): TrendDirection {
    return when (trend) {
        Trend.UP -> TrendDirection.UP
        Trend.DOWN -> TrendDirection.DOWN
        Trend.NEUTRAL -> TrendDirection.NEUTRAL
    }
}

/**
 * Formats a number as currency (USD).
 */
private fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(value)
}

/**
 * Formats a number with thousand separators.
 */
private fun formatNumber(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    return formatter.format(value)
}

/**
 * Formats the last updated timestamp as a relative time string.
 */
private fun formatLastUpdated(timestamp: Long?): String {
    if (timestamp == null) return "Unknown"

    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> "${days / 7} week${if (days / 7 > 1) "s" else ""} ago"
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    OwnerHubTheme {
        DashboardContent(
            stats = DashboardStats(
                salesMtd = 45320.50,
                cashBalance = 12500.75,
                inventoryValue = 87650.00,
                totalBv = 1234.56,
                pendingApprovals = 8,
                salesTrend = Trend.UP,
                cashTrend = Trend.NEUTRAL,
                inventoryTrend = Trend.DOWN,
                bvTrend = Trend.UP,
                approvalsTrend = Trend.NEUTRAL,
                lastUpdated = System.currentTimeMillis() - (30 * 60 * 1000) // 30 min ago
            ),
            isStale = false,
            lastUpdated = System.currentTimeMillis() - (30 * 60 * 1000),
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentStalePreview() {
    OwnerHubTheme {
        DashboardContent(
            stats = DashboardStats(
                salesMtd = 45320.50,
                cashBalance = 12500.75,
                inventoryValue = 87650.00,
                totalBv = 1234.56,
                pendingApprovals = 8,
                salesTrend = Trend.UP,
                cashTrend = Trend.NEUTRAL,
                inventoryTrend = Trend.DOWN,
                bvTrend = Trend.UP,
                approvalsTrend = Trend.NEUTRAL,
                lastUpdated = System.currentTimeMillis() - (8 * 60 * 60 * 1000) // 8 hours ago
            ),
            isStale = true,
            lastUpdated = System.currentTimeMillis() - (8 * 60 * 60 * 1000),
            onRefresh = {}
        )
    }
}
