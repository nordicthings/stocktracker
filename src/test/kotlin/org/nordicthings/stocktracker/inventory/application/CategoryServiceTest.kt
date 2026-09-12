package org.nordicthings.stocktracker.inventory.application

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.nordicthings.stocktracker.inventory.domain.Category
import org.nordicthings.stocktracker.inventory.domain.CategoryId
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId

class CategoryServiceTest {

    @Test
    fun `creates categories and rejects duplicate normalized names`() {
        val service = service()
        val created = service.create(CreateCategoryCommand("Konserven"))

        assertEquals("Konserven", created.name)
        assertFailsWith<DuplicateCategoryNameException> { service.create(CreateCategoryCommand(" konserven ")) }
    }

    @Test
    fun `does not delete categories that contain articles`() {
        val categories = FakeCategoryRepository()
        val category = categories.save(Category.create(org.nordicthings.stocktracker.inventory.domain.CategoryName.of("Obst")))
        val service = CategoryService(categories, FakeInventoryItemRepository(setOf(category.id)))

        assertFailsWith<CategoryContainsInventoryItemsException> { service.delete(category.id.value) }
    }

    @Test
    fun `does not delete the system category`() {
        assertFailsWith<SystemCategoryDeletionException> { service().delete(Category.SYSTEM_CATEGORY_ID.value) }
    }

    @Test
    fun `lists categories in selected alphabetical order`() {
        val categories = FakeCategoryRepository()
        val fruit = categories.save(Category.create(org.nordicthings.stocktracker.inventory.domain.CategoryName.of("Obst")))
        categories.save(Category.create(org.nordicthings.stocktracker.inventory.domain.CategoryName.of("Backwaren")))
        val service = CategoryService(categories, FakeInventoryItemRepository(setOf(fruit.id)))

        val categoriesDescending = service.viewCategories(CategorySort.NAME_DESCENDING)

        assertEquals(listOf("Obst", "Backwaren", "--ohne--"), categoriesDescending.map { it.name })
        assertTrue(categoriesDescending.last().isSystemCategory)
    }

    private fun service(): CategoryService = CategoryService(FakeCategoryRepository(), FakeInventoryItemRepository())

    private class FakeCategoryRepository : CategoryRepository {
        private val categories = mutableMapOf(Category.SYSTEM_CATEGORY_ID to Category.reconstitute(Category.SYSTEM_CATEGORY_ID, Category.SYSTEM_CATEGORY_NAME))
        override fun save(category: Category): Category = category.also { categories[it.id] = it }
        override fun findById(id: CategoryId): Category? = categories[id]
        override fun findAll(): List<Category> = categories.values.toList()
        override fun deleteById(id: CategoryId) { categories.remove(id) }
        override fun existsByNormalizedName(normalizedName: String): Boolean = categories.values.any { it.name.normalizedValue == normalizedName }
        override fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: CategoryId): Boolean = categories.values.any { it.id != excludedId && it.name.normalizedValue == normalizedName }
    }

    private class FakeInventoryItemRepository(private val usedCategoryIds: Set<CategoryId> = emptySet()) : InventoryItemRepository {
        override fun save(item: InventoryItem): InventoryItem = error("Not used")
        override fun findById(id: InventoryItemId): InventoryItem? = null
        override fun findAll(): List<InventoryItem> = emptyList()
        override fun findAllUpdatedAfter(instant: Instant): List<InventoryItem> = emptyList()
        override fun deleteById(id: InventoryItemId) = error("Not used")
        override fun existsByCategoryId(categoryId: CategoryId): Boolean = categoryId in usedCategoryIds
        override fun existsByNormalizedName(normalizedName: String): Boolean = false
        override fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: InventoryItemId): Boolean = false
    }
}
