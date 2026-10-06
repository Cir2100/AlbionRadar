package com.kurbat.albionradar.overlay.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kurbat.albionradar.core.settings.AppConfig
import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Player

@Composable
internal fun MinimapContent(
    players: Map<Int, Player>,
    mobs: Map<Int, Mob>,
    harvestables: Map<Int, Harvestable>,
    localPlayer: Player?,
    zoom: Float,
    config: AppConfig,
    layers: RadarLayers = RadarLayers.All,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = config.display.opacity * 0.5f))
            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        RadarCanvas(
            players = players,
            mobs = mobs,
            harvestables = harvestables,
            localPlayer = localPlayer,
            zoom = zoom,
            displayConfig = config.display.copy(
                showNames = false,
                showHealth = false,
                showGuild = false
            ),
            filterConfig = config.filter,
            layers = layers,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
internal fun MinimapCard(
    title: String,
    players: Map<Int, Player>,
    mobs: Map<Int, Mob>,
    harvestables: Map<Int, Harvestable>,
    localPlayer: Player?,
    zoom: Float,
    config: AppConfig,
    size: Dp = 220.dp,
    layers: RadarLayers = RadarLayers.All,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.width(size)) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            MinimapContent(
                players = players,
                mobs = mobs,
                harvestables = harvestables,
                localPlayer = localPlayer,
                zoom = zoom,
                config = config,
                layers = layers,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
                    .width(size - 16.dp)
                    .height(size - 16.dp)
            )
        }
    }
}
