package org.nordicthings.stocktracker.inventory.domain

import java.util.UUID
import org.jmolecules.ddd.annotation.ValueObject

@ValueObject
@JvmInline
value class InventoryItemId private constructor(
    val value: String,
) {

    companion object {
        fun newId(): InventoryItemId = of(UUID.randomUUID().toString())

        fun of(value: String): InventoryItemId {
            validateUuid(value)
            return InventoryItemId(value)
        }

        private fun validateUuid(value: String) {
            try {
                UUID.fromString(value)
            } catch (exception: IllegalArgumentException) {
                throw InvalidInventoryItemIdException(value)
            }
        }
    }

    override fun toString(): String = value
}
