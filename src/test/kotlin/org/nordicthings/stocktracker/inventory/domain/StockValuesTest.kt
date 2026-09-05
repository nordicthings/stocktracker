package org.nordicthings.stocktracker.inventory.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StockValuesTest {

    @Test
    fun `current stock may be zero`() {
        assertEquals(0, CurrentStock.of(0).value)
    }

    @Test
    fun `current stock rejects negative value`() {
        assertFailsWith<InvalidCurrentStockException> {
            CurrentStock.of(-1)
        }
    }

    @Test
    fun `minimum stock must be at least one`() {
        assertEquals(1, MinimumStock.of(1).value)

        assertFailsWith<InvalidMinimumStockException> {
            MinimumStock.of(0)
        }
    }

    @Test
    fun `target stock must be at least one`() {
        assertEquals(1, TargetStock.of(1).value)

        assertFailsWith<InvalidTargetStockException> {
            TargetStock.of(0)
        }
    }

    @Test
    fun `current stock can be increased by positive quantity`() {
        val currentStock = CurrentStock.of(2)

        assertEquals(CurrentStock.of(5), currentStock + 3)
    }

    @Test
    fun `current stock rejects non-positive increase quantity`() {
        val currentStock = CurrentStock.of(2)

        assertFailsWith<InvalidStockOperationException> {
            currentStock + 0
        }
    }

    @Test
    fun `current stock can be decreased by positive quantity`() {
        val currentStock = CurrentStock.of(5)

        assertEquals(CurrentStock.of(2), currentStock - 3)
    }

    @Test
    fun `current stock rejects decrease below zero`() {
        val currentStock = CurrentStock.of(2)

        assertFailsWith<InvalidStockOperationException> {
            currentStock - 3
        }
    }
}
