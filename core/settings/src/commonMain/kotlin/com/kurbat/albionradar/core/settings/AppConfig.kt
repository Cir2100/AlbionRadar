package com.kurbat.albionradar.core.settings

import com.kurbat.albionradar.domain.ResourceType
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val adapter: AdapterConfig = AdapterConfig(),
    val filter: FilterConfig = FilterConfig(),
    val display: DisplayConfig = DisplayConfig(),
    val capture: CaptureConfig = CaptureConfig()
) {
    companion object {
        fun default() = AppConfig(filter = FilterConfig().withMigration())
    }
}

@Serializable
data class AdapterConfig(val lastUsedIP: String? = null)

private val defaultResourceTypes = listOf("WOOD", "ORE", "FIBER", "HIDE", "ROCK")

@Serializable
data class FilterConfig(
    val showHostilePlayers: Boolean = true,
    val showFriendlyPlayers: Boolean = true,
    val showNeutralPlayers: Boolean = false,
    val showMobs: Boolean = true,
    val showResources: Boolean = true,
    val minMobTier: Int = 4,
    val minMobEnchantment: Int = 0,
    val resourceFilters: Map<String, ResourceEnchantMatrix> = emptyMap(),
    val minResourceTier: Int = 4,
    val resourceTypes: Set<String> = setOf("WOOD", "ORE", "FIBER", "HIDE", "ROCK")
) {
    fun shouldShowResource(type: ResourceType, tier: Int, enchantment: Int): Boolean {
        if (!showResources) return false
        val matrix = resourceFilters[type.name] ?: return false
        return matrix.isEnabled(tier, enchantment)
    }

    fun shouldShowMob(tier: Int, enchantment: Int): Boolean {
        if (!showMobs) return false
        if (tier < minMobTier) return false
        if (enchantment < minMobEnchantment) return false
        return true
    }

    fun withMigration(): FilterConfig {
        if (resourceFilters.isNotEmpty()) return this
        val migratedFilters = defaultResourceTypes.associateWith { type ->
            val enabled = type in resourceTypes
            ResourceEnchantMatrix.fromLegacy(minResourceTier, enabled)
        }
        return copy(resourceFilters = migratedFilters)
    }

    fun updateResourceFilter(type: String, matrix: ResourceEnchantMatrix): FilterConfig =
        copy(resourceFilters = resourceFilters + (type to matrix))
}

@Serializable
data class DisplayConfig(
    val zoom: Float = 1.0f,
    val showNames: Boolean = true,
    val showHealth: Boolean = true,
    val showDistance: Boolean = false,
    val showGuild: Boolean = true,
    val radarRadius: Float = 100f,
    val opacity: Float = 0.8f,
    val colors: ColorScheme = ColorScheme()
)

@Serializable
data class ColorScheme(
    val hostile: String = "#FF0000",
    val friendly: String = "#FFFF00",
    val neutral: String = "#FFFFFF",
    val mob: String = "#FF00FF",
    val mobBoss: String = "#FF8800",
    val resource: String = "#00FF00"
)

@Serializable
data class CaptureConfig(val port: Int = 5056, val snapLen: Int = 65536, val bufferSize: Int = 1000)
