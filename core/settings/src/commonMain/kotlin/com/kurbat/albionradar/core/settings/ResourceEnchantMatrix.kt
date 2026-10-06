package com.kurbat.albionradar.core.settings

import kotlinx.serialization.Serializable

@Serializable
data class ResourceEnchantMatrix(
    val cells: List<List<Boolean>> = defaultCells()
) {
    fun isEnabled(tier: Int, enchantment: Int): Boolean {
        if (tier !in 1..8 || enchantment !in 0..4) return false
        if (enchantment > 0 && tier < 4) return false
        return cells.getOrNull(enchantment)?.getOrNull(tier - 1) == true
    }

    fun setCell(tier: Int, enchantment: Int, enabled: Boolean): ResourceEnchantMatrix {
        if (tier !in 1..8 || enchantment !in 0..4) return this
        if (enchantment > 0 && tier < 4) return this
        val newCells = cells.mapIndexed { row, rowCells ->
            if (row == enchantment) {
                rowCells.mapIndexed { col, value ->
                    if (col == tier - 1) enabled else value
                }
            } else {
                rowCells
            }
        }
        return copy(cells = newCells)
    }

    fun toggleTier(tier: Int): ResourceEnchantMatrix {
        if (tier !in 1..8) return this
        val applicableEnchants = if (tier < 4) listOf(0) else 0..4
        val allEnabled = applicableEnchants.all { isEnabled(tier, it) }
        var result = this
        applicableEnchants.forEach { enchant ->
            result = result.setCell(tier, enchant, !allEnabled)
        }
        return result
    }

    companion object {
        private const val ENCHANT_COUNT = 5
        private const val TIER_COUNT = 8

        fun defaultCells(minTier: Int = 4): List<List<Boolean>> =
            List(ENCHANT_COUNT) { enchant ->
                List(TIER_COUNT) { tier ->
                    tier + 1 >= minTier && (enchant == 0 || tier + 1 >= 4)
                }
            }

        fun default(): ResourceEnchantMatrix = ResourceEnchantMatrix(defaultCells())

        fun fromLegacy(minTier: Int, enabled: Boolean): ResourceEnchantMatrix =
            if (enabled) ResourceEnchantMatrix(defaultCells(minTier)) else ResourceEnchantMatrix(emptyCells())

        private fun emptyCells(): List<List<Boolean>> =
            List(ENCHANT_COUNT) { List(TIER_COUNT) { false } }
    }
}
