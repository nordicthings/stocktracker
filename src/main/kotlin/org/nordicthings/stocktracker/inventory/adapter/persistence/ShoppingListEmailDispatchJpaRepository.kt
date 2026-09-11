package org.nordicthings.stocktracker.inventory.adapter.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ShoppingListEmailDispatchJpaRepository : JpaRepository<ShoppingListEmailDispatchJpaEntity, Long> {
    fun findTopByOrderByDispatchNumberDesc(): ShoppingListEmailDispatchJpaEntity?
}
