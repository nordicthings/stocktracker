package org.nordicthings.stocktracker.inventory.domain

import java.util.Locale
import org.jmolecules.ddd.annotation.ValueObject

@ValueObject
class ItemName private constructor(
    val value: String,
    val normalizedValue: String,
) {

    companion object {
        private val whitespace = Regex("\\s+")

        fun of(value: String): ItemName {
            val displayValue = value.trim().replace(whitespace, " ")

            if (displayValue.isBlank()) {
                throw InvalidItemNameException()
            }

            return ItemName(
                value = displayValue,
                normalizedValue = displayValue.lowercase(Locale.ROOT),
            )
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is ItemName && normalizedValue == other.normalizedValue

    override fun hashCode(): Int = normalizedValue.hashCode()

    override fun toString(): String = value
}
