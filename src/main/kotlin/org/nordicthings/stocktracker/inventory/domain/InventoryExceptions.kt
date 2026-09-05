package org.nordicthings.stocktracker.inventory.domain

open class InventoryException(message: String) : RuntimeException(message)

class InvalidInventoryItemIdException(value: String) :
    InventoryException("Inventory item id must be a valid UUID: '$value'.")

class InvalidItemNameException :
    InventoryException("Item name must not be blank.")

class InvalidCurrentStockException(value: Int) :
    InventoryException("Current stock must not be negative: $value.")

class InvalidMinimumStockException(value: Int) :
    InventoryException("Minimum stock must be at least 1: $value.")

class InvalidTargetStockException(value: Int) :
    InventoryException("Target stock must be at least 1: $value.")

class InvalidStockConfigurationException(minimumStock: MinimumStock, targetStock: TargetStock) :
    InventoryException(
        "Target stock (${targetStock.value}) must be greater than or equal to minimum stock (${minimumStock.value}).",
    )

class InvalidStockOperationException(message: String) : InventoryException(message)
