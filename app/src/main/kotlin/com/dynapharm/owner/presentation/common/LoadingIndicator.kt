package com.dynapharm.owner.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.presentation.theme.OwnerHubTheme

/**
 * Fullscreen loading indicator
 *
 * Displays a centered circular progress indicator that fills the entire screen.
 * Use this for full-screen loading states.
 *
 * @param modifier Modifier for the container
 * @param color Color of the progress indicator. Defaults to primary color.
 * @param strokeWidth Width of the progress indicator stroke
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = strokeWidth
        )
    }
}

/**
 * Inline loading indicator
 *
 * Displays a small circular progress indicator for inline loading states.
 * Use this within cards, lists, or other components.
 *
 * @param modifier Modifier for the container
 * @param color Color of the progress indicator. Defaults to primary color.
 * @param size Size of the progress indicator
 * @param strokeWidth Width of the progress indicator stroke
 */
@Composable
fun InlineLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 24.dp,
    strokeWidth: Dp = 3.dp
) {
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth
        )
    }
}

/**
 * Loading overlay
 *
 * Displays a loading indicator in the center of its parent container.
 * Use this to show loading state over existing content.
 *
 * @param modifier Modifier for the container
 * @param color Color of the progress indicator. Defaults to primary color.
 */
@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = color)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    OwnerHubTheme {
        LoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun InlineLoadingIndicatorPreview() {
    OwnerHubTheme {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            InlineLoadingIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingOverlayPreview() {
    OwnerHubTheme {
        LoadingOverlay()
    }
}
