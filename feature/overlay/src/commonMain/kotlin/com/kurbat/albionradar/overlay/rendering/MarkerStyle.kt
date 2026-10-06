package com.kurbat.albionradar.overlay.rendering

import androidx.compose.ui.graphics.Color
import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Player
import com.kurbat.albionradar.core.settings.ColorScheme

object MarkerStyle {

    fun getPlayerColor(player: Player, colorScheme: ColorScheme): Color = when {
        player.isHostile -> parseColor(colorScheme.hostile)
        player.isFriendly -> parseColor(colorScheme.friendly)
        else -> parseColor(colorScheme.neutral)
    }

    fun getMobColor(mob: Mob, colorScheme: ColorScheme): Color = if (mob.isBoss) {
        parseColor(colorScheme.mobBoss)
    } else {
        when (mob.enchantment) {
            0 -> Color.Gray
            1 -> Color.Blue
            2 -> Color.Green
            3 -> parseColor(colorScheme.mob)
            else -> Color.White
        }
    }

    fun getResourceColor(harvestable: Harvestable, colorScheme: ColorScheme): Color = when {
        harvestable.tier >= 8 -> Color(0xFFFF00FF)
        harvestable.tier >= 6 -> Color(0xFF00FFFF)
        harvestable.tier >= 4 -> Color(0xFFFFFF00)
        else -> Color.Gray
    }

    fun getPlayerRadius(zoom: Float): Float = 8f * zoom
    fun getMobRadius(zoom: Float): Float = 6f * zoom
    fun getResourceSize(zoom: Float): Float = 8f * zoom

    private fun parseColor(hex: String): Color = try {
        val cleanHex = hex.removePrefix("#")
        Color(cleanHex.toLong(16) or 0x00000000FF000000)
    } catch (e: Exception) {
        Color.White
    }
}
