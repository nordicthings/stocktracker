package org.nordicthings.stocktracker.inventory.application

import org.nordicthings.stocktracker.inventory.domain.Category
import org.nordicthings.stocktracker.inventory.domain.CategoryId
import org.nordicthings.stocktracker.inventory.domain.CategoryName
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val inventoryItemRepository: InventoryItemRepository,
) : CreateCategoryUseCase, EditCategoryUseCase, DeleteCategoryUseCase, ViewCategoryUseCase, ViewCategoriesUseCase {

    override fun create(command: CreateCategoryCommand): CategoryView {
        val name = CategoryName.of(command.name)
        ensureNameIsAvailable(name)
        return categoryRepository.save(Category.create(name)).toView(articleCount = 0)
    }

    override fun edit(command: EditCategoryCommand): CategoryView {
        val category = getCategory(command.categoryId)
        val name = CategoryName.of(command.name)
        ensureNameIsAvailable(name, category.id)
        return categoryRepository.save(category.rename(name)).toView(articleCount(category.id))
    }

    override fun delete(categoryId: String) {
        val category = getCategory(categoryId)
        if (category.isSystemCategory) {
            throw SystemCategoryDeletionException()
        }
        if (inventoryItemRepository.existsByCategoryId(category.id)) {
            throw CategoryContainsInventoryItemsException()
        }
        categoryRepository.deleteById(category.id)
    }

    override fun viewCategory(categoryId: String): CategoryView {
        val category = getCategory(categoryId)
        return category.toView(articleCount(category.id))
    }

    override fun viewCategories(sort: CategorySort): List<CategoryView> {
        val articleCounts = inventoryItemRepository.findAll()
            .groupingBy { it.categoryId }
            .eachCount()
        return categoryRepository.findAll()
            .sortedWith(categoryComparator(sort))
            .map { category -> category.toView(articleCounts[category.id] ?: 0) }
    }

    private fun getCategory(categoryId: String): Category {
        val id = CategoryId.of(categoryId)
        return categoryRepository.findById(id) ?: throw CategoryNotFoundException(id)
    }

    private fun articleCount(categoryId: CategoryId): Int = inventoryItemRepository.findAll().count { it.categoryId == categoryId }

    private fun ensureNameIsAvailable(name: CategoryName, excludedId: CategoryId? = null) {
        val exists = if (excludedId == null) {
            categoryRepository.existsByNormalizedName(name.normalizedValue)
        } else {
            categoryRepository.existsByNormalizedNameExcludingId(name.normalizedValue, excludedId)
        }
        if (exists) {
            throw DuplicateCategoryNameException(name.value)
        }
    }

    private fun categoryComparator(sort: CategorySort): Comparator<Category> = when (sort) {
        CategorySort.NAME_ASCENDING -> compareBy { it.name.normalizedValue }
        CategorySort.NAME_DESCENDING -> compareByDescending<Category> { it.name.normalizedValue }
    }

    private fun Category.toView(articleCount: Int): CategoryView = CategoryView(
        id = id.value,
        name = name.value,
        articleCount = articleCount,
        isSystemCategory = isSystemCategory,
    )
}
