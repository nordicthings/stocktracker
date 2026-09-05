package org.nordicthings.stocktracker.inventory.application

interface CreateInventoryItemUseCase {
    fun create(command: CreateInventoryItemCommand): InventoryItemView
}

interface EditInventoryItemUseCase {
    fun edit(command: EditInventoryItemCommand): InventoryItemView
}

interface DeleteInventoryItemUseCase {
    fun delete(command: DeleteInventoryItemCommand)
}

interface SetCurrentStockUseCase {
    fun setCurrentStock(command: SetCurrentStockCommand): InventoryItemView
}

interface IncreaseCurrentStockUseCase {
    fun increaseCurrentStock(command: ChangeCurrentStockCommand): InventoryItemView
}

interface DecreaseCurrentStockUseCase {
    fun decreaseCurrentStock(command: ChangeCurrentStockCommand): InventoryItemView
}

interface RemoveOneItemUseCase {
    fun removeOne(itemId: String): InventoryItemView
}

interface SetStockToTargetUseCase {
    fun setStockToTarget(itemId: String): InventoryItemView
}

interface ViewInventoryItemUseCase {
    fun viewInventoryItem(itemId: String): InventoryItemView
}

interface ViewInventoryItemsUseCase {
    fun viewInventoryItems(query: InventoryItemsQuery = InventoryItemsQuery()): InventoryOverviewView
}

interface ViewShoppingListUseCase {
    fun viewShoppingList(query: ShoppingListQuery = ShoppingListQuery()): List<ShoppingListItemView>
}
