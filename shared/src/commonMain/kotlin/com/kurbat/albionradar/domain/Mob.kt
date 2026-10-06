package com.kurbat.albionradar.domain

data class Mob(
    val id: Int,
    val type: String,
    val position: Position,
    val enchantment: Int = 0,
    val tier: Int = 0,
    val health: Float? = null,
    val maxHealth: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = type.removePrefix("@MOB_").replace("_", " ")
    
    val healthPercent: Float?
        get() = if (health != null && maxHealth != null && maxHealth > 0) {
            (health / maxHealth) * 100f
        } else null
    
    val isBoss: Boolean
        get() = type.contains("BOSS", ignoreCase = true)
    
    val isElite: Boolean
        get() = enchantment > 0
    
    fun withPosition(pos: Position) = copy(position = pos, timestamp = System.currentTimeMillis())
    fun withHealth(current: Float, max: Float) = copy(health = current, maxHealth = max, timestamp = System.currentTimeMillis())
}
