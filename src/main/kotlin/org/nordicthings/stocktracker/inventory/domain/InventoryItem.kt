package org.nordicthings.stocktracker.inventory.domain

import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity

@AggregateRoot
class InventoryItem private constructor(
    @field:Identity
    val id: InventoryItemId,
    val name: ItemName,
    val currentStock: CurrentStock,
    val minimumStock: MinimumStock,
    val targetStock: TargetStock,
    val note: ItemNote?,
) {

    init {
        requireValidStockConfiguration(minimumStock, targetStock)
    }

    val isBelowMinimumStock: Boolean
        get() = currentStock.value < minimumStock.value

    val recommendedPurchaseQuantity: Int
        get() = (targetStock.value - currentStock.value).coerceAtLeast(0)

    fun edit(
        name: ItemName,
        minimumStock: MinimumStock,
        targetStock: TargetStock,
        note: ItemNote?,
    ): InventoryItem = InventoryItem(
        id = id,
        name = name,
        currentStock = currentStock,
        minimumStock = minimumStock,
        targetStock = targetStock,
        note = note,
    )

    fun setCurrentStock(currentStock: CurrentStock): InventoryItem = InventoryItem(
        id = id,
        name = name,
        currentStock = currentStock,
        minimumStock = minimumStock,
        targetStock = targetStock,
        note = note,
    )

    fun increaseCurrentStockBy(quantity: Int): InventoryItem =
        setCurrentStock(currentStock + quantity)

    fun decreaseCurrentStockBy(quantity: Int): InventoryItem =
        setCurrentStock(currentStock - quantity)

    fun removeOne(): InventoryItem = decreaseCurrentStockBy(1)

    fun setStockToTarget(): InventoryItem {
        if (currentStock.value >= targetStock.value) {
            throw InvalidStockOperationException(
                "Current stock (${currentStock.value}) must be below target stock (${targetStock.value}).",
            )
        }

        return setCurrentStock(CurrentStock.of(targetStock.value))
    }

    fun toShoppingListItem(): ShoppingListItem? =
        if (isBelowMinimumStock) {
            ShoppingListItem(
                itemId = id,
                itemName = name,
                currentStock = currentStock,
                minimumStock = minimumStock,
                targetStock = targetStock,
                recommendedPurchaseQuantity = recommendedPurchaseQuantity,
                note = note,
            )
        } else {
            null
        }

    override fun equals(other: Any?): Boolean =
        this === other || other is InventoryItem && id == other.id

    override fun hashCode(): Int = id.hashCode()

    companion object {
        fun create(
            name: ItemName,
            currentStock: CurrentStock,
            minimumStock: MinimumStock,
            targetStock: TargetStock,
            note: ItemNote?,
        ): InventoryItem = InventoryItem(
            id = InventoryItemId.newId(),
            name = name,
            currentStock = currentStock,
            minimumStock = minimumStock,
            targetStock = targetStock,
            note = note,
        )

        fun reconstitute(
            id: InventoryItemId,
            name: ItemName,
            currentStock: CurrentStock,
            minimumStock: MinimumStock,
            targetStock: TargetStock,
            note: ItemNote?,
        ): InventoryItem = InventoryItem(
            id = id,
            name = name,
            currentStock = currentStock,
            minimumStock = minimumStock,
            targetStock = targetStock,
            note = note,
        )

        private fun requireValidStockConfiguration(minimumStock: MinimumStock, targetStock: TargetStock) {
            if (targetStock.value < minimumStock.value) {
                throw InvalidStockConfigurationException(minimumStock, targetStock)
            }
        }
    }
}
