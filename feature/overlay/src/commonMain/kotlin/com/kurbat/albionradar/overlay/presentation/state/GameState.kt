package com.kurbat.albionradar.overlay.presentation.state

import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Player
import com.kurbat.albionradar.domain.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class GameState {
    private val _players = MutableStateFlow<Map<Int, Player>>(mapOf())
    val players: StateFlow<Map<Int, Player>> = _players.asStateFlow()

    private val _mobs = MutableStateFlow<Map<Int, Mob>>(mapOf())
    val mobs: StateFlow<Map<Int, Mob>> = _mobs.asStateFlow()

    private val _harvestables = MutableStateFlow<Map<Int, Harvestable>>(mapOf())
    val harvestables: StateFlow<Map<Int, Harvestable>> = _harvestables.asStateFlow()

    private val _localPlayer = MutableStateFlow<Player?>(null)
    val localPlayer: StateFlow<Player?> = _localPlayer.asStateFlow()

    fun addPlayer(player: Player) {
        _players.value = _players.value.put(player.id, player)
    }

    fun updatePlayerPosition(id: Int, position: Position) {
        val current = _players.value[id] ?: return
        _players.value = _players.value.put(id, current.withPosition(position))
    }

    fun updatePlayerHealth(id: Int, health: Float, maxHealth: Float) {
        val current = _players.value[id] ?: return
        _players.value = _players.value.put(id, current.withHealth(health, maxHealth))
    }

    fun removePlayer(id: Int) {
        _players.value = _players.value.remove(id)
    }

    fun addMob(mob: Mob) {
        _mobs.value = _mobs.value.put(mob.id, mob)
    }

    fun updateMobPosition(id: Int, position: Position) {
        val current = _mobs.value[id] ?: return
        _mobs.value = _mobs.value.put(id, current.withPosition(position))
    }

    fun updateMobHealth(id: Int, health: Float, maxHealth: Float) {
        val current = _mobs.value[id] ?: return
        _mobs.value = _mobs.value.put(id, current.withHealth(health, maxHealth))
    }

    fun removeMob(id: Int) {
        _mobs.value = _mobs.value.remove(id)
    }

    fun addHarvestable(harvestable: Harvestable) {
        _harvestables.value = _harvestables.value.put(harvestable.id, harvestable)
    }

    fun updateHarvestableState(id: Int, size: Int, enchantment: Int?) {
        val current = _harvestables.value[id] ?: return
        _harvestables.value = _harvestables.value.put(id, current.withState(size, enchantment))
    }

    fun removeHarvestable(id: Int) {
        _harvestables.value = _harvestables.value.remove(id)
    }

    fun setLocalPlayer(player: Player) {
        _localPlayer.value = player
    }

    fun updateLocalPlayerPosition(position: Position) {
        val current = _localPlayer.value
        _localPlayer.value = current?.withPosition(position) ?: Player(
            id = 0,
            name = "Local player",
            position = position
        )
    }

    fun clear() {
        _players.value = mapOf()
        _mobs.value = mapOf()
        _harvestables.value = mapOf()
        _localPlayer.value = null
    }

    fun cleanupOldObjects(maxAgeMs: Long = 60000) {
        val now = System.currentTimeMillis()

        _players.value = _players.value.filter { (_, player) ->
            now - player.timestamp < maxAgeMs
        }.toMap()

        _mobs.value = _mobs.value.filter { (_, mob) ->
            now - mob.timestamp < maxAgeMs
        }.toMap()

        _harvestables.value = _harvestables.value.filter { (_, harvestable) ->
            now - harvestable.timestamp < maxAgeMs
        }.toMap()
    }
}

private fun <T, R> Map<T, R>.put(key: T, value: R): Map<T, R> = toMutableMap().apply { put(key, value) }.toMap()

private fun <T, R> Map<T, R>.remove(key: T): Map<T, R> = toMutableMap().apply { remove(key) }.toMap()
