package org.nordicthings.stocktracker.inventory.adapter.persistence

import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.nordicthings.stocktracker.inventory.domain.CurrentStock
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.ItemName
import org.nordicthings.stocktracker.inventory.domain.ItemNote
import org.nordicthings.stocktracker.inventory.domain.MinimumStock
import org.nordicthings.stocktracker.inventory.domain.TargetStock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:inventory-persistence-test;MODE=MariaDB;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    ],
)
class InventoryItemJpaAdapterTest @Autowired constructor(
    private val repository: InventoryItemJpaAdapter,
    private val jpaRepository: InventoryItemJpaRepository,
    private val dispatchRepository: ShoppingListEmailDispatchJpaAdapter,
    private val dispatchJpaRepository: ShoppingListEmailDispatchJpaRepository,
) {

    @BeforeTest
    fun clearRepository() {
        jpaRepository.deleteAll()
        dispatchJpaRepository.deleteAll()
    }

    @Test
    fun `stores and restores all domain data`() {
        val item = createItem(note = "Bio bevorzugen")

        val savedItem = repository.save(item)
        val restoredItem = assertNotNull(repository.findById(item.id))

        assertEquals(item.id, savedItem.id)
        assertEquals(item.id, restoredItem.id)
        assertEquals("Nudeln (500g)", restoredItem.name.value)
        assertEquals(2, restoredItem.currentStock.value)
        assertEquals(3, restoredItem.minimumStock.value)
        assertEquals(5, restoredItem.targetStock.value)
        assertEquals("Bio bevorzugen", restoredItem.note?.value)
    }

    @Test
    fun `finds names by normalized value and excludes an item id`() {
        val noodles = repository.save(createItem(name = "Nudeln (500g)"))
        val rice = repository.save(createItem(name = "Reis (1kg)"))

        assertTrue(repository.existsByNormalizedName("nudeln (500g)"))
        assertFalse(repository.existsByNormalizedNameExcludingId("nudeln (500g)", noodles.id))
        assertTrue(repository.existsByNormalizedNameExcludingId("nudeln (500g)", rice.id))
    }

    @Test
    fun `preserves creation time and updates modification time when saving an existing item`() {
        val item = repository.save(createItem())
        val createdEntity = assertNotNull(jpaRepository.findById(item.id.value).orElse(null))

        repository.save(item.setCurrentStock(CurrentStock.of(4)))
        val updatedEntity = assertNotNull(jpaRepository.findById(item.id.value).orElse(null))

        assertEquals(createdEntity.createdAt, updatedEntity.createdAt)
        assertTrue(updatedEntity.updatedAt >= createdEntity.updatedAt)
    }

    @Test
    fun `deletes an item`() {
        val item = repository.save(createItem())

        repository.deleteById(item.id)

        assertFalse(repository.findById(item.id) != null)
        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    fun `stores successful email dispatches and exposes their sequence`() {
        val sentAt = Instant.parse("2026-09-06T10:15:30Z")

        assertEquals(1, dispatchRepository.nextDispatchNumber())
        dispatchRepository.saveSuccessfulDispatch(1, sentAt)

        assertEquals(sentAt, dispatchRepository.findLastSuccessfulDispatchAt())
        assertEquals(2, dispatchRepository.nextDispatchNumber())
    }

    private fun createItem(
        name: String = "Nudeln (500g)",
        note: String? = null,
    ): InventoryItem = InventoryItem.create(
        name = ItemName.of(name),
        currentStock = CurrentStock.of(2),
        minimumStock = MinimumStock.of(3),
        targetStock = TargetStock.of(5),
        note = ItemNote.optional(note),
    )
}
