package com.dynapharm.owner.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.presentation.theme.OwnerHubTheme

/**
 * Common empty state scenarios
 */
enum class EmptyStateType {
    NO_DATA,
    NO_RESULTS,
    NO_ITEMS,
    NO_ORDERS,
    NO_MEMBERS,
    NO_PRODUCTS,
    CUSTOM
}

/**
 * Empty state component for displaying when no data is available
 *
 * @param message The message to display
 * @param modifier Modifier for the container
 * @param icon Icon to display. If null, uses default based on empty state type.
 * @param emptyStateType Type of empty state to determine default icon
 * @param actionText Optional action button text
 * @param onActionClick Optional action button click callback
 * @param description Optional additional description text
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emptyStateType: EmptyStateType = EmptyStateType.NO_DATA,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    description: String? = null
) {
    val displayIcon = icon ?: when (emptyStateType) {
        EmptyStateType.NO_DATA -> Icons.Default.FolderOpen
        EmptyStateType.NO_RESULTS -> Icons.Default.Search
        EmptyStateType.NO_ITEMS -> Icons.Default.Receipt
        EmptyStateType.NO_ORDERS -> Icons.Default.ShoppingCart
        EmptyStateType.NO_MEMBERS -> Icons.Default.Person
        EmptyStateType.NO_PRODUCTS -> Icons.Default.Storefront
        EmptyStateType.CUSTOM -> Icons.Default.FolderOpen
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = displayIcon,
            contentDescription = "Empty state icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onActionClick
            ) {
                Text(text = actionText)
            }
        }
    }
}

/**
 * Compact empty state for inline display
 *
 * @param message The message to display
 * @param modifier Modifier for the container
 * @param icon Icon to display
 */
@Composable
fun CompactEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.FolderOpen
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Empty state icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    OwnerHubTheme {
        EmptyState(
            message = "No orders yet",
            emptyStateType = EmptyStateType.NO_ORDERS,
            description = "Orders from your franchise will appear here",
            actionText = "Refresh",
            onActionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateNoActionPreview() {
    OwnerHubTheme {
        EmptyState(
            message = "No data available",
            emptyStateType = EmptyStateType.NO_DATA,
            description = "Data will be synced when available"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptySearchResultsPreview() {
    OwnerHubTheme {
        EmptyState(
            message = "No results found",
            emptyStateType = EmptyStateType.NO_RESULTS,
            description = "Try adjusting your search criteria"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactEmptyStatePreview() {
    OwnerHubTheme {
        CompactEmptyState(
            message = "No items to display"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoMembersEmptyStatePreview() {
    OwnerHubTheme {
        EmptyState(
            message = "No members yet",
            emptyStateType = EmptyStateType.NO_MEMBERS,
            description = "New members will appear here once registered"
        )
    }
}
