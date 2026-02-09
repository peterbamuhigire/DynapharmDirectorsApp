package com.dynapharm.owner.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.presentation.theme.OwnerHubTheme
import com.dynapharm.owner.presentation.theme.Warning
import com.dynapharm.owner.presentation.theme.WarningDark

/**
 * Banner type for different stale data scenarios
 */
enum class StaleBannerType {
    OFFLINE,
    STALE_DATA,
    SYNC_FAILED,
    CUSTOM
}

/**
 * Banner component for displaying stale or offline data warnings
 *
 * @param message The warning message to display
 * @param modifier Modifier for the banner
 * @param bannerType Type of banner to determine icon and styling
 * @param onRefresh Callback when refresh button is clicked. If null, refresh button is hidden.
 * @param icon Custom icon to display. If null, uses default based on banner type.
 * @param showRefreshButton Whether to show the refresh button
 */
@Composable
fun StaleDataBanner(
    message: String,
    modifier: Modifier = Modifier,
    bannerType: StaleBannerType = StaleBannerType.STALE_DATA,
    onRefresh: (() -> Unit)? = null,
    icon: ImageVector? = null,
    showRefreshButton: Boolean = true
) {
    val displayIcon = icon ?: when (bannerType) {
        StaleBannerType.OFFLINE -> Icons.Default.WifiOff
        StaleBannerType.STALE_DATA -> Icons.Default.Warning
        StaleBannerType.SYNC_FAILED -> Icons.Default.CloudOff
        StaleBannerType.CUSTOM -> Icons.Default.Warning
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Warning.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = displayIcon,
                    contentDescription = "Warning icon",
                    tint = WarningDark,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (showRefreshButton && onRefresh != null) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = WarningDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact stale data banner with minimal styling
 *
 * @param message The warning message to display
 * @param modifier Modifier for the banner
 * @param onRefresh Callback when refresh action is clicked
 */
@Composable
fun CompactStaleDataBanner(
    message: String,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Warning.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning icon",
                    tint = WarningDark,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (onRefresh != null) {
                TextButton(
                    onClick = onRefresh
                ) {
                    Text(
                        text = "Refresh",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Full-width stale data banner with action button
 *
 * @param message The warning message to display
 * @param modifier Modifier for the banner
 * @param actionText Text for the action button
 * @param onAction Callback when action button is clicked
 * @param bannerType Type of banner to determine icon
 */
@Composable
fun StaleDataBannerWithAction(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String = "Retry",
    onAction: () -> Unit,
    bannerType: StaleBannerType = StaleBannerType.STALE_DATA
) {
    val displayIcon = when (bannerType) {
        StaleBannerType.OFFLINE -> Icons.Default.WifiOff
        StaleBannerType.STALE_DATA -> Icons.Default.Warning
        StaleBannerType.SYNC_FAILED -> Icons.Default.CloudOff
        StaleBannerType.CUSTOM -> Icons.Default.Warning
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Warning.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = displayIcon,
                    contentDescription = "Warning icon",
                    tint = WarningDark,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = onAction
            ) {
                Text(text = actionText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StaleDataBannerPreview() {
    OwnerHubTheme {
        StaleDataBanner(
            message = "Data may be outdated. Last synced 2 hours ago.",
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    OwnerHubTheme {
        StaleDataBanner(
            message = "You are offline. Showing cached data.",
            bannerType = StaleBannerType.OFFLINE,
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncFailedBannerPreview() {
    OwnerHubTheme {
        StaleDataBanner(
            message = "Failed to sync latest data.",
            bannerType = StaleBannerType.SYNC_FAILED,
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactStaleDataBannerPreview() {
    OwnerHubTheme {
        CompactStaleDataBanner(
            message = "Showing cached data",
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StaleDataBannerWithActionPreview() {
    OwnerHubTheme {
        StaleDataBannerWithAction(
            message = "Connection lost. Data may be outdated.",
            actionText = "Retry Sync",
            onAction = {},
            bannerType = StaleBannerType.OFFLINE
        )
    }
}
