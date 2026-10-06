package com.kurbat.albionradar.overlay.presentation.state

import com.kurbat.albionradar.core.capture.CaptureStats
import com.kurbat.albionradar.core.settings.AppConfig
import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Player

internal data class RadarUiState(
    val players: Map<Int, Player> = mapOf(),
    val mobs: Map<Int, Mob> = mapOf(),
    val harvestables: Map<Int, Harvestable> = mapOf(),
    val localPlayer: Player? = null,
    val zoom: Float = 1.0f,
    val centerX: Float = 0f,
    val centerY: Float = 0f,
    val captureStats: CaptureStats = CaptureStats(),
    val config: AppConfig = AppConfig.default(),
    val showOverlay: Boolean = false,
    val isCapturing: Boolean = false
)
