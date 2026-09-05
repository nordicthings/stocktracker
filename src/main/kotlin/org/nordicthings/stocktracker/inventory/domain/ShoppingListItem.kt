package org.nordicthings.stocktracker.inventory.domain

import org.jmolecules.ddd.annotation.ValueObject

@ValueObject
class ShoppingListItem(
    val itemId: InventoryItemId,
    val itemName: ItemName,
    val currentStock: CurrentStock,
    val minimumStock: MinimumStock,
    val targetStock: TargetStock,
    val recommendedPurchaseQuantity: Int,
    val note: ItemNote?,
) {

    init {
        if (recommendedPurchaseQuantity < 1) {
            throw InvalidStockOperationException(
                "Shopping list item purchase quantity must be at least 1: $recommendedPurchaseQuantity.",
            )
        }
    }
}
