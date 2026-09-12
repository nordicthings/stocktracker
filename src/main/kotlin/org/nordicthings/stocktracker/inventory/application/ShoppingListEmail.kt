package org.nordicthings.stocktracker.inventory.application

import java.time.Instant

enum class ShoppingListEmailCondition {
    CHANGED_ITEMS_BELOW_MINIMUM_STOCK,
    CHANGED_ITEMS_BELOW_TARGET_STOCK,
}

data class ShoppingListEmail(
    val dispatchNumber: Long,
    val recipients: List<String>,
    val subject: String,
    val items: List<ShoppingListEmailItem>,
)

data class ShoppingListEmailItem(
    val itemName: String,
    val categoryName: String,
    val purchaseQuantity: Int,
)

interface ShoppingListEmailSender {
    fun send(email: ShoppingListEmail)
}

interface ShoppingListEmailDispatchRepository {
    fun findLastSuccessfulDispatchAt(): Instant?

    fun nextDispatchNumber(): Long

    fun saveSuccessfulDispatch(dispatchNumber: Long, sentAt: Instant)
}

interface ShoppingListEmailSettings {
    val recipients: List<String>
    val condition: ShoppingListEmailCondition
}
