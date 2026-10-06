package com.kurbat.albionradar.settings.presentation.composables.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.settings.AdapterConfig

@Composable
internal fun NetworkSection(adapter: AdapterConfig, onAdapterChange: (AdapterConfig) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Network Adapter",
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
                    text = "Current Adapter",
                    style = MaterialTheme.typography.titleMedium
                )

                if (adapter.lastUsedIP != null) {
                    Text(
                        text = "IP Address: ${adapter.lastUsedIP}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "No adapter selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { /* TODO: Open adapter selector dialog */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Adapter")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Info",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "The network adapter is used to capture UDP packets from Albion Online (port 5056).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Select the adapter that connects your computer to the Internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
