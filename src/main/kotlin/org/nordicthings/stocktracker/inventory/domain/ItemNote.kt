package org.nordicthings.stocktracker.inventory.domain

import org.jmolecules.ddd.annotation.ValueObject

@ValueObject
@JvmInline
value class ItemNote private constructor(
    val value: String,
) {

    companion object {
        const val MAX_LENGTH = 500

        fun optional(value: String?): ItemNote? {
            val trimmedValue = value?.trim()

            if (trimmedValue.isNullOrEmpty()) {
                return null
            }

            if (trimmedValue.length > MAX_LENGTH) {
                throw InvalidStockOperationException("Item note must not exceed $MAX_LENGTH characters.")
            }

            return ItemNote(trimmedValue)
        }
    }

    override fun toString(): String = value
}
