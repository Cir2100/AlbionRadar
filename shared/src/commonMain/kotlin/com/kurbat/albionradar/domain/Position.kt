package com.kurbat.albionradar.domain

import kotlin.math.sqrt

data class Position(
    val x: Float,
    val y: Float
) {
    fun distanceTo(other: Position): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }
    
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun times(scale: Float) = Position(x * scale, y * scale)
    
    companion object {
        val ZERO = Position(0f, 0f)
    }
}
