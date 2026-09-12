package org.nordicthings.stocktracker.inventory.adapter.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryJpaRepository : JpaRepository<CategoryJpaEntity, String> {
    fun existsByNormalizedName(normalizedName: String): Boolean

    fun existsByNormalizedNameAndIdNot(normalizedName: String, id: String): Boolean
}
