package org.nordicthings.stocktracker.inventory.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "inventory_items")
class InventoryItemJpaEntity(
    @Id
    @Column(nullable = false, updatable = false, length = 36)
    var id: String,

    @Column(nullable = false, length = 255)
    var name: String,

    @Column(name = "normalized_name", nullable = false, unique = true, length = 255)
    var normalizedName: String,

    @Column(name = "current_stock", nullable = false)
    var currentStock: Int,

    @Column(name = "minimum_stock", nullable = false)
    var minimumStock: Int,

    @Column(name = "target_stock", nullable = false)
    var targetStock: Int,

    @Column(length = 500)
    var note: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
