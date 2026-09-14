package org.nordicthings.stocktracker.inventory.application

import java.time.Clock
import java.time.Instant
import org.slf4j.LoggerFactory
import org.nordicthings.stocktracker.inventory.domain.InventoryItem
import org.springframework.stereotype.Service

@Service
class ShoppingListEmailService(
    private val inventoryItemRepository: InventoryItemRepository,
    private val categoryRepository: CategoryRepository,
    private val dispatchRepository: ShoppingListEmailDispatchRepository,
    private val emailSender: ShoppingListEmailSender,
    private val settings: ShoppingListEmailSettings,
    private val clock: Clock,
) : SendShoppingListEmailUseCase, CheckScheduledShoppingListEmailUseCase {

    override fun sendShoppingListEmail(): Long = sendCurrentShoppingList()

    override fun checkScheduledShoppingListEmail() {
        val lastDispatchAt = dispatchRepository.findLastSuccessfulDispatchAt()
        val changedItems = inventoryItemRepository.findAllUpdatedAfter(lastDispatchAt ?: Instant.EPOCH)
        if (changedItems.isEmpty()) {
            logger.info("Scheduled shopping-list email check skipped: no inventory stock changes since the last successful dispatch.")
            return
        }

        if (!changedItems.any(::matchesCondition)) {
            logger.info(
                "Scheduled shopping-list email check skipped: {} changed inventory item(s), condition {} not met.",
                changedItems.size,
                settings.condition,
            )
            return
        }

        val dispatchNumber = sendCurrentShoppingList()
        logger.info(
            "Scheduled shopping-list email check dispatched email number {} after {} changed inventory item(s).",
            dispatchNumber,
            changedItems.size,
        )
    }

    private fun sendCurrentShoppingList(): Long {
        val recipients = settings.recipients.map(String::trim).filter(String::isNotBlank)
        if (recipients.isEmpty()) {
            logger.error("Shopping-list email was not sent because no recipients are configured.")
            throw ShoppingListEmailConfigurationException("No shopping-list email recipients are configured.")
        }

        val categoriesById = categoryRepository.findAll().associateBy { it.id }
        val shoppingList = inventoryItemRepository.findAll()
            .filter { it.isBelowTargetStock }
            .sortedWith(compareBy<InventoryItem> { categoriesById.getValue(it.categoryId).name.normalizedValue }
                .thenBy { it.name.normalizedValue })
        if (shoppingList.isEmpty()) {
            throw EmptyShoppingListException()
        }

        val dispatchNumber = dispatchRepository.nextDispatchNumber()
        val email = ShoppingListEmail(
            dispatchNumber = dispatchNumber,
            recipients = recipients,
            subject = "Einkaufsliste #$dispatchNumber",
            items = shoppingList.map { item ->
                ShoppingListEmailItem(
                    itemName = item.name.value,
                    categoryName = categoriesById.getValue(item.categoryId).name.value,
                    purchaseQuantity = item.targetStock.value - item.currentStock.value,
                    isBelowMinimumStock = item.isBelowMinimumStock,
                )
            },
        )

        try {
            emailSender.send(email)
        } catch (exception: Exception) {
            logger.error(
                "Shopping-list email number {} failed for {} recipient(s).",
                dispatchNumber,
                recipients.size,
                exception,
            )
            throw ShoppingListEmailDeliveryException(exception)
        }

        dispatchRepository.saveSuccessfulDispatch(dispatchNumber, clock.instant())
        logger.info(
            "Shopping-list email number {} sent successfully to {} recipient(s).",
            dispatchNumber,
            recipients.size,
        )
        return dispatchNumber
    }

    private fun matchesCondition(item: InventoryItem): Boolean = when (settings.condition) {
        ShoppingListEmailCondition.CHANGED_ITEMS_BELOW_MINIMUM_STOCK -> item.isBelowMinimumStock
        ShoppingListEmailCondition.CHANGED_ITEMS_BELOW_TARGET_STOCK -> item.isBelowTargetStock
    }

    private companion object {
        val logger = LoggerFactory.getLogger(ShoppingListEmailService::class.java)
    }
}
