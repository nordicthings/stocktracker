package org.nordicthings.stocktracker.inventory.domain

import java.util.Locale
import java.util.UUID
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.ValueObject

@AggregateRoot
class Category private constructor(
    @field:Identity
    val id: CategoryId,
    val name: CategoryName,
) {

    val isSystemCategory: Boolean
        get() = id == SYSTEM_CATEGORY_ID

    fun rename(name: CategoryName): Category {
        if (isSystemCategory) {
            throw SystemCategoryModificationException()
        }
        return Category(id, name)
    }

    companion object {
        val SYSTEM_CATEGORY_ID: CategoryId = CategoryId.of("00000000-0000-0000-0000-000000000001")
        val SYSTEM_CATEGORY_NAME: CategoryName = CategoryName.of("--ohne--")

        fun create(name: CategoryName): Category = Category(CategoryId.newId(), name)

        fun reconstitute(id: CategoryId, name: CategoryName): Category = Category(id, name)
    }

    override fun equals(other: Any?): Boolean = this === other || other is Category && id == other.id

    override fun hashCode(): Int = id.hashCode()
}

@ValueObject
@JvmInline
value class CategoryId private constructor(val value: String) {
    companion object {
        fun newId(): CategoryId = of(UUID.randomUUID().toString())

        fun of(value: String): CategoryId {
            try {
                UUID.fromString(value)
            } catch (exception: IllegalArgumentException) {
                throw InvalidCategoryIdException(value)
            }
            return CategoryId(value)
        }
    }

    override fun toString(): String = value
}

@ValueObject
class CategoryName private constructor(
    val value: String,
    val normalizedValue: String,
) {
    companion object {
        private val whitespace = Regex("\\s+")

        fun of(value: String): CategoryName {
            val displayValue = value.trim().replace(whitespace, " ")
            if (displayValue.isBlank()) {
                throw InvalidCategoryNameException()
            }
            if (displayValue.length > 30) {
                throw CategoryNameTooLongException()
            }
            return CategoryName(displayValue, displayValue.lowercase(Locale.ROOT))
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is CategoryName && normalizedValue == other.normalizedValue

    override fun hashCode(): Int = normalizedValue.hashCode()

    override fun toString(): String = value
}
