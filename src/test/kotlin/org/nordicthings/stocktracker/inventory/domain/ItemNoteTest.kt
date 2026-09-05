package org.nordicthings.stocktracker.inventory.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ItemNoteTest {

    @Test
    fun `creates note from non-blank text`() {
        val note = ItemNote.optional("  Nur ungesuesst kaufen.  ")

        assertEquals("Nur ungesuesst kaufen.", note?.value)
    }

    @Test
    fun `blank note becomes null`() {
        assertNull(ItemNote.optional("   "))
    }

    @Test
    fun `missing note becomes null`() {
        assertNull(ItemNote.optional(null))
    }

    @Test
    fun `rejects note longer than 500 characters`() {
        assertFailsWith<InvalidStockOperationException> {
            ItemNote.optional("x".repeat(501))
        }
    }
}
