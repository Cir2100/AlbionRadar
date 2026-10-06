package com.kurbat.albionradar.domain

data class Player(
    val id: Int,
    val name: String,
    val equipment: List<Int> = emptyList(),
    val faction: Byte = 0,
    val guild: String? = null,
    val position: Position? = null,
    val health: Float? = null,
    val maxHealth: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isHostile: Boolean
        get() = faction.toUByte().toInt() == 255

    val isFriendly: Boolean
        get() = faction.toUByte().toInt() in 1..6

    val isNeutral: Boolean
        get() = faction.toInt() == 0

    val healthPercent: Float?
        get() = if (health != null && maxHealth != null && maxHealth > 0) {
            (health / maxHealth) * 100f
        } else {
            null
        }

    fun withPosition(pos: Position) = copy(position = pos, timestamp = System.currentTimeMillis())
    fun withHealth(current: Float, max: Float) =
        copy(health = current, maxHealth = max, timestamp = System.currentTimeMillis())
}
