package com.kurbat.albionradar.overlay.presentation.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.kurbat.albionradar.core.settings.DisplayConfig
import com.kurbat.albionradar.core.settings.FilterConfig
import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Player
import com.kurbat.albionradar.overlay.rendering.RadarRenderer

@Composable
internal fun RadarCanvas(
    players: Map<Int, Player>,
    mobs: Map<Int, Mob>,
    harvestables: Map<Int, Harvestable>,
    localPlayer: Player?,
    zoom: Float,
    displayConfig: DisplayConfig,
    filterConfig: FilterConfig,
    modifier: Modifier = Modifier,
    layers: RadarLayers = RadarLayers.All,
    onZoomChange: (Float) -> Unit = {}
) {
    val renderer = remember { RadarRenderer() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoomChange, _ ->
                    onZoomChange((zoomChange - 1f) * 0.1f)
                }
            }
    ) {
        val center = Offset(size.width / 2, size.height / 2)

        renderer.drawGrid(this, center, zoom)

        if (layers.resources) {
            harvestables.values
                .filter { filterConfig.shouldShowResource(it.resourceType, it.tier, it.enchantment) }
                .forEach { resource ->
                    renderer.drawResource(this, resource, localPlayer, center, zoom, displayConfig)
                }
        }

        if (layers.mobs) {
            mobs.values
                .filter { filterConfig.shouldShowMob(it.tier, it.enchantment) }
                .forEach { mob ->
                    renderer.drawMob(this, mob, localPlayer, center, zoom, displayConfig)
                }
        }

        if (layers.players) {
            players.values.forEach { player ->
                val shouldShow = when {
                    player.isHostile -> filterConfig.showHostilePlayers
                    player.isFriendly -> filterConfig.showFriendlyPlayers
                    player.isNeutral -> filterConfig.showNeutralPlayers
                    else -> false
                }

                if (shouldShow) {
                    renderer.drawPlayer(this, player, localPlayer, center, zoom, displayConfig)
                }
            }
        }

        if (localPlayer != null) {
            renderer.drawLocalPlayer(this, center, zoom)
        }
    }
}
