package com.kurbat.albionradar.decode.events

import com.kurbat.albionradar.domain.EventCodes
import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Position

class HarvestableEventHandler(
    private val onHarvestableAdded: (Harvestable) -> Unit,
    private val onHarvestableChanged: (id: Int, size: Int, enchantment: Int?) -> Unit,
    private val onHarvestableRemoved: (id: Int) -> Unit
) : EventHandler {
    override fun handle(event: EventData) {
        when (event.code) {
            EventCodes.NEW_SIMPLE_HARVESTABLE_OBJECT,
            EventCodes.NEW_SIMPLE_HARVESTABLE_OBJECT_LIST -> handleBatchSpawn(event)
            EventCodes.NEW_HARVESTABLE_OBJECT -> handleIndividualSpawn(event)
            EventCodes.HARVESTABLE_CHANGE_STATE -> handleStateChange(event)
            EventCodes.LEAVE -> handleLeave(event)
        }
    }

    private fun handleIndividualSpawn(event: EventData) {
        val id = event.getInt(0) ?: return
        val typeNumber = event.getInt(5) ?: return
        val tier = event.getInt(7) ?: return
        val position = event.getFloatList(8) ?: return
        if (position.size < 2) return

        val size = event.getInt(10) ?: 0
        val enchantment = event.getInt(11) ?: 0
        onHarvestableAdded(
            Harvestable(
                id = id,
                type = resourceTypeName(typeNumber),
                position = Position(position[0], position[1]),
                charges = size,
                tier = tier,
                enchantment = enchantment,
                size = size
            )
        )
    }

    private fun handleBatchSpawn(event: EventData) {
        val ids = event.intArray(0) ?: return
        val types = event.intArray(1) ?: return
        val tiers = event.intArray(2) ?: return
        val positions = event.getFloatList(3) ?: return
        val sizes = event.intArray(4) ?: return

        val count = minOf(ids.size, types.size, tiers.size, sizes.size, positions.size / 2)
        repeat(count) { index ->
            onHarvestableAdded(
                Harvestable(
                    id = ids[index],
                    type = resourceTypeName(types[index]),
                    position = Position(positions[index * 2], positions[index * 2 + 1]),
                    charges = sizes[index],
                    tier = tiers[index],
                    enchantment = 0,
                    size = sizes[index]
                )
            )
        }
    }

    private fun handleStateChange(event: EventData) {
        val id = event.getInt(0) ?: return
        val newSize = event.getInt(1)
        if (newSize == null) {
            onHarvestableRemoved(id)
            return
        }
        onHarvestableChanged(id, newSize, event.getInt(2))
    }

    private fun handleLeave(event: EventData) {
        event.getInt(0)?.let(onHarvestableRemoved)
    }

    private fun EventData.intArray(key: Int): List<Int>? = when (val value = parameters[key]) {
        is ByteArray -> value.map { it.toUByte().toInt() }
        is List<*> -> value.mapNotNull {
            when (it) {
                is Byte -> it.toInt()
                is UByte -> it.toInt()
                is Short -> it.toInt()
                is UShort -> it.toInt()
                is Int -> it
                is UInt -> it.toInt()
                is Long -> it.toInt()
                is ULong -> it.toInt()
                else -> null
            }
        }
        else -> null
    }

    private fun resourceTypeName(typeNumber: Int): String = when (typeNumber) {
        in 0..5 -> "WOOD"
        in 6..10 -> "ROCK"
        in 11..15 -> "FIBER"
        in 16..22 -> "HIDE"
        in 23..27 -> "ORE"
        else -> "RESOURCE_$typeNumber"
    }
}
