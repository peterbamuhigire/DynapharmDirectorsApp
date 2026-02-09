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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.presentation.theme.OwnerHubTheme

/**
 * Error type for different error scenarios
 */
enum class ErrorType {
    GENERIC,
    NETWORK,
    SERVER,
    UNAUTHORIZED
}

/**
 * Error state component for displaying errors with retry capability
 *
 * @param message The error message to display
 * @param modifier Modifier for the container
 * @param errorType Type of error to determine icon and styling
 * @param onRetry Callback when retry button is clicked. If null, retry button is hidden.
 * @param retryButtonText Text for the retry button
 * @param icon Custom icon to display. If null, uses default based on error type.
 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    errorType: ErrorType = ErrorType.GENERIC,
    onRetry: (() -> Unit)? = null,
    retryButtonText: String = "Retry",
    icon: ImageVector? = null
) {
    val displayIcon = icon ?: when (errorType) {
        ErrorType.NETWORK -> Icons.Default.WifiOff
        ErrorType.SERVER -> Icons.Default.Warning
        ErrorType.UNAUTHORIZED -> Icons.Default.Error
        ErrorType.GENERIC -> Icons.Default.Error
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
            contentDescription = "Error icon",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = retryButtonText)
            }
        }
    }
}

/**
 * Compact error state for inline display
 *
 * @param message The error message to display
 * @param modifier Modifier for the container
 * @param onRetry Callback when retry button is clicked. If null, retry button is hidden.
 */
@Composable
fun CompactErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error icon",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRetry
            ) {
                Text(text = "Retry")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    OwnerHubTheme {
        ErrorState(
            message = "Unable to load data. Please check your internet connection and try again.",
            errorType = ErrorType.NETWORK,
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStateWithoutRetryPreview() {
    OwnerHubTheme {
        ErrorState(
            message = "You don't have permission to access this resource.",
            errorType = ErrorType.UNAUTHORIZED,
            onRetry = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactErrorStatePreview() {
    OwnerHubTheme {
        CompactErrorState(
            message = "Failed to load data",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ServerErrorStatePreview() {
    OwnerHubTheme {
        ErrorState(
            message = "Server error occurred. Please try again later.",
            errorType = ErrorType.SERVER,
            onRetry = {}
        )
    }
}
