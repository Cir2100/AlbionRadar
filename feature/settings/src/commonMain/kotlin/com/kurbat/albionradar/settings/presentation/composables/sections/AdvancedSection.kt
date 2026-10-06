package com.kurbat.albionradar.settings.presentation.composables.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.settings.CaptureConfig

@Composable
internal fun AdvancedSection(capture: CaptureConfig, onCaptureChange: (CaptureConfig) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Advanced Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚠️ Warning",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Changing these settings may cause the application to stop working correctly. Only modify if you know what you're doing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
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
                    text = "Capture Settings",
                    style = MaterialTheme.typography.titleMedium
                )

                var portText by remember(capture.port) { mutableStateOf(capture.port.toString()) }
                OutlinedTextField(
                    value = portText,
                    onValueChange = {
                        portText = it
                        it.toIntOrNull()?.let { port ->
                            if (port in 1..65535) {
                                onCaptureChange(capture.copy(port = port))
                            }
                        }
                    },
                    label = { Text("UDP Port") },
                    supportingText = { Text("Albion Online UDP port (default: 5056)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                var bufferText by remember(capture.bufferSize) { mutableStateOf(capture.bufferSize.toString()) }
                OutlinedTextField(
                    value = bufferText,
                    onValueChange = {
                        bufferText = it
                        it.toIntOrNull()?.let { size ->
                            if (size in 100..10000) {
                                onCaptureChange(capture.copy(bufferSize = size))
                            }
                        }
                    },
                    label = { Text("Buffer Size") },
                    supportingText = { Text("Packet buffer size (100-10000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                var snapLenText by remember(capture.snapLen) { mutableStateOf(capture.snapLen.toString()) }
                OutlinedTextField(
                    value = snapLenText,
                    onValueChange = {
                        snapLenText = it
                        it.toIntOrNull()?.let { len ->
                            if (len in 1500..65536) {
                                onCaptureChange(capture.copy(snapLen = len))
                            }
                        }
                    },
                    label = { Text("Snap Length") },
                    supportingText = { Text("Maximum packet capture size (default: 65536)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
