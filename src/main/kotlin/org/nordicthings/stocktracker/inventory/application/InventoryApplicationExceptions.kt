package org.nordicthings.stocktracker.inventory.application

import org.nordicthings.stocktracker.inventory.domain.InventoryItemId

open class InventoryApplicationException(message: String) : RuntimeException(message)

class InventoryItemNotFoundException(id: InventoryItemId) :
    InventoryApplicationException("Inventory item not found: '$id'.")

class DuplicateItemNameException(name: String) :
    InventoryApplicationException("An inventory item with the name '$name' already exists.")

class DeleteInventoryItemNotConfirmedException :
    InventoryApplicationException("Deleting an inventory item requires confirmation.")

class SetShoppingListToTargetNotConfirmedException :
    InventoryApplicationException("Setting all shopping list items to target stock requires confirmation.")

class EmptyShoppingListException :
    InventoryApplicationException("The shopping list is empty.")

class ShoppingListEmailConfigurationException(message: String) : InventoryApplicationException(message)

class ShoppingListEmailDeliveryException(cause: Throwable) :
    InventoryApplicationException("The shopping list email could not be sent.") {
    init {
        initCause(cause)
    }
}
