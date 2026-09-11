package org.nordicthings.stocktracker.inventory.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "shopping_list_email_dispatches")
class ShoppingListEmailDispatchJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "dispatch_number", nullable = false, unique = true)
    var dispatchNumber: Long,

    @Column(name = "sent_at", nullable = false)
    var sentAt: Instant,
)
