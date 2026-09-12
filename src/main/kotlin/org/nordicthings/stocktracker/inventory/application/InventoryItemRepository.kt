package org.nordicthings.stocktracker.inventory.application

import java.time.Instant
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId
import org.nordicthings.stocktracker.inventory.domain.CategoryId

interface InventoryItemRepository {
    fun save(item: InventoryItem): InventoryItem

    fun findById(id: InventoryItemId): InventoryItem?

    fun findAll(): List<InventoryItem>

    fun findAllUpdatedAfter(instant: Instant): List<InventoryItem>

    fun existsByCategoryId(categoryId: CategoryId): Boolean

    fun deleteById(id: InventoryItemId)

    fun existsByNormalizedName(normalizedName: String): Boolean

    fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: InventoryItemId): Boolean
}
