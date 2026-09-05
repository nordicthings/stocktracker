package org.nordicthings.stocktracker.inventory.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class InventoryItemIdTest {

    @Test
    fun `accepts valid UUID string`() {
        val id = InventoryItemId.of("550e8400-e29b-41d4-a716-446655440000")

        assertEquals("550e8400-e29b-41d4-a716-446655440000", id.value)
    }

    @Test
    fun `rejects invalid UUID string`() {
        assertFailsWith<InvalidInventoryItemIdException> {
            InventoryItemId.of("not-a-uuid")
        }
    }

    @Test
    fun `generates distinct ids`() {
        val first = InventoryItemId.newId()
        val second = InventoryItemId.newId()

        assertNotEquals(first, second)
    }
}
