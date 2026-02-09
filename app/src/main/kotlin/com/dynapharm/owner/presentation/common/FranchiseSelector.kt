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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.domain.model.Franchise
import com.dynapharm.owner.presentation.theme.OwnerHubTheme

/**
 * Material 3 dropdown selector for choosing a franchise.
 * Displays franchise name, branch count, and store icon.
 * Shows checkmark for currently selected franchise.
 *
 * @param franchises List of available franchises
 * @param selectedFranchise Currently selected franchise
 * @param onFranchiseSelected Callback when a franchise is selected
 * @param enabled Whether the selector is enabled
 * @param modifier Modifier for the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranchiseSelector(
    franchises: List<Franchise>,
    selectedFranchise: Franchise?,
    onFranchiseSelected: (Franchise) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && enabled },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedFranchise?.name ?: "Select franchise",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Franchise") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Franchise",
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            franchises.forEach { franchise ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = franchise.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${franchise.branchCount} branch${if (franchise.branchCount != 1) "es" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (selectedFranchise?.id == franchise.id) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onFranchiseSelected(franchise)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FranchiseSelectorPreview() {
    val franchises = listOf(
        Franchise(id = 1, name = "Kampala Central", branchCount = 3),
        Franchise(id = 2, name = "Nairobi Main", branchCount = 5),
        Franchise(id = 3, name = "Dar es Salaam", branchCount = 2)
    )

    OwnerHubTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("With Selection", style = MaterialTheme.typography.titleMedium)
            FranchiseSelector(
                franchises = franchises,
                selectedFranchise = franchises[0],
                onFranchiseSelected = {}
            )

            Text("No Selection", style = MaterialTheme.typography.titleMedium)
            FranchiseSelector(
                franchises = franchises,
                selectedFranchise = null,
                onFranchiseSelected = {}
            )

            Text("Disabled", style = MaterialTheme.typography.titleMedium)
            FranchiseSelector(
                franchises = franchises,
                selectedFranchise = franchises[1],
                onFranchiseSelected = {},
                enabled = false
            )
        }
    }
}
