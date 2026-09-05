package org.nordicthings.stocktracker.inventory.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InventoryItemTest {

    @Test
    fun `creates item with generated id`() {
        val item = createItem()

        assertNotNull(item.id.value)
        assertEquals(ItemName.of("Nudeln (500g)"), item.name)
    }

    @Test
    fun `reconstitutes item with existing id`() {
        val id = InventoryItemId.of("550e8400-e29b-41d4-a716-446655440000")

        val item = InventoryItem.reconstitute(
            id = id,
            name = ItemName.of("Nudeln (500g)"),
            currentStock = CurrentStock.of(2),
            minimumStock = MinimumStock.of(3),
            targetStock = TargetStock.of(5),
            note = null,
        )

        assertEquals(id, item.id)
    }

    @Test
    fun `rejects target stock below minimum stock`() {
        assertFailsWith<InvalidStockConfigurationException> {
            createItem(
                minimumStock = MinimumStock.of(5),
                targetStock = TargetStock.of(4),
            )
        }
    }

    @Test
    fun `detects item below minimum stock`() {
        val item = createItem(
            currentStock = CurrentStock.of(2),
            minimumStock = MinimumStock.of(3),
        )

        assertTrue(item.isBelowMinimumStock)
    }

    @Test
    fun `does not mark item at minimum stock as below minimum`() {
        val item = createItem(
            currentStock = CurrentStock.of(3),
            minimumStock = MinimumStock.of(3),
        )

        assertFalse(item.isBelowMinimumStock)
    }

    @Test
    fun `detects item below target stock`() {
        val item = createItem(
            currentStock = CurrentStock.of(4),
            minimumStock = MinimumStock.of(3),
            targetStock = TargetStock.of(5),
        )

        assertTrue(item.isBelowTargetStock)
    }

    @Test
    fun `calculates recommended purchase quantity up to target stock`() {
        val item = createItem(
            currentStock = CurrentStock.of(2),
            minimumStock = MinimumStock.of(3),
            targetStock = TargetStock.of(5),
        )

        assertEquals(3, item.recommendedPurchaseQuantity)
    }

    @Test
    fun `recommended purchase quantity is zero when current stock exceeds target stock`() {
        val item = createItem(
            currentStock = CurrentStock.of(7),
            minimumStock = MinimumStock.of(3),
            targetStock = TargetStock.of(5),
        )

        assertEquals(0, item.recommendedPurchaseQuantity)
    }

    @Test
    fun `stock changes create new instance`() {
        val item = createItem(currentStock = CurrentStock.of(2))

        val updatedItem = item.increaseCurrentStockBy(1)

        assertNotSame(item, updatedItem)
        assertEquals(CurrentStock.of(2), item.currentStock)
        assertEquals(CurrentStock.of(3), updatedItem.currentStock)
    }

    @Test
    fun `sets current stock directly`() {
        val item = createItem(currentStock = CurrentStock.of(2))

        val updatedItem = item.setCurrentStock(CurrentStock.of(8))

        assertEquals(CurrentStock.of(8), updatedItem.currentStock)
    }

    @Test
    fun `decreases current stock`() {
        val item = createItem(currentStock = CurrentStock.of(4))

        val updatedItem = item.decreaseCurrentStockBy(2)

        assertEquals(CurrentStock.of(2), updatedItem.currentStock)
    }

    @Test
    fun `removes one item`() {
        val item = createItem(currentStock = CurrentStock.of(4))

        val updatedItem = item.removeOne()

        assertEquals(CurrentStock.of(3), updatedItem.currentStock)
    }

    @Test
    fun `sets stock to target when current stock is below target`() {
        val item = createItem(
            currentStock = CurrentStock.of(2),
            targetStock = TargetStock.of(5),
        )

        val updatedItem = item.setStockToTarget()

        assertEquals(CurrentStock.of(5), updatedItem.currentStock)
    }

    @Test
    fun `rejects setting stock to target when current stock is already at target`() {
        val item = createItem(
            currentStock = CurrentStock.of(5),
            targetStock = TargetStock.of(5),
        )

        assertFailsWith<InvalidStockOperationException> {
            item.setStockToTarget()
        }
    }

    @Test
    fun `rejects setting stock to target when current stock is above target`() {
        val item = createItem(
            currentStock = CurrentStock.of(6),
            targetStock = TargetStock.of(5),
        )

        assertFailsWith<InvalidStockOperationException> {
            item.setStockToTarget()
        }
    }

    @Test
    fun `edits item data while keeping id`() {
        val item = createItem(currentStock = CurrentStock.of(2))

        val updatedItem = item.edit(
            name = ItemName.of("Spaghetti (500g)"),
            currentStock = CurrentStock.of(3),
            minimumStock = MinimumStock.of(4),
            targetStock = TargetStock.of(6),
            note = ItemNote.optional("Nur Vollkorn."),
        )

        assertEquals(item.id, updatedItem.id)
        assertEquals(CurrentStock.of(3), updatedItem.currentStock)
        assertEquals(ItemName.of("Spaghetti (500g)"), updatedItem.name)
        assertEquals(MinimumStock.of(4), updatedItem.minimumStock)
        assertEquals(TargetStock.of(6), updatedItem.targetStock)
        assertEquals(ItemNote.optional("Nur Vollkorn."), updatedItem.note)
    }

    @Test
    fun `creates shopping list item when below target stock`() {
        val item = createItem(
            currentStock = CurrentStock.of(3),
            minimumStock = MinimumStock.of(3),
            targetStock = TargetStock.of(5),
            note = ItemNote.optional("Bio bevorzugt."),
        )

        val shoppingListItem = item.toShoppingListItem()

        assertNotNull(shoppingListItem)
        assertEquals(item.id, shoppingListItem.itemId)
        assertEquals(item.name, shoppingListItem.itemName)
        assertEquals(2, shoppingListItem.recommendedPurchaseQuantity)
        assertFalse(shoppingListItem.isBelowMinimumStock)
        assertEquals(item.note, shoppingListItem.note)
    }

    @Test
    fun `does not create shopping list item when target stock is reached`() {
        val item = createItem(
            currentStock = CurrentStock.of(5),
            minimumStock = MinimumStock.of(3),
            targetStock = TargetStock.of(5),
        )

        assertNull(item.toShoppingListItem())
    }

    private fun createItem(
        name: ItemName = ItemName.of("Nudeln (500g)"),
        currentStock: CurrentStock = CurrentStock.of(2),
        minimumStock: MinimumStock = MinimumStock.of(3),
        targetStock: TargetStock = TargetStock.of(5),
        note: ItemNote? = null,
    ): InventoryItem = InventoryItem.create(
        name = name,
        currentStock = currentStock,
        minimumStock = minimumStock,
        targetStock = targetStock,
        note = note,
    )
}
