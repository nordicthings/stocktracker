package org.nordicthings.stocktracker.inventory.config

import org.nordicthings.stocktracker.inventory.application.InventoryItemRepository
import org.nordicthings.stocktracker.inventory.application.InventoryService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InventoryConfiguration {

    @Bean
    fun inventoryService(repository: InventoryItemRepository): InventoryService =
        InventoryService(repository)
}
