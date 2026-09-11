package org.nordicthings.stocktracker.inventory.config

import java.time.Clock
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
    fun clock(): Clock = Clock.systemUTC()
}
