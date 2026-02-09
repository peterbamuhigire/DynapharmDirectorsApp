package com.dynapharm.owner.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.domain.model.Franchise
import com.dynapharm.owner.presentation.theme.OwnerHubTheme

/**
 * Card-based banner displaying the currently active franchise.
 * Uses subtle tertiary blue color for obvious but non-intrusive visibility.
 * Includes a "Change" button to allow switching franchises.
 *
 * @param franchise The currently active franchise
 * @param onChangeFranchise Callback when "Change" button is clicked
 * @param showChangeButton Whether to show the change button (default true)
 * @param modifier Modifier for the component
 */
@Composable
fun ActiveFranchiseBanner(
    franchise: Franchise,
    onChangeFranchise: () -> Unit,
    modifier: Modifier = Modifier,
    showChangeButton: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Active franchise",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = franchise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "${franchise.branchCount} branch${if (franchise.branchCount != 1) "es" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            if (showChangeButton) {
                TextButton(
                    onClick = onChangeFranchise
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActiveFranchiseBannerPreview() {
    val franchise = Franchise(
        id = 1,
        name = "Kampala Central Franchise",
        branchCount = 3
    )

    OwnerHubTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("With Change Button", style = MaterialTheme.typography.titleSmall)
            ActiveFranchiseBanner(
                franchise = franchise,
                onChangeFranchise = {}
            )

            Text("Without Change Button", style = MaterialTheme.typography.titleSmall)
            ActiveFranchiseBanner(
                franchise = franchise,
                onChangeFranchise = {},
                showChangeButton = false
            )

            Text("Single Branch", style = MaterialTheme.typography.titleSmall)
            ActiveFranchiseBanner(
                franchise = franchise.copy(branchCount = 1),
                onChangeFranchise = {}
            )
        }
    }
}
