package com.kurbat.albionradar.overlay.presentation.composables

data class RadarLayers(
    val players: Boolean = true,
    val mobs: Boolean = true,
    val resources: Boolean = true
) {
    companion object {
        val All = RadarLayers()
        val ResourcesOnly = RadarLayers(players = false, mobs = false, resources = true)
    }
}
