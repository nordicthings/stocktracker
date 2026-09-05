package org.nordicthings.stocktracker.inventory.adapter.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface InventoryItemJpaRepository : JpaRepository<InventoryItemJpaEntity, String> {
    fun existsByNormalizedName(normalizedName: String): Boolean

    fun existsByNormalizedNameAndIdNot(normalizedName: String, id: String): Boolean
}
