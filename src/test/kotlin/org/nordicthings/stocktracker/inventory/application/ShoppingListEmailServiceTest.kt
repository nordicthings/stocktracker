package org.nordicthings.stocktracker.inventory.application

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.nordicthings.stocktracker.inventory.domain.CurrentStock
import org.nordicthings.stocktracker.inventory.domain.Category
import org.nordicthings.stocktracker.inventory.domain.CategoryId
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.nordicthings.stocktracker.inventory.domain.InventoryItemId
import org.nordicthings.stocktracker.inventory.domain.ItemName
import org.nordicthings.stocktracker.inventory.domain.MinimumStock
import org.nordicthings.stocktracker.inventory.domain.TargetStock

class ShoppingListEmailServiceTest {

    @Test
    fun `manually sends the current shopping list independent of the scheduled condition`() {
        val sender = FakeEmailSender()
        val dispatches = FakeDispatchRepository()
        val service = service(
            items = listOf(item("Nudeln", currentStock = 2, minimumStock = 1, targetStock = 5)),
            sender = sender,
            dispatches = dispatches,
            condition = ShoppingListEmailCondition.CHANGED_ITEMS_BELOW_MINIMUM_STOCK,
        )

        val dispatchNumber = service.sendShoppingListEmail()

        assertEquals(1, dispatchNumber)
        assertEquals(1, sender.sentEmails.size)
        assertEquals(listOf("Nudeln"), sender.sentEmails.single().items.map { it.itemName })
        assertEquals(listOf(3), sender.sentEmails.single().items.map { it.purchaseQuantity })
        assertEquals(1, dispatches.successfulDispatches.size)
    }

    @Test
    fun `does not persist a dispatch when mail sending fails`() {
        val sender = FakeEmailSender(fail = true)
        val dispatches = FakeDispatchRepository()
        val service = service(listOf(item("Reis")), sender, dispatches)

        assertFailsWith<ShoppingListEmailDeliveryException> { service.sendShoppingListEmail() }

        assertTrue(dispatches.successfulDispatches.isEmpty())
    }

    @Test
    fun `scheduled check sends only when a changed item matches the condition`() {
        val sender = FakeEmailSender()
        val dispatches = FakeDispatchRepository(lastDispatchAt = Instant.parse("2026-09-06T08:00:00Z"))
        val belowMinimum = item("Reis", currentStock = 0, minimumStock = 2, targetStock = 5)
        val service = service(
            items = listOf(belowMinimum),
            sender = sender,
            dispatches = dispatches,
            condition = ShoppingListEmailCondition.CHANGED_ITEMS_BELOW_MINIMUM_STOCK,
        )

        service.checkScheduledShoppingListEmail()

        assertEquals(1, sender.sentEmails.size)
        assertEquals(1, dispatches.successfulDispatches.size)
    }

    @Test
    fun `scheduled check skips changed items that do not match the condition`() {
        val sender = FakeEmailSender()
        val dispatches = FakeDispatchRepository(lastDispatchAt = Instant.parse("2026-09-06T08:00:00Z"))
        val service = service(
            items = listOf(item("Nudeln", currentStock = 2, minimumStock = 1, targetStock = 5)),
            sender = sender,
            dispatches = dispatches,
            condition = ShoppingListEmailCondition.CHANGED_ITEMS_BELOW_MINIMUM_STOCK,
        )

        service.checkScheduledShoppingListEmail()

        assertTrue(sender.sentEmails.isEmpty())
        assertTrue(dispatches.successfulDispatches.isEmpty())
    }

    private fun service(
        items: List<InventoryItem>,
        sender: FakeEmailSender,
        dispatches: FakeDispatchRepository,
        condition: ShoppingListEmailCondition = ShoppingListEmailCondition.CHANGED_ITEMS_BELOW_TARGET_STOCK,
    ): ShoppingListEmailService = ShoppingListEmailService(
        FakeInventoryItemRepository(items),
        FakeCategoryRepository(),
        dispatches,
        sender,
        object : ShoppingListEmailSettings {
            override val recipients = listOf("shopping@example.test")
            override val condition = condition
        },
        Clock.fixed(Instant.parse("2026-09-06T10:00:00Z"), ZoneOffset.UTC),
    )

    private fun item(
        name: String,
        currentStock: Int = 1,
        minimumStock: Int = 2,
        targetStock: Int = 5,
    ): InventoryItem = InventoryItem.create(
        ItemName.of(name),
        Category.SYSTEM_CATEGORY_ID,
        CurrentStock.of(currentStock),
        MinimumStock.of(minimumStock),
        TargetStock.of(targetStock),
        null,
    )

    private class FakeEmailSender(private val fail: Boolean = false) : ShoppingListEmailSender {
        val sentEmails = mutableListOf<ShoppingListEmail>()

        override fun send(email: ShoppingListEmail) {
            if (fail) error("SMTP unavailable")
            sentEmails += email
        }
    }

    private class FakeDispatchRepository(
        private var lastDispatchAt: Instant? = null,
    ) : ShoppingListEmailDispatchRepository {
        val successfulDispatches = mutableListOf<Pair<Long, Instant>>()

        override fun findLastSuccessfulDispatchAt(): Instant? = lastDispatchAt

        override fun nextDispatchNumber(): Long = (successfulDispatches.maxOfOrNull { it.first } ?: 0) + 1

        override fun saveSuccessfulDispatch(dispatchNumber: Long, sentAt: Instant) {
            successfulDispatches += dispatchNumber to sentAt
            lastDispatchAt = sentAt
        }
    }

    private class FakeInventoryItemRepository(
        private val items: List<InventoryItem>,
    ) : InventoryItemRepository {
        override fun save(item: InventoryItem): InventoryItem = error("Not used")
        override fun findById(id: InventoryItemId): InventoryItem? = items.firstOrNull { it.id == id }
        override fun findAll(): List<InventoryItem> = items
        override fun findAllUpdatedAfter(instant: Instant): List<InventoryItem> = items
        override fun deleteById(id: InventoryItemId) = error("Not used")
        override fun existsByCategoryId(categoryId: CategoryId): Boolean = items.any { it.categoryId == categoryId }
        override fun existsByNormalizedName(normalizedName: String): Boolean = false
        override fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: InventoryItemId): Boolean = false
    }

    private class FakeCategoryRepository : CategoryRepository {
        private val category = Category.reconstitute(Category.SYSTEM_CATEGORY_ID, Category.SYSTEM_CATEGORY_NAME)

        override fun save(category: Category): Category = error("Not used")
        override fun findById(id: CategoryId): Category? = if (id == category.id) category else null
        override fun findAll(): List<Category> = listOf(category)
        override fun deleteById(id: CategoryId) = error("Not used")
        override fun existsByNormalizedName(normalizedName: String): Boolean = false
        override fun existsByNormalizedNameExcludingId(normalizedName: String, excludedId: CategoryId): Boolean = false
    }
}
