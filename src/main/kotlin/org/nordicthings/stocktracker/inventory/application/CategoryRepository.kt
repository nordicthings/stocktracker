package org.nordicthings.stocktracker.inventory.application

import org.nordicthings.stocktracker.inventory.domain.Category
import org.nordicthings.stocktracker.inventory.domain.CategoryId

interface CategoryRepository {
    fun save(category: Category): Category

    fun findById(id: CategoryId): Category?

    fun findAll(): List<Category>

    fun deleteById(id: CategoryId)

    fun existsByNormalizedName(normalizedName: String): Boolean

    fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: CategoryId): Boolean
}
