package com.kurbat.albionradar.decode.events

import com.kurbat.albionradar.domain.EventCodes
import com.kurbat.albionradar.domain.Player
import com.kurbat.albionradar.domain.Position

class PlayerEventHandler(
    private val onPlayerAdded: (Player) -> Unit,
    private val onPlayerMoved: (id: Int, position: Position) -> Unit,
    private val onPlayerRemoved: (id: Int) -> Unit,
    private val onPlayerHealthChanged: (id: Int, health: Float, maxHealth: Float) -> Unit
) : EventHandler {
    
    override fun handle(event: EventData) {
        when (event.code) {
            EventCodes.NEW_CHARACTER -> handleNewCharacter(event)
            EventCodes.MOVE -> handleMove(event)
            EventCodes.LEAVE -> handleLeave(event)
            EventCodes.HEALTH_UPDATE, EventCodes.REGENERATION_HEALTH_CHANGED -> handleHealthUpdate(event)
        }
    }
    
    private fun handleNewCharacter(event: EventData) {
        val id = event.getInt(0) ?: return
        val name = event.getString(1) ?: "Unknown"
        val equipment = event.getIntList(40).orEmpty()
        val faction = event.getInt(53)?.toByte() ?: 0
        val guild = event.getString(8)
        val player = Player(
            id = id,
            name = name,
            equipment = equipment,
            faction = faction,
            guild = guild,
            // Protocol18 player positions are XOR-encrypted without the MITM key.
            position = null
        )
        
        onPlayerAdded(player)
    }
    
    private fun handleMove(event: EventData) {
        val id = event.getInt(0) ?: return
        val posX = event.getFloat(4) ?: return
        val posY = event.getFloat(5) ?: return
        
        onPlayerMoved(id, Position(posX, posY))
    }
    
    private fun handleLeave(event: EventData) {
        val id = event.getInt(0) ?: return
        onPlayerRemoved(id)
    }
    
    private fun handleHealthUpdate(event: EventData) {
        val id = event.getInt(0) ?: return
        val health = event.getFloat(2) ?: return
        val maxHealth = event.getFloat(3) ?: return
        
        onPlayerHealthChanged(id, health, maxHealth)
    }
}
