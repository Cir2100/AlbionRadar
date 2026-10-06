package com.kurbat.albionradar.overlay.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.capture.CaptureStats

@Composable
internal fun MainStatsPanel(
    stats: CaptureStats,
    playerCount: Int,
    mobCount: Int,
    resourceCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium
            )

            Stat(
                label = "Status",
                value = if (stats.isActive) "Capturing" else "Stopped",
                color = if (stats.isActive) Color.Green else Color.Red
            )

            Text(
                text = "Objects",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            Stat(label = "Players", value = playerCount.toString())
            Stat(label = "Mobs", value = mobCount.toString())
            Stat(label = "Resources", value = resourceCount.toString())
        }
    }
}

@Composable
private fun Stat(label: String, value: String, color: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
