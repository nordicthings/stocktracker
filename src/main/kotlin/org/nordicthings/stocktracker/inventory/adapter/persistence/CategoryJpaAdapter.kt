package org.nordicthings.stocktracker.inventory.adapter.persistence

import org.nordicthings.stocktracker.inventory.application.CategoryRepository
import org.nordicthings.stocktracker.inventory.domain.Category
import org.nordicthings.stocktracker.inventory.domain.CategoryId
import org.nordicthings.stocktracker.inventory.domain.CategoryName
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CategoryJpaAdapter(
    private val jpaRepository: CategoryJpaRepository,
) : CategoryRepository {

    @Transactional
    override fun save(category: Category): Category = toDomain(
        jpaRepository.save(
            CategoryJpaEntity(
                id = category.id.value,
                name = category.name.value,
                normalizedName = category.name.normalizedValue,
            ),
        ),
    )

    @Transactional(readOnly = true)
    override fun findById(id: CategoryId): Category? = jpaRepository.findById(id.value).map(::toDomain).orElse(null)

    @Transactional(readOnly = true)
    override fun findAll(): List<Category> = jpaRepository.findAll().map(::toDomain)

    @Transactional
    override fun deleteById(id: CategoryId) {
        jpaRepository.deleteById(id.value)
    }

    @Transactional(readOnly = true)
    override fun existsByNormalizedName(normalizedName: String): Boolean =
        jpaRepository.existsByNormalizedName(normalizedName)

    @Transactional(readOnly = true)
    override fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: CategoryId): Boolean =
        jpaRepository.existsByNormalizedNameAndIdNot(normalizedName, excludedId.value)

    private fun toDomain(entity: CategoryJpaEntity): Category = Category.reconstitute(
        CategoryId.of(entity.id),
        CategoryName.of(entity.name),
    )
}
