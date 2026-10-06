package com.kurbat.albionradar.overlay.presentation.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kurbat.albionradar.overlay.presentation.OverlayViewModel

@Composable
internal fun MinimapOverlay(viewModel: OverlayViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    MinimapContent(
        players = uiState.players,
        mobs = uiState.mobs,
        harvestables = uiState.harvestables,
        localPlayer = uiState.localPlayer,
        zoom = uiState.zoom * 0.5f,
        config = uiState.config,
        layers = RadarLayers.All,
        modifier = Modifier.fillMaxSize()
    )
}
