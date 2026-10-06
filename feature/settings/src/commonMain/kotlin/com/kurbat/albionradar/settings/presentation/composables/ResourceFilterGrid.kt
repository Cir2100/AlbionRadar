package com.kurbat.albionradar.settings.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.settings.ResourceEnchantMatrix

private val resourceTypeLabels = mapOf(
    "WOOD" to "Дерево",
    "ORE" to "Руда",
    "FIBER" to "Волокно",
    "HIDE" to "Кожа",
    "ROCK" to "Камень"
)

@Composable
internal fun ResourceFilterGrid(
    resourceType: String,
    matrix: ResourceEnchantMatrix,
    enabled: Boolean,
    onMatrixChange: (ResourceEnchantMatrix) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = resourceTypeLabels[resourceType] ?: resourceType,
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.width(28.dp))
                (1..8).forEach { tier ->
                    Button(
                        onClick = { onMatrixChange(matrix.toggleTier(tier)) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        contentPadding = ButtonDefaults.ContentPadding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("T$tier", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.width(28.dp))
                (1..8).forEach { tier ->
                    Text(
                        text = "T$tier",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            (0..4).forEach { enchant ->
                EnchantRow(
                    enchant = enchant,
                    matrix = matrix,
                    enabled = enabled,
                    onMatrixChange = onMatrixChange
                )
            }
        }
    }
}

@Composable
private fun EnchantRow(
    enchant: Int,
    matrix: ResourceEnchantMatrix,
    enabled: Boolean,
    onMatrixChange: (ResourceEnchantMatrix) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ".$enchant",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(28.dp)
        )

        (1..8).forEach { tier ->
            val cellEnabled = enchant == 0 || tier >= 4
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (cellEnabled) {
                    Checkbox(
                        checked = matrix.isEnabled(tier, enchant),
                        onCheckedChange = { checked ->
                            onMatrixChange(matrix.setCell(tier, enchant, checked))
                        },
                        enabled = enabled,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
