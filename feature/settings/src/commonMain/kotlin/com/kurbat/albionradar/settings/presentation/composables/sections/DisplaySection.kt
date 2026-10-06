package com.kurbat.albionradar.settings.presentation.composables.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.settings.DisplayConfig

@Composable
internal fun DisplaySection(display: DisplayConfig, onDisplayChange: (DisplayConfig) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Display Settings",
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
                    text = "Zoom",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Default zoom level: ${String.format("%.1f", display.zoom)}x",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = display.zoom,
                    onValueChange = { onDisplayChange(display.copy(zoom = it)) },
                    valueRange = 0.5f..3.0f,
                    steps = 24
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
                    text = "Opacity",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Overlay opacity: ${(display.opacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = display.opacity,
                    onValueChange = { onDisplayChange(display.copy(opacity = it)) },
                    valueRange = 0.1f..1.0f,
                    steps = 8
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
                    text = "Information Display",
                    style = MaterialTheme.typography.titleMedium
                )

                DisplayToggle(
                    label = "Show Names",
                    checked = display.showNames,
                    onCheckedChange = { onDisplayChange(display.copy(showNames = it)) }
                )

                DisplayToggle(
                    label = "Show Health Bars",
                    checked = display.showHealth,
                    onCheckedChange = { onDisplayChange(display.copy(showHealth = it)) }
                )

                DisplayToggle(
                    label = "Show Guild Names",
                    checked = display.showGuild,
                    onCheckedChange = { onDisplayChange(display.copy(showGuild = it)) }
                )

                DisplayToggle(
                    label = "Show Distance",
                    checked = display.showDistance,
                    onCheckedChange = { onDisplayChange(display.copy(showDistance = it)) }
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
                    text = "Radar Radius",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Display radius: ${display.radarRadius.toInt()} meters",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = display.radarRadius,
                    onValueChange = { onDisplayChange(display.copy(radarRadius = it)) },
                    valueRange = 50f..200f,
                    steps = 14
                )
            }
        }
    }
}

@Composable
private fun DisplayToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
