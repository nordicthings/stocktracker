package org.nordicthings.stocktracker.inventory.application

import java.util.Locale
import org.nordicthings.stocktracker.inventory.domain.CurrentStock
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId
import org.nordicthings.stocktracker.inventory.domain.ItemName
import org.nordicthings.stocktracker.inventory.domain.ItemNote
import org.nordicthings.stocktracker.inventory.domain.MinimumStock
import org.nordicthings.stocktracker.inventory.domain.TargetStock

class InventoryService(
    private val repository: InventoryItemRepository,
) :
    CreateInventoryItemUseCase,
    EditInventoryItemUseCase,
    DeleteInventoryItemUseCase,
    SetCurrentStockUseCase,
    IncreaseCurrentStockUseCase,
    DecreaseCurrentStockUseCase,
    RemoveOneItemUseCase,
    SetStockToTargetUseCase,
    ViewInventoryItemsUseCase,
    ViewShoppingListUseCase {

    override fun create(command: CreateInventoryItemCommand): InventoryItemView {
        val name = ItemName.of(command.name)
        ensureNameIsAvailable(name)

        val savedItem = repository.save(
            InventoryItem.create(
                name = name,
                currentStock = CurrentStock.of(command.currentStock),
                minimumStock = MinimumStock.of(command.minimumStock),
                targetStock = TargetStock.of(command.targetStock),
                note = ItemNote.optional(command.note),
            ),
        )

        return savedItem.toView()
    }

    override fun edit(command: EditInventoryItemCommand): InventoryItemView {
        val item = getItem(command.itemId)
        val name = ItemName.of(command.name)
        ensureNameIsAvailable(name, item.id)

        val savedItem = repository.save(
            item.edit(
                name = name,
                minimumStock = MinimumStock.of(command.minimumStock),
                targetStock = TargetStock.of(command.targetStock),
                note = ItemNote.optional(command.note),
            ),
        )

        return savedItem.toView()
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

    override fun viewInventoryItems(query: InventoryItemsQuery): InventoryOverviewView {
        val allItems = repository.findAll()
        val items = allItems
            .asSequence()
            .filter { item -> item.matches(query.searchTerm) }
            .sortedWith(inventoryItemComparator(query.sort))
            .map { item -> item.toView() }
            .toList()

        return InventoryOverviewView(
            items = items,
            hasPurchaseNeeds = allItems.any { item -> item.isBelowMinimumStock },
        )
    }

    override fun viewShoppingList(query: ShoppingListQuery): List<ShoppingListItemView> =
        repository.findAll()
            .mapNotNull { item -> item.toShoppingListItem() }
            .sortedWith(shoppingListItemComparator(query.sort))
            .map { item ->
                ShoppingListItemView(
                    itemId = item.itemId.value,
                    itemName = item.itemName.value,
                    currentStock = item.currentStock.value,
                    minimumStock = item.minimumStock.value,
                    targetStock = item.targetStock.value,
                    recommendedPurchaseQuantity = item.recommendedPurchaseQuantity,
                    note = item.note?.value,
                )
            }

    private fun getItem(itemId: String): InventoryItem {
        val id = InventoryItemId.of(itemId)
        return repository.findById(id) ?: throw InventoryItemNotFoundException(id)
    }

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

    private fun saveUpdatedItem(itemId: String, update: (InventoryItem) -> InventoryItem): InventoryItemView =
        repository.save(update(getItem(itemId))).toView()

    private fun InventoryItem.matches(searchTerm: String?): Boolean =
        searchTerm.isNullOrBlank() || name.normalizedValue.contains(searchTerm.trim().lowercase(Locale.ROOT))

    private fun inventoryItemComparator(sort: InventoryItemSort): Comparator<InventoryItem> = when (sort) {
        InventoryItemSort.NAME -> compareBy { item -> item.name.normalizedValue }
        InventoryItemSort.CRITICAL_FIRST -> compareByDescending<InventoryItem> { item -> item.isBelowMinimumStock }
            .thenBy { item -> item.name.normalizedValue }
        InventoryItemSort.CURRENT_STOCK_ASCENDING -> compareBy<InventoryItem> { item -> item.currentStock.value }
            .thenBy { item -> item.name.normalizedValue }
        InventoryItemSort.CURRENT_STOCK_DESCENDING -> compareByDescending<InventoryItem> { item -> item.currentStock.value }
            .thenBy { item -> item.name.normalizedValue }
    }

    private fun shoppingListItemComparator(sort: ShoppingListSort) = when (sort) {
        ShoppingListSort.NAME -> compareBy { item -> item.itemName.normalizedValue }
        ShoppingListSort.RECOMMENDED_PURCHASE_QUANTITY_DESCENDING ->
            compareByDescending<org.nordicthings.stocktracker.inventory.domain.ShoppingListItem> {
                item -> item.recommendedPurchaseQuantity
            }.thenBy { item -> item.itemName.normalizedValue }
        ShoppingListSort.RECOMMENDED_PURCHASE_QUANTITY_ASCENDING ->
            compareBy<org.nordicthings.stocktracker.inventory.domain.ShoppingListItem> {
                item -> item.recommendedPurchaseQuantity
            }.thenBy { item -> item.itemName.normalizedValue }
    }

    private fun InventoryItem.toView(): InventoryItemView = InventoryItemView(
        id = id.value,
        name = name.value,
        currentStock = currentStock.value,
        minimumStock = minimumStock.value,
        targetStock = targetStock.value,
        note = note?.value,
        isBelowMinimumStock = isBelowMinimumStock,
    )
}
