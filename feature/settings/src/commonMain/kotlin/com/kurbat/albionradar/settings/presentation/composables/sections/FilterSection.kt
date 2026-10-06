package com.kurbat.albionradar.settings.presentation.composables.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.settings.FilterConfig
import com.kurbat.albionradar.core.settings.ResourceEnchantMatrix
import com.kurbat.albionradar.settings.presentation.composables.ResourceFilterGrid

private val resourceTypes = listOf("WOOD", "ORE", "FIBER", "HIDE", "ROCK")

@Composable
internal fun FilterSection(filter: FilterConfig, onFilterChange: (FilterConfig) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Display Filters",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Players",
                    style = MaterialTheme.typography.titleMedium
                )

                FilterCheckbox(
                    label = "Show Hostile Players",
                    description = "Red players (enemies)",
                    checked = filter.showHostilePlayers,
                    onCheckedChange = { onFilterChange(filter.copy(showHostilePlayers = it)) }
                )

                FilterCheckbox(
                    label = "Show Friendly Players",
                    description = "Yellow players (allies)",
                    checked = filter.showFriendlyPlayers,
                    onCheckedChange = { onFilterChange(filter.copy(showFriendlyPlayers = it)) }
                )

                FilterCheckbox(
                    label = "Show Neutral Players",
                    description = "White players",
                    checked = filter.showNeutralPlayers,
                    onCheckedChange = { onFilterChange(filter.copy(showNeutralPlayers = it)) }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Mobs",
                    style = MaterialTheme.typography.titleMedium
                )

                FilterCheckbox(
                    label = "Show Mobs",
                    description = "Display all mobs on radar",
                    checked = filter.showMobs,
                    onCheckedChange = { onFilterChange(filter.copy(showMobs = it)) }
                )

                Text(
                    text = "Minimum Mob Tier: ${filter.minMobTier}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = filter.minMobTier.toFloat(),
                    onValueChange = { onFilterChange(filter.copy(minMobTier = it.toInt())) },
                    valueRange = 1f..8f,
                    steps = 6,
                    enabled = filter.showMobs
                )

                Text(
                    text = "Minimum Enchantment: ${if (filter.minMobEnchantment == 0) "None" else ".${filter.minMobEnchantment}"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = filter.minMobEnchantment.toFloat(),
                    onValueChange = { onFilterChange(filter.copy(minMobEnchantment = it.toInt())) },
                    valueRange = 0f..3f,
                    steps = 2,
                    enabled = filter.showMobs
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Resources",
                    style = MaterialTheme.typography.titleMedium
                )

                FilterCheckbox(
                    label = "Show Resources",
                    description = "Display harvestable resources",
                    checked = filter.showResources,
                    onCheckedChange = { onFilterChange(filter.copy(showResources = it)) }
                )

                resourceTypes.forEach { type ->
                    val matrix = filter.resourceFilters[type] ?: ResourceEnchantMatrix.default()
                    ResourceFilterGrid(
                        resourceType = type,
                        matrix = matrix,
                        enabled = filter.showResources,
                        onMatrixChange = { updated ->
                            onFilterChange(filter.updateResourceFilter(type, updated))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterCheckbox(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
