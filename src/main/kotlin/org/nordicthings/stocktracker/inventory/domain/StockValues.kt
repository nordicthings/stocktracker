package org.nordicthings.stocktracker.inventory.domain

import org.jmolecules.ddd.annotation.ValueObject

@ValueObject
class CurrentStock private constructor(
    val value: Int,
) : Comparable<CurrentStock> {

    companion object {
        fun of(value: Int): CurrentStock {
            if (value < 0) {
                throw InvalidCurrentStockException(value)
            }

            return CurrentStock(value)
        }
    }

    operator fun plus(quantity: Int): CurrentStock {
        if (quantity < 1) {
            throw InvalidStockOperationException("Quantity to add must be at least 1: $quantity.")
        }

        return of(value + quantity)
    }

    operator fun minus(quantity: Int): CurrentStock {
        if (quantity < 1) {
            throw InvalidStockOperationException("Quantity to subtract must be at least 1: $quantity.")
        }

        if (quantity > value) {
            throw InvalidStockOperationException(
                "Quantity to subtract ($quantity) must not exceed current stock ($value).",
            )
        }

        return of(value - quantity)
    }

    override fun compareTo(other: CurrentStock): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        this === other || other is CurrentStock && value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = value.toString()
}

@ValueObject
class MinimumStock private constructor(
    val value: Int,
) : Comparable<MinimumStock> {

    companion object {
        fun of(value: Int): MinimumStock {
            if (value < 1) {
                throw InvalidMinimumStockException(value)
            }

            return MinimumStock(value)
        }
    }

    override fun compareTo(other: MinimumStock): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        this === other || other is MinimumStock && value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = value.toString()
}

@ValueObject
class TargetStock private constructor(
    val value: Int,
) : Comparable<TargetStock> {

    companion object {
        fun of(value: Int): TargetStock {
            if (value < 1) {
                throw InvalidTargetStockException(value)
            }

            return TargetStock(value)
        }
    }

    override fun compareTo(other: TargetStock): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        this === other || other is TargetStock && value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = value.toString()
}
