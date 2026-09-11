package org.nordicthings.stocktracker.inventory.adapter.persistence

import java.time.Instant
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailDispatchRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ShoppingListEmailDispatchJpaAdapter(
    private val jpaRepository: ShoppingListEmailDispatchJpaRepository,
) : ShoppingListEmailDispatchRepository {

    @Transactional(readOnly = true)
    override fun findLastSuccessfulDispatchAt(): Instant? =
        jpaRepository.findTopByOrderByDispatchNumberDesc()?.sentAt

    @Transactional(readOnly = true)
    override fun nextDispatchNumber(): Long =
        (jpaRepository.findTopByOrderByDispatchNumberDesc()?.dispatchNumber ?: 0) + 1

    @Transactional
    override fun saveSuccessfulDispatch(dispatchNumber: Long, sentAt: Instant) {
        jpaRepository.save(
            ShoppingListEmailDispatchJpaEntity(
                dispatchNumber = dispatchNumber,
                sentAt = sentAt,
            ),
        )
    }
}
