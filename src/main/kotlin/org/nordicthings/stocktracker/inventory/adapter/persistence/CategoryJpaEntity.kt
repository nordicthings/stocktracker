package org.nordicthings.stocktracker.inventory.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
class CategoryJpaEntity(
    @Id
    @Column(nullable = false, updatable = false, length = 36)
    var id: String,

    @Column(nullable = false, length = 30)
    var name: String,

    @Column(name = "normalized_name", nullable = false, unique = true, length = 30)
    var normalizedName: String,
)
