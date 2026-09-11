package org.nordicthings.stocktracker.inventory.config

import java.time.Clock
import org.nordicthings.stocktracker.inventory.application.InventoryItemRepository
import org.nordicthings.stocktracker.inventory.application.InventoryService
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailDispatchRepository
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailSender
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailService
import org.nordicthings.stocktracker.inventory.adapter.mail.ShoppingListEmailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ShoppingListEmailProperties::class)
class InventoryConfiguration {

    @Bean
    fun inventoryService(repository: InventoryItemRepository): InventoryService =
        InventoryService(repository)

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun shoppingListEmailService(
        inventoryItemRepository: InventoryItemRepository,
        dispatchRepository: ShoppingListEmailDispatchRepository,
        emailSender: ShoppingListEmailSender,
        settings: ShoppingListEmailProperties,
        clock: Clock,
    ): ShoppingListEmailService = ShoppingListEmailService(
        inventoryItemRepository,
        dispatchRepository,
        emailSender,
        settings,
        clock,
    )
}
