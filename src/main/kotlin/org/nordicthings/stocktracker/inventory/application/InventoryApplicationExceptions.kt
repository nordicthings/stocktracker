package org.nordicthings.stocktracker.inventory.application

import org.nordicthings.stocktracker.inventory.domain.InventoryItemId

open class InventoryApplicationException(message: String) : RuntimeException(message)

class InventoryItemNotFoundException(id: InventoryItemId) :
    InventoryApplicationException("Inventory item not found: '$id'.")

class DuplicateItemNameException(name: String) :
    InventoryApplicationException("An inventory item with the name '$name' already exists.")

class DeleteInventoryItemNotConfirmedException :
    InventoryApplicationException("Deleting an inventory item requires confirmation.")
