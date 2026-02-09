package com.dynapharm.owner.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.presentation.theme.OwnerHubTheme
import com.dynapharm.owner.presentation.theme.Success
import com.dynapharm.owner.presentation.theme.Error
import com.dynapharm.owner.presentation.theme.Warning

/**
 * Trend direction for KPI values
 */
enum class TrendDirection {
    UP,
    DOWN,
    NEUTRAL
}

/**
 * Reusable KPI Card component for displaying key performance indicators
 *
 * @param title The KPI title/label
 * @param value The KPI value to display
 * @param modifier Modifier for the card
 * @param trend The trend direction (up, down, neutral)
 * @param icon Optional icon to display
 * @param iconTint Color tint for the icon
 * @param isLoading Whether to show loading skeleton state
 * @param trendValue Optional trend value/percentage to display
 */
@Composable
fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    trend: TrendDirection = TrendDirection.NEUTRAL,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    isLoading: Boolean = false,
    trendValue: String? = null
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                // Loading skeleton state
                LoadingSkeleton()
            } else {
                // Value
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Trend indicator
                if (trend != TrendDirection.NEUTRAL || trendValue != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (trendIcon, trendColor) = when (trend) {
                            TrendDirection.UP -> Icons.Default.TrendingUp to Success
                            TrendDirection.DOWN -> Icons.Default.TrendingDown to Error
                            TrendDirection.NEUTRAL -> Icons.Default.TrendingFlat to Warning
                        }

                        Icon(
                            imageVector = trendIcon,
                            contentDescription = "Trend $trend",
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )

                        if (trendValue != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trendValue,
                                style = MaterialTheme.typography.bodySmall,
                                color = trendColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Loading skeleton for KPI card
 */
@Composable
private fun LoadingSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column {
        // Value placeholder
        Surface(
            modifier = Modifier
                .width(120.dp)
                .height(40.dp)
                .alpha(alpha),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small
        ) {}

        Spacer(modifier = Modifier.height(8.dp))

        // Trend placeholder
        Surface(
            modifier = Modifier
                .width(60.dp)
                .height(20.dp)
                .alpha(alpha),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun KpiCardPreview() {
    OwnerHubTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KpiCard(
                title = "Total Revenue",
                value = "$25,430",
                trend = TrendDirection.UP,
                trendValue = "+12.5%"
            )

            KpiCard(
                title = "Active Members",
                value = "1,234",
                trend = TrendDirection.DOWN,
                trendValue = "-3.2%"
            )

            KpiCard(
                title = "Pending Orders",
                value = "56",
                trend = TrendDirection.NEUTRAL,
                isLoading = false
            )

            KpiCard(
                title = "Loading State",
                value = "",
                isLoading = true
            )
        }
    }
}
