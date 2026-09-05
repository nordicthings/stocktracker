package org.nordicthings.stocktracker.inventory.adapter.persistence

import java.time.Instant
import org.nordicthings.stocktracker.inventory.domain.CurrentStock
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId
import org.nordicthings.stocktracker.inventory.domain.ItemName
import org.nordicthings.stocktracker.inventory.domain.ItemNote
import org.nordicthings.stocktracker.inventory.domain.MinimumStock
import org.nordicthings.stocktracker.inventory.domain.TargetStock
import org.springframework.stereotype.Component

@Component
class InventoryItemJpaMapper {

    fun toDomain(entity: InventoryItemJpaEntity): InventoryItem = InventoryItem.reconstitute(
        id = InventoryItemId.of(entity.id),
        name = ItemName.of(entity.name),
        currentStock = CurrentStock.of(entity.currentStock),
        minimumStock = MinimumStock.of(entity.minimumStock),
        targetStock = TargetStock.of(entity.targetStock),
        note = ItemNote.optional(entity.note),
    )

    fun toEntity(
        item: InventoryItem,
        createdAt: Instant,
        updatedAt: Instant,
    ): InventoryItemJpaEntity = InventoryItemJpaEntity(
        id = item.id.value,
        name = item.name.value,
        normalizedName = item.name.normalizedValue,
        currentStock = item.currentStock.value,
        minimumStock = item.minimumStock.value,
        targetStock = item.targetStock.value,
        note = item.note?.value,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
