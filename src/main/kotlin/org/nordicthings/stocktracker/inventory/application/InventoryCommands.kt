package org.nordicthings.stocktracker.inventory.application

data class CreateInventoryItemCommand(
    val name: String,
    val currentStock: Int,
    val minimumStock: Int,
    val targetStock: Int,
    val note: String?,
)

data class EditInventoryItemCommand(
    val itemId: String,
    val name: String,
    val currentStock: Int,
    val minimumStock: Int,
    val targetStock: Int,
    val note: String?,
)

data class DeleteInventoryItemCommand(
    val itemId: String,
    val confirmed: Boolean,
)

data class SetCurrentStockCommand(
    val itemId: String,
    val currentStock: Int,
)

data class ChangeCurrentStockCommand(
    val itemId: String,
    val quantity: Int,
)

data class InventoryItemsQuery(
    val searchTerm: String? = null,
    val sort: InventoryItemSort = InventoryItemSort.NAME,
)

data class ShoppingListQuery(
    val sort: ShoppingListSort = ShoppingListSort.NAME,
)

enum class InventoryItemSort {
    NAME,
    CRITICAL_FIRST,
    CURRENT_STOCK_ASCENDING,
    CURRENT_STOCK_DESCENDING,
}

enum class ShoppingListSort {
    NAME,
    RECOMMENDED_PURCHASE_QUANTITY_DESCENDING,
    RECOMMENDED_PURCHASE_QUANTITY_ASCENDING,
}
