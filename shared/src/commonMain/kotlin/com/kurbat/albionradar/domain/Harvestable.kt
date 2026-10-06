package com.kurbat.albionradar.domain

data class Harvestable(
    val id: Int,
    val type: String,
    val position: Position,
    val charges: Int,
    val tier: Int,
    val enchantment: Int = 0,
    val size: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val resourceType: ResourceType
        get() = when {
            type.contains("WOOD") -> ResourceType.WOOD
            type.contains("ORE") -> ResourceType.ORE
            type.contains("FIBER") -> ResourceType.FIBER
            type.contains("HIDE") -> ResourceType.HIDE
            type.contains("ROCK") -> ResourceType.ROCK
            type.contains("FISH") -> ResourceType.FISH
            else -> ResourceType.UNKNOWN
        }

    val displayName: String
        get() = type.removePrefix("@").replace("_", " ")

    val sizeLabel: String
        get() = when (size) {
            1 -> "SMALL"
            2 -> "MEDIUM"
            3 -> "LARGE"
            else -> ""
        }

    fun withState(newSize: Int, newEnchantment: Int?) = copy(
        charges = newSize,
        size = newSize,
        enchantment = newEnchantment ?: enchantment,
        timestamp = System.currentTimeMillis()
    )
}

enum class ResourceType {
    WOOD,
    ORE,
    FIBER,
    HIDE,
    ROCK,
    FISH,
    UNKNOWN
}
