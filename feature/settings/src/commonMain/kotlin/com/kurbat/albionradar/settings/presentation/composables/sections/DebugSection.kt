package com.kurbat.albionradar.settings.presentation.composables.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.capture.CaptureStats
import com.kurbat.albionradar.core.settings.CaptureConfig

@Composable
internal fun DebugSection(
    captureStats: CaptureStats,
    capture: CaptureConfig,
    onCaptureChange: (CaptureConfig) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Debug",
            style = MaterialTheme.typography.headlineSmall
        )

        DebugStatsPanel(stats = captureStats)

        AdvancedSection(
            capture = capture,
            onCaptureChange = onCaptureChange
        )
    }
}

@Composable
private fun DebugStatsPanel(
    stats: CaptureStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Packet Statistics",
                style = MaterialTheme.typography.titleMedium
            )

            DebugStat(
                label = "Status",
                value = if (stats.isActive) "Capturing" else "Stopped",
                color = if (stats.isActive) Color.Green else Color.Red
            )
            DebugStat(label = "Packets/sec", value = stats.packetsPerSecond.toString())
            DebugStat(label = "Bandwidth", value = formatBytes(stats.bytesPerSecond))
            DebugStat(label = "Total Packets", value = formatNumber(stats.packetsReceived))
            DebugStat(label = "Total Data", value = formatBytes(stats.bytesReceived))

            if (stats.packetsDropped > 0) {
                DebugStat(
                    label = "Dropped",
                    value = stats.packetsDropped.toString(),
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
private fun DebugStat(label: String, value: String, color: Color? = null) {
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

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B/s"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB/s"
    else -> "${bytes / (1024 * 1024)} MB/s"
}

private fun formatNumber(num: Long): String = when {
    num < 1000 -> num.toString()
    num < 1000000 -> "${num / 1000}K"
    else -> "${num / 1000000}M"
}
