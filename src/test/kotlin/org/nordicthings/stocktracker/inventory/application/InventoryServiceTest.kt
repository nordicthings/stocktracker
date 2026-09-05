package org.nordicthings.stocktracker.inventory.application

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId

class InventoryServiceTest {

    @Test
    fun `creates an item and returns its view`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)

        val item = service.create(createCommand(name = "Nudeln (500g)"))

        assertEquals("Nudeln (500g)", item.name)
        assertEquals(2, item.currentStock)
        assertTrue(item.isBelowMinimumStock)
        assertTrue(item.isBelowTargetStock)
        assertEquals(item.id, repository.findAll().single().id.value)
    }

    @Test
    fun `rejects creating an item with an existing normalized name`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        service.create(createCommand(name = "Nudeln (500g)"))

        assertFailsWith<DuplicateItemNameException> {
            service.create(createCommand(name = "  nudeln   (500G) "))
        }
    }

    @Test
    fun `edits item data including current stock`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        val createdItem = service.create(createCommand(currentStock = 2))

        val updatedItem = service.edit(
            EditInventoryItemCommand(
                itemId = createdItem.id,
                name = "Spaghetti (500g)",
                currentStock = 3,
                minimumStock = 4,
                targetStock = 6,
                note = "Vollkorn",
            ),
        )

        assertEquals(createdItem.id, updatedItem.id)
        assertEquals("Spaghetti (500g)", updatedItem.name)
        assertEquals(3, updatedItem.currentStock)
        assertEquals(4, updatedItem.minimumStock)
        assertEquals(6, updatedItem.targetStock)
        assertEquals("Vollkorn", updatedItem.note)
    }

    @Test
    fun `allows editing an item without changing its normalized name`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        val createdItem = service.create(createCommand(name = "Nudeln (500g)"))

        val updatedItem = service.edit(
            EditInventoryItemCommand(
                itemId = createdItem.id,
                name = "  nudeln   (500G)  ",
                currentStock = 2,
                minimumStock = 3,
                targetStock = 5,
                note = null,
            ),
        )

        assertEquals("nudeln (500G)", updatedItem.name)
    }

    @Test
    fun `rejects editing an item to another item's normalized name`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        val pasta = service.create(createCommand(name = "Nudeln (500g)"))
        service.create(createCommand(name = "Reis (1kg)"))

        assertFailsWith<DuplicateItemNameException> {
            service.edit(
                EditInventoryItemCommand(pasta.id, "reis (1KG)", 2, 3, 5, null),
            )
        }
    }

    @Test
    fun `requires confirmation before deleting an item`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        val item = service.create(createCommand())

        assertFailsWith<DeleteInventoryItemNotConfirmedException> {
            service.delete(DeleteInventoryItemCommand(item.id, confirmed = false))
        }

        assertEquals(1, repository.findAll().size)
    }

    @Test
    fun `deletes an existing confirmed item`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        val item = service.create(createCommand())

        service.delete(DeleteInventoryItemCommand(item.id, confirmed = true))

        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    fun `rejects updates for a missing item`() {
        val service = InventoryService(FakeInventoryItemRepository())

        assertFailsWith<InventoryItemNotFoundException> {
            service.setCurrentStock(
                SetCurrentStockCommand("550e8400-e29b-41d4-a716-446655440000", 4),
            )
        }
    }

    @Test
    fun `views a single inventory item`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        val item = service.create(createCommand(name = "Haferflocken", currentStock = 4))

        val view = service.viewInventoryItem(item.id)

        assertEquals(item.id, view.id)
        assertEquals("Haferflocken", view.name)
        assertEquals(4, view.currentStock)
    }

    @Test
    fun `changes current stock through every stock use case`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        val item = service.create(createCommand(currentStock = 2, targetStock = 6))

        assertEquals(4, service.setCurrentStock(SetCurrentStockCommand(item.id, 4)).currentStock)
        assertEquals(6, service.increaseCurrentStock(ChangeCurrentStockCommand(item.id, 2)).currentStock)
        assertEquals(3, service.decreaseCurrentStock(ChangeCurrentStockCommand(item.id, 3)).currentStock)
        assertEquals(2, service.removeOne(item.id).currentStock)
        assertEquals(6, service.setStockToTarget(item.id).currentStock)
    }

    @Test
    fun `filters inventory by a case insensitive part of the item name`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        service.create(createCommand(name = "Nudeln (500g)", currentStock = 3))
        service.create(createCommand(name = "Reis (1kg)"))

        val overview = service.viewInventoryItems(InventoryItemsQuery(searchTerm = "DELN"))

        assertEquals(listOf("Nudeln (500g)"), overview.items.map { item -> item.name })
        assertTrue(overview.hasPurchaseNeeds)
        assertEquals(1, overview.belowMinimumStockCount)
        assertEquals(2, overview.belowTargetStockCount)
    }

    @Test
    fun `sorts inventory with critical items first and name as tie breaker`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        service.create(createCommand(name = "Reis", currentStock = 3, minimumStock = 3))
        service.create(createCommand(name = "Äpfel", currentStock = 1, minimumStock = 2))
        service.create(createCommand(name = "Bohnen", currentStock = 0, minimumStock = 2))

        val overview = service.viewInventoryItems(InventoryItemsQuery(sort = InventoryItemSort.CRITICAL_FIRST))

        assertEquals(listOf("Bohnen", "Äpfel", "Reis"), overview.items.map { item -> item.name })
        assertTrue(overview.hasPurchaseNeeds)
    }

    @Test
    fun `sorts inventory by current stock in both directions`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        service.create(createCommand(name = "A", currentStock = 3))
        service.create(createCommand(name = "B", currentStock = 1))
        service.create(createCommand(name = "C", currentStock = 2))

        val ascending = service.viewInventoryItems(InventoryItemsQuery(sort = InventoryItemSort.CURRENT_STOCK_ASCENDING))
        val descending = service.viewInventoryItems(InventoryItemsQuery(sort = InventoryItemSort.CURRENT_STOCK_DESCENDING))

        assertEquals(listOf("B", "C", "A"), ascending.items.map { item -> item.name })
        assertEquals(listOf("A", "C", "B"), descending.items.map { item -> item.name })
    }

    @Test
    fun `derives and sorts the shopping list from items below target stock`() {
        val repository = FakeInventoryItemRepository()
        val service = InventoryService(repository)
        service.create(createCommand(name = "Nudeln", currentStock = 2, minimumStock = 3, targetStock = 5, note = "Bio"))
        service.create(createCommand(name = "Reis", currentStock = 0, minimumStock = 2, targetStock = 7))
        service.create(createCommand(name = "Salz", currentStock = 3, minimumStock = 3, targetStock = 5))

        val shoppingList = service.viewShoppingList(
            ShoppingListQuery(ShoppingListSort.RECOMMENDED_PURCHASE_QUANTITY_DESCENDING),
        )

        assertEquals(listOf("Reis", "Nudeln", "Salz"), shoppingList.map { item -> item.itemName })
        assertEquals(listOf(7, 3, 2), shoppingList.map { item -> item.recommendedPurchaseQuantity })
        assertEquals(listOf(true, true, false), shoppingList.map { item -> item.isBelowMinimumStock })
        assertEquals("Bio", shoppingList[1].note)
    }

    private fun createCommand(
        name: String = "Nudeln (500g)",
        currentStock: Int = 2,
        minimumStock: Int = 3,
        targetStock: Int = 5,
        note: String? = null,
    ) = CreateInventoryItemCommand(name, currentStock, minimumStock, targetStock, note)

    private class FakeInventoryItemRepository : InventoryItemRepository {
        private val items = mutableMapOf<InventoryItemId, InventoryItem>()

        override fun save(item: InventoryItem): InventoryItem {
            items[item.id] = item
            return item
        }

        override fun findById(id: InventoryItemId): InventoryItem? = items[id]

        override fun findAll(): List<InventoryItem> = items.values.toList()

        override fun deleteById(id: InventoryItemId) {
            items.remove(id)
        }

        override fun existsByNormalizedName(normalizedName: String): Boolean =
            items.values.any { item -> item.name.normalizedValue == normalizedName }

        override fun existsByNormalizedNameExcludingId(
            normalizedName: String,
            excludedId: InventoryItemId,
        ): Boolean = items.values.any { item ->
            item.id != excludedId && item.name.normalizedValue == normalizedName
        }
    }
}
