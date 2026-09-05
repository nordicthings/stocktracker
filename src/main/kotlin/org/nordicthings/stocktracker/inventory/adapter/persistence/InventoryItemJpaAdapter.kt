package org.nordicthings.stocktracker.inventory.adapter.persistence

import java.time.Clock
import org.nordicthings.stocktracker.inventory.application.InventoryItemRepository
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class InventoryItemJpaAdapter(
    private val jpaRepository: InventoryItemJpaRepository,
    private val mapper: InventoryItemJpaMapper,
    private val clock: Clock = Clock.systemUTC(),
) : InventoryItemRepository {

    @Transactional
    override fun save(item: InventoryItem): InventoryItem {
        val existingEntity = jpaRepository.findById(item.id.value).orElse(null)
        val now = clock.instant()
        val entity = mapper.toEntity(
            item = item,
            createdAt = existingEntity?.createdAt ?: now,
            updatedAt = now,
        )

        return mapper.toDomain(jpaRepository.save(entity))
    }

    @Transactional(readOnly = true)
    override fun findById(id: InventoryItemId): InventoryItem? =
        jpaRepository.findById(id.value)
            .map(mapper::toDomain)
            .orElse(null)

    @Transactional(readOnly = true)
    override fun findAll(): List<InventoryItem> =
        jpaRepository.findAll().map(mapper::toDomain)

    @Transactional
    override fun deleteById(id: InventoryItemId) {
        jpaRepository.deleteById(id.value)
    }

    @Transactional(readOnly = true)
    override fun existsByNormalizedName(normalizedName: String): Boolean =
        jpaRepository.existsByNormalizedName(normalizedName)

    @Transactional(readOnly = true)
    override fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: InventoryItemId): Boolean =
        jpaRepository.existsByNormalizedNameAndIdNot(normalizedName, excludedId.value)
}
