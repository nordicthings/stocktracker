package org.nordicthings.stocktracker.inventory.application

data class InventoryItemView(
    val id: String,
    val name: String,
    val currentStock: Int,
    val minimumStock: Int,
    val targetStock: Int,
    val note: String?,
    val isBelowMinimumStock: Boolean,
    val isBelowTargetStock: Boolean,
)

data class ShoppingListItemView(
    val itemId: String,
    val itemName: String,
    val currentStock: Int,
    val minimumStock: Int,
    val targetStock: Int,
    val recommendedPurchaseQuantity: Int,
    val note: String?,
    val isBelowMinimumStock: Boolean,
)

data class InventoryOverviewView(
    val items: List<InventoryItemView>,
    val hasPurchaseNeeds: Boolean,
    val belowMinimumStockCount: Int,
    val belowTargetStockCount: Int,
)
