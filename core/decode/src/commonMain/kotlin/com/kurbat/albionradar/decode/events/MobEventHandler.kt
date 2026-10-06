package com.kurbat.albionradar.decode.events

import com.kurbat.albionradar.domain.EventCodes
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Position

class MobEventHandler(
    private val onMobAdded: (Mob) -> Unit,
    private val onMobMoved: (id: Int, position: Position) -> Unit,
    private val onMobRemoved: (id: Int) -> Unit,
    private val onMobHealthChanged: (id: Int, health: Float, maxHealth: Float) -> Unit
) : EventHandler {
    
    override fun handle(event: EventData) {
        when (event.code) {
            EventCodes.NEW_MOB -> handleNewMob(event)
            EventCodes.MOVE -> handleMove(event)
            EventCodes.LEAVE -> handleLeave(event)
            EventCodes.HEALTH_UPDATE, EventCodes.REGENERATION_HEALTH_CHANGED -> handleHealthUpdate(event)
        }
    }
    
    private fun handleNewMob(event: EventData) {
        val id = event.getInt(0) ?: return
        val typeId = event.getInt(1) ?: return
        val position = event.getFloatList(7) ?: return
        if (position.size < 2) return
        val enchantment = event.getInt(34) ?: 0
        val normalizedHealth = event.getFloat(2)
        val maxHealth = event.getFloat(13)
        val health = if (normalizedHealth != null && maxHealth != null) {
            normalizedHealth.coerceIn(0f, 255f) / 255f * maxHealth
        } else {
            normalizedHealth
        }
        
        val mob = Mob(
            id = id,
            type = "MOB_$typeId",
            position = Position(position[0], position[1]),
            enchantment = enchantment,
            tier = 0,
            health = health,
            maxHealth = maxHealth
        )
        
        onMobAdded(mob)
    }
    
    private fun handleMove(event: EventData) {
        val id = event.getInt(0) ?: return
        val posX = event.getFloat(4) ?: return
        val posY = event.getFloat(5) ?: return
        
        onMobMoved(id, Position(posX, posY))
    }
    
    private fun handleLeave(event: EventData) {
        val id = event.getInt(0) ?: return
        onMobRemoved(id)
    }
    
    private fun handleHealthUpdate(event: EventData) {
        val id = event.getInt(0) ?: return
        val health = event.getFloat(2) ?: return
        val maxHealth = event.getFloat(3) ?: return
        
        onMobHealthChanged(id, health, maxHealth)
    }
    
}
