package com.kurbat.albionradar.decode.events

import com.kurbat.albionradar.domain.EventCodes
import com.kurbat.albionradar.domain.Harvestable
import com.kurbat.albionradar.domain.Mob
import com.kurbat.albionradar.domain.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Protocol18EventHandlersTest {
    @Test
    fun readsDragonfireMobLayout() {
        var mob: Mob? = null
        val handler = MobEventHandler(
            onMobAdded = { mob = it },
            onMobMoved = { _, _ -> },
            onMobRemoved = {},
            onMobHealthChanged = { _, _, _ -> }
        )

        handler.handle(
            EventData(
                code = EventCodes.NEW_MOB,
                parameters = mapOf(
                    0 to 1000L,
                    1 to 421,
                    2 to 128.toUByte(),
                    7 to listOf(10.5f, 20.25f),
                    13 to 1_000f,
                    34 to 3
                )
            )
        )

        val result = assertNotNull(mob)
        assertEquals("MOB_421", result.type)
        assertEquals(10.5f, result.position.x)
        assertEquals(20.25f, result.position.y)
        assertEquals(3, result.enchantment)
        assertEquals(128f / 255f * 1_000f, result.health!!, 0.001f)
    }

    @Test
    fun readsProtocol18PlayerLayout() {
        var player: Player? = null
        val handler = PlayerEventHandler(
            onPlayerAdded = { player = it },
            onPlayerMoved = { _, _ -> },
            onPlayerRemoved = {},
            onPlayerHealthChanged = { _, _, _ -> }
        )

        handler.handle(
            EventData(
                code = EventCodes.NEW_CHARACTER,
                parameters = mapOf(
                    0 to 42L,
                    1 to "Bandit",
                    8 to "Guild",
                    40 to listOf(100, 200, 300),
                    53 to 255.toUByte()
                )
            )
        )

        val result = assertNotNull(player)
        assertEquals(listOf(100, 200, 300), result.equipment)
        assertEquals("Guild", result.guild)
        assertTrue(result.isHostile)
        assertEquals(null, result.position)
    }

    @Test
    fun readsIndividualAndBatchHarvestableLayouts() {
        val resources = mutableListOf<Harvestable>()
        val handler = HarvestableEventHandler(
            onHarvestableAdded = resources::add,
            onHarvestableChanged = { _, _, _ -> },
            onHarvestableRemoved = {}
        )

        handler.handle(
            EventData(
                code = EventCodes.NEW_HARVESTABLE_OBJECT,
                parameters = mapOf(
                    0 to 1L,
                    5 to 24,
                    7 to 6,
                    8 to listOf(12.5f, -3.5f),
                    10 to 5,
                    11 to 2
                )
            )
        )
        handler.handle(
            EventData(
                code = EventCodes.NEW_SIMPLE_HARVESTABLE_OBJECT_LIST,
                parameters = mapOf(
                    0 to listOf(2L, 3L),
                    1 to byteArrayOf(0, 11),
                    2 to byteArrayOf(4, 5),
                    3 to listOf(1f, 2f, 3f, 4f),
                    4 to byteArrayOf(7, 8)
                )
            )
        )

        assertEquals(3, resources.size)
        assertEquals("ORE", resources[0].type)
        assertEquals(2, resources[0].enchantment)
        assertEquals("WOOD", resources[1].type)
        assertEquals("FIBER", resources[2].type)
        assertEquals(8, resources[2].size)
    }
}
