package org.nordicthings.stocktracker.inventory.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CategoryTest {

    @Test
    fun `normalizes category names like item names`() {
        val name = CategoryName.of("  Obst   und  Gemüse  ")

        assertEquals("Obst und Gemüse", name.value)
        assertEquals("obst und gemüse", name.normalizedValue)
    }

    @Test
    fun `rejects blank and too long category names`() {
        assertFailsWith<InvalidCategoryNameException> { CategoryName.of("   ") }
        assertFailsWith<CategoryNameTooLongException> { CategoryName.of("a".repeat(31)) }
    }

    @Test
    fun `does not allow the system category to be renamed`() {
        val category = Category.reconstitute(Category.SYSTEM_CATEGORY_ID, Category.SYSTEM_CATEGORY_NAME)

        assertFailsWith<SystemCategoryModificationException> { category.rename(CategoryName.of("Sonstiges")) }
    }
}
