package org.nordicthings.stocktracker.inventory.application

import org.nordicthings.stocktracker.inventory.domain.InventoryItemId
import org.nordicthings.stocktracker.inventory.domain.CategoryId

open class InventoryApplicationException(message: String) : RuntimeException(message)

class InventoryItemNotFoundException(id: InventoryItemId) :
    InventoryApplicationException("Inventory item not found: '$id'.")

class DuplicateItemNameException(name: String) :
    InventoryApplicationException("An inventory item with the name '$name' already exists.")

class DeleteInventoryItemNotConfirmedException :
    InventoryApplicationException("Deleting an inventory item requires confirmation.")

class CategoryNotFoundException(id: CategoryId) : InventoryApplicationException("Category not found: '$id'.")

class DuplicateCategoryNameException(name: String) :
    InventoryApplicationException("A category with the name '$name' already exists.")

class CategoryContainsInventoryItemsException :
    InventoryApplicationException("A category containing inventory items must not be deleted.")

class SystemCategoryDeletionException :
    InventoryApplicationException("The system category must not be deleted.")

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
