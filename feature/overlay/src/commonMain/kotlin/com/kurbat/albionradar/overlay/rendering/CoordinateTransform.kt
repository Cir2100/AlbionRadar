package com.kurbat.albionradar.overlay.rendering

import androidx.compose.ui.geometry.Offset
import com.kurbat.albionradar.domain.Position

object CoordinateTransform {

    fun worldToScreen(worldPos: Position?, localPos: Position?, center: Offset, zoom: Float): Offset? {
        if (worldPos == null || localPos == null) return null

        val dx = (worldPos.x - localPos.x) * zoom * 5f
        val dy = (worldPos.y - localPos.y) * zoom * 5f

        return Offset(
            center.x + dx,
            center.y + dy
        )
    }

    fun isOnScreen(screenPos: Offset?, screenWidth: Float, screenHeight: Float): Boolean {
        if (screenPos == null) return false
        return screenPos.x in 0f..screenWidth && screenPos.y in 0f..screenHeight
    }

    fun getDistance(worldPos: Position?, localPos: Position?): Float? {
        if (worldPos == null || localPos == null) return null
        return worldPos.distanceTo(localPos)
    }
}
