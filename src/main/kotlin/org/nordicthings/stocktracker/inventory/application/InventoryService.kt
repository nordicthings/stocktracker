package org.nordicthings.stocktracker.inventory.application

import java.util.Locale
import org.nordicthings.stocktracker.inventory.domain.CurrentStock
import org.nordicthings.stocktracker.inventory.domain.Category
import org.nordicthings.stocktracker.inventory.domain.CategoryId
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId
import org.nordicthings.stocktracker.inventory.domain.ItemName
import org.nordicthings.stocktracker.inventory.domain.ItemNote
import org.nordicthings.stocktracker.inventory.domain.MinimumStock
import org.nordicthings.stocktracker.inventory.domain.TargetStock
import org.springframework.stereotype.Service

@Service
class InventoryService(
    private val repository: InventoryItemRepository,
    private val categoryRepository: CategoryRepository,
) :
    CreateInventoryItemUseCase,
    EditInventoryItemUseCase,
    DeleteInventoryItemUseCase,
    SetCurrentStockUseCase,
    IncreaseCurrentStockUseCase,
    DecreaseCurrentStockUseCase,
    RemoveOneItemUseCase,
    SetStockToTargetUseCase,
    SetShoppingListToTargetUseCase,
    ViewInventoryItemUseCase,
    ViewInventoryItemsUseCase,
    ViewShoppingListUseCase {

    override fun create(command: CreateInventoryItemCommand): InventoryItemView {
        val name = ItemName.of(command.name)
        ensureNameIsAvailable(name)
        val category = getCategory(command.categoryId)

        val savedItem = repository.save(
            InventoryItem.create(
                name = name,
                categoryId = category.id,
                currentStock = CurrentStock.of(command.currentStock),
                minimumStock = MinimumStock.of(command.minimumStock),
                targetStock = TargetStock.of(command.targetStock),
                note = ItemNote.optional(command.note),
            ),
        )

        return savedItem.toView(category)
    }

    override fun edit(command: EditInventoryItemCommand): InventoryItemView {
        val item = getItem(command.itemId)
        val name = ItemName.of(command.name)
        ensureNameIsAvailable(name, item.id)
        val category = getCategory(command.categoryId)

        val savedItem = repository.save(
            item.edit(
                name = name,
                categoryId = category.id,
                currentStock = CurrentStock.of(command.currentStock),
                minimumStock = MinimumStock.of(command.minimumStock),
                targetStock = TargetStock.of(command.targetStock),
                note = ItemNote.optional(command.note),
            ),
        )

        return savedItem.toView(category)
    }

    override fun delete(command: DeleteInventoryItemCommand) {
        if (!command.confirmed) {
            throw DeleteInventoryItemNotConfirmedException()
        }

        val item = getItem(command.itemId)
        repository.deleteById(item.id)
    }

    override fun setCurrentStock(command: SetCurrentStockCommand): InventoryItemView =
        saveUpdatedItem(command.itemId) { item -> item.setCurrentStock(CurrentStock.of(command.currentStock)) }

    override fun increaseCurrentStock(command: ChangeCurrentStockCommand): InventoryItemView =
        saveUpdatedItem(command.itemId) { item -> item.increaseCurrentStockBy(command.quantity) }

    override fun decreaseCurrentStock(command: ChangeCurrentStockCommand): InventoryItemView =
        saveUpdatedItem(command.itemId) { item -> item.decreaseCurrentStockBy(command.quantity) }

    override fun removeOne(itemId: String): InventoryItemView =
        saveUpdatedItem(itemId) { item -> item.removeOne() }

    override fun setStockToTarget(itemId: String): InventoryItemView =
        saveUpdatedItem(itemId) { item -> item.setStockToTarget() }

    override fun setShoppingListToTarget(command: SetShoppingListToTargetCommand) {
        if (!command.confirmed) {
            throw SetShoppingListToTargetNotConfirmedException()
        }

        repository.findAll()
            .filter { it.isBelowTargetStock }
            .forEach { item -> repository.save(item.setStockToTarget()) }
    }

    override fun viewInventoryItem(itemId: String): InventoryItemView {
        val item = getItem(itemId)
        return item.toView(getCategory(item.categoryId))
    }

    override fun viewInventoryItems(query: InventoryItemsQuery): InventoryOverviewView {
        val allItems = repository.findAll()
        val categories = categoriesById()
        val selectedCategoryId = query.categoryId?.let { getCategory(it).id }
        val items = allItems
            .asSequence()
            .filter { item -> item.matches(query.searchTerm) }
            .filter { item -> selectedCategoryId == null || item.categoryId == selectedCategoryId }
            .sortedWith(inventoryItemComparator(query.sort, categories))
            .map { item -> item.toView(categories.getValue(item.categoryId)) }
            .toList()

        return InventoryOverviewView(
            items = items,
            hasPurchaseNeeds = allItems.any { item -> item.isBelowMinimumStock },
            belowMinimumStockCount = allItems.count { item -> item.isBelowMinimumStock },
            belowTargetStockCount = allItems.count { item -> item.isBelowTargetStock },
        )
    }

    override fun viewShoppingList(query: ShoppingListQuery): List<ShoppingListItemView> =
        repository.findAll()
            .mapNotNull { item -> item.toShoppingListItem() }
            .let { shoppingList ->
                val categories = categoriesById()
                shoppingList.sortedWith(shoppingListItemComparator(query.sort, categories))
                    .map { item ->
                        val category = categories.getValue(item.categoryId)
                        ShoppingListItemView(
                            itemId = item.itemId.value,
                            itemName = item.itemName.value,
                            categoryId = category.id.value,
                            categoryName = category.name.value,
                            currentStock = item.currentStock.value,
                            minimumStock = item.minimumStock.value,
                            targetStock = item.targetStock.value,
                            recommendedPurchaseQuantity = item.recommendedPurchaseQuantity,
                            note = item.note?.value,
                            isBelowMinimumStock = item.currentStock.value < item.minimumStock.value,
                        )
                    }
            }

    private fun getItem(itemId: String): InventoryItem {
        val id = InventoryItemId.of(itemId)
        return repository.findById(id) ?: throw InventoryItemNotFoundException(id)
    }

    private fun getCategory(categoryId: String): Category = getCategory(CategoryId.of(categoryId))

    private fun getCategory(categoryId: CategoryId): Category =
        categoryRepository.findById(categoryId) ?: throw CategoryNotFoundException(categoryId)

    private fun categoriesById(): Map<CategoryId, Category> = categoryRepository.findAll().associateBy { it.id }

    private fun ensureNameIsAvailable(name: ItemName, excludedId: InventoryItemId? = null) {
        val exists = if (excludedId == null) {
            repository.existsByNormalizedName(name.normalizedValue)
        } else {
            repository.existsByNormalizedNameExcludingId(name.normalizedValue, excludedId)
        }

        if (exists) {
            throw DuplicateItemNameException(name.value)
        }
    }

    private fun saveUpdatedItem(itemId: String, update: (InventoryItem) -> InventoryItem): InventoryItemView {
        val updatedItem = repository.save(update(getItem(itemId)))
        return updatedItem.toView(getCategory(updatedItem.categoryId))
    }

    private fun InventoryItem.matches(searchTerm: String?): Boolean =
        searchTerm.isNullOrBlank() || name.normalizedValue.contains(searchTerm.trim().lowercase(Locale.ROOT))

    private fun inventoryItemComparator(
        sort: InventoryItemSort,
        categories: Map<CategoryId, Category>,
    ): Comparator<InventoryItem> = when (sort) {
        InventoryItemSort.NAME_ASCENDING -> compareBy { it.name.normalizedValue }
        InventoryItemSort.NAME_DESCENDING -> compareByDescending<InventoryItem> { it.name.normalizedValue }
        InventoryItemSort.CATEGORY_ASCENDING -> compareBy<InventoryItem> { categories.getValue(it.categoryId).name.normalizedValue }
            .thenBy { it.name.normalizedValue }
        InventoryItemSort.CATEGORY_DESCENDING -> compareByDescending<InventoryItem> { categories.getValue(it.categoryId).name.normalizedValue }
            .thenBy { it.name.normalizedValue }
    }

    private fun shoppingListItemComparator(
        sort: ShoppingListSort,
        categories: Map<CategoryId, Category>,
    ): Comparator<org.nordicthings.stocktracker.inventory.domain.ShoppingListItem> = when (sort) {
        ShoppingListSort.NAME_ASCENDING -> compareBy { it.itemName.normalizedValue }
        ShoppingListSort.NAME_DESCENDING -> compareByDescending<org.nordicthings.stocktracker.inventory.domain.ShoppingListItem> { it.itemName.normalizedValue }
        ShoppingListSort.CATEGORY_ASCENDING ->
            compareBy<org.nordicthings.stocktracker.inventory.domain.ShoppingListItem> { categories.getValue(it.categoryId).name.normalizedValue }
                .thenBy { it.itemName.normalizedValue }
        ShoppingListSort.CATEGORY_DESCENDING ->
            compareByDescending<org.nordicthings.stocktracker.inventory.domain.ShoppingListItem> { categories.getValue(it.categoryId).name.normalizedValue }
                .thenBy { it.itemName.normalizedValue }
    }

    private fun InventoryItem.toView(category: Category): InventoryItemView = InventoryItemView(
        id = id.value,
        name = name.value,
        categoryId = category.id.value,
        categoryName = category.name.value,
        currentStock = currentStock.value,
        minimumStock = minimumStock.value,
        targetStock = targetStock.value,
        note = note?.value,
        isBelowMinimumStock = isBelowMinimumStock,
        isBelowTargetStock = isBelowTargetStock,
    )
}
