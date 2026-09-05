package org.nordicthings.stocktracker.inventory.application

import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId

interface InventoryItemRepository {
    fun save(item: InventoryItem): InventoryItem

    fun findById(id: InventoryItemId): InventoryItem?

    fun findAll(): List<InventoryItem>

    fun deleteById(id: InventoryItemId)

    fun existsByNormalizedName(normalizedName: String): Boolean

    fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: InventoryItemId): Boolean
}
