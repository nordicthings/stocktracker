package org.nordicthings.stocktracker.inventory.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ItemNameTest {

    @Test
    fun `normalizes whitespace and case for comparison`() {
        val name = ItemName.of("  Passierte   Tomaten (500g)  ")

        assertEquals("Passierte Tomaten (500g)", name.value)
        assertEquals("passierte tomaten (500g)", name.normalizedValue)
    }

    @Test
    fun `uses normalized value for equality`() {
        val first = ItemName.of("Passierte   Tomaten (500g)")
        val second = ItemName.of("passierte tomaten (500g)")

        assertEquals(first, second)
    }

    @Test
    fun `rejects blank name`() {
        assertFailsWith<InvalidItemNameException> {
            ItemName.of("   ")
        }
    }
}
