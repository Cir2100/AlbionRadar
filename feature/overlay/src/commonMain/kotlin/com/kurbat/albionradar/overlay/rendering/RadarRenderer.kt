package com.kurbat.albionradar.overlay.rendering

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Player
import com.kurbat.albionradar.core.settings.DisplayConfig
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine

class RadarRenderer {

    fun drawGrid(drawScope: DrawScope, center: Offset, zoom: Float) {
        with(drawScope) {
            val gridSpacing = 50f * zoom
            val gridColor = Color.White.copy(alpha = 0.1f)

            var x = center.x % gridSpacing
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }

            var y = center.y % gridSpacing
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }
        }
    }

    fun drawPlayer(
        drawScope: DrawScope,
        player: Player,
        localPlayer: Player?,
        center: Offset,
        zoom: Float,
        config: DisplayConfig
    ) {
        val screenPos = CoordinateTransform.worldToScreen(
            player.position,
            localPlayer?.position,
            center,
            zoom
        ) ?: return

        if (!CoordinateTransform.isOnScreen(
                screenPos,
                drawScope.size.width,
                drawScope.size.height
            )
        ) {
            return
        }

        with(drawScope) {
            val color = MarkerStyle.getPlayerColor(player, config.colors)
            val radius = MarkerStyle.getPlayerRadius(zoom)

            drawCircle(
                color = color,
                radius = radius,
                center = screenPos
            )

            if (config.showHealth && player.healthPercent != null) {
                drawHealthBar(screenPos, player.healthPercent!!, radius * 2, zoom)
            }

            if (config.showNames) {
                drawText(
                    text = player.name,
                    position = screenPos.copy(y = screenPos.y + radius + 12f * zoom),
                    color = color,
                    fontSize = 12f * zoom
                )
            }

            if (config.showGuild && player.guild != null) {
                drawText(
                    text = "[${player.guild}]",
                    position = screenPos.copy(y = screenPos.y + radius + 24f * zoom),
                    color = color.copy(alpha = 0.7f),
                    fontSize = 10f * zoom
                )
            }
        }
    }

    fun drawMob(
        drawScope: DrawScope,
        mob: Mob,
        localPlayer: Player?,
        center: Offset,
        zoom: Float,
        config: DisplayConfig
    ) {
        val screenPos = CoordinateTransform.worldToScreen(
            mob.position,
            localPlayer?.position,
            center,
            zoom
        ) ?: return

        if (!CoordinateTransform.isOnScreen(
                screenPos,
                drawScope.size.width,
                drawScope.size.height
            )
        ) {
            return
        }

        with(drawScope) {
            val color = MarkerStyle.getMobColor(mob, config.colors)
            val radius = MarkerStyle.getMobRadius(zoom)

            drawCircle(
                color = color,
                radius = radius,
                center = screenPos,
                style = Stroke(width = 2f * zoom)
            )

            if (mob.isBoss) {
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = radius * 1.5f,
                    center = screenPos
                )
            }

            if (config.showHealth && mob.healthPercent != null) {
                drawHealthBar(screenPos, mob.healthPercent!!, radius * 2, zoom)
            }
        }
    }

    fun drawResource(
        drawScope: DrawScope,
        resource: Harvestable,
        localPlayer: Player?,
        center: Offset,
        zoom: Float,
        config: DisplayConfig
    ) {
        val screenPos = CoordinateTransform.worldToScreen(
            resource.position,
            localPlayer?.position,
            center,
            zoom
        ) ?: return

        if (!CoordinateTransform.isOnScreen(
                screenPos,
                drawScope.size.width,
                drawScope.size.height
            )
        ) {
            return
        }

        with(drawScope) {
            val color = MarkerStyle.getResourceColor(resource, config.colors)
            val size = MarkerStyle.getResourceSize(zoom)

            drawRect(
                color = color,
                topLeft = screenPos - Offset(size / 2, size / 2),
                size = Size(size, size)
            )

            if (resource.charges > 0) {
                drawText(
                    text = "${resource.charges}",
                    position = screenPos.copy(y = screenPos.y - size / 2 - 8f * zoom),
                    color = color,
                    fontSize = 10f * zoom
                )
            }
        }
    }

    fun drawLocalPlayer(drawScope: DrawScope, center: Offset, zoom: Float) {
        with(drawScope) {
            drawCircle(
                color = Color.White,
                radius = 10f * zoom,
                center = center
            )
            drawCircle(
                color = Color.Black,
                radius = 8f * zoom,
                center = center
            )
        }
    }

    private fun DrawScope.drawHealthBar(position: Offset, percent: Float, width: Float, zoom: Float) {
        val barHeight = 3f * zoom
        val barWidth = width
        val barY = position.y - 15f * zoom

        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(position.x - barWidth / 2, barY),
            size = Size(barWidth, barHeight)
        )

        val healthColor = when {
            percent > 66 -> Color.Green
            percent > 33 -> Color.Yellow
            else -> Color.Red
        }

        drawRect(
            color = healthColor,
            topLeft = Offset(position.x - barWidth / 2, barY),
            size = Size(barWidth * (percent / 100f), barHeight)
        )
    }

    private fun DrawScope.drawText(text: String, position: Offset, color: Color, fontSize: Float) {
        val paint = Paint().apply {
            this.color = color.value.toInt()
        }
        val font = Font(null, fontSize)
        val textLine = TextLine.make(text, font)

        val textWidth = textLine.width
        val textX = position.x - textWidth / 2

        drawContext.canvas.nativeCanvas.drawTextLine(
            textLine,
            textX,
            position.y,
            paint
        )
    }
}
