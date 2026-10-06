package com.kurbat.albionradar.overlay.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.settings.FilterConfig
import com.kurbat.albionradar.overlay.presentation.OverlayViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSettings: () -> Unit, onToggleOverlay: () -> Unit) {
    val viewModel = koinViewModel<OverlayViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Albion Radar") },
                actions = {
                    IconButton(onClick = onToggleOverlay) {
                        Text(if (uiState.showOverlay) "Hide" else "Show")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Text("⚙")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterPanel(
                config = uiState.config.filter,
                modifier = Modifier.width(250.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                RadarCanvas(
                    players = uiState.players,
                    mobs = uiState.mobs,
                    harvestables = uiState.harvestables,
                    localPlayer = uiState.localPlayer,
                    zoom = uiState.zoom,
                    displayConfig = uiState.config.display,
                    filterConfig = uiState.config.filter,
                    onZoomChange = viewModel::updateZoom
                )

                if (!uiState.isCapturing) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Waiting for packets...",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Make sure Albion Online is running",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinimapCard(
                    title = "Resources",
                    players = uiState.players,
                    mobs = uiState.mobs,
                    harvestables = uiState.harvestables,
                    localPlayer = uiState.localPlayer,
                    zoom = uiState.zoom * 0.5f,
                    config = uiState.config,
                    layers = RadarLayers.ResourcesOnly
                )

                MainStatsPanel(
                    stats = uiState.captureStats,
                    playerCount = uiState.players.size,
                    mobCount = uiState.mobs.size,
                    resourceCount = uiState.harvestables.size
                )
            }
        }
    }
}

@Composable
private fun FilterPanel(config: FilterConfig, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxHeight().padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Filters",
                style = MaterialTheme.typography.titleMedium
            )

            FilterItem("Hostile Players", config.showHostilePlayers)
            FilterItem("Friendly Players", config.showFriendlyPlayers)
            FilterItem("Mobs", config.showMobs)
            FilterItem("Resources", config.showResources)
        }
    }
}

@Composable
private fun FilterItem(label: String, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
        )
    }
}
