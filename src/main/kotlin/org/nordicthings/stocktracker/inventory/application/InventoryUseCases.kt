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

interface SetShoppingListToTargetUseCase {
    fun setShoppingListToTarget(command: SetShoppingListToTargetCommand)
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

interface CreateCategoryUseCase { fun create(command: CreateCategoryCommand): CategoryView }

interface EditCategoryUseCase { fun edit(command: EditCategoryCommand): CategoryView }

interface DeleteCategoryUseCase { fun delete(categoryId: String) }

interface ViewCategoryUseCase { fun viewCategory(categoryId: String): CategoryView }

interface ViewCategoriesUseCase { fun viewCategories(sort: CategorySort = CategorySort.NAME_ASCENDING): List<CategoryView> }
