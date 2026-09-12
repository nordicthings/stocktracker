package org.nordicthings.stocktracker.inventory.application

data class CreateInventoryItemCommand(
    val name: String,
    val categoryId: String,
    val currentStock: Int,
    val minimumStock: Int,
    val targetStock: Int,
    val note: String?,
)

data class EditInventoryItemCommand(
    val itemId: String,
    val name: String,
    val categoryId: String,
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
    val categoryId: String? = null,
    val sort: InventoryItemSort = InventoryItemSort.NAME_ASCENDING,
)

data class ShoppingListQuery(
    val sort: ShoppingListSort = ShoppingListSort.CATEGORY_ASCENDING,
)

data class SetShoppingListToTargetCommand(
    val confirmed: Boolean,
)

enum class InventoryItemSort {
    NAME_ASCENDING,
    NAME_DESCENDING,
    CATEGORY_ASCENDING,
    CATEGORY_DESCENDING,
}

enum class ShoppingListSort {
    NAME_ASCENDING,
    NAME_DESCENDING,
    CATEGORY_ASCENDING,
    CATEGORY_DESCENDING,
}

data class CreateCategoryCommand(val name: String)

data class EditCategoryCommand(val categoryId: String, val name: String)

enum class CategorySort {
    NAME_ASCENDING,
    NAME_DESCENDING,
}
