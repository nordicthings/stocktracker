package org.nordicthings.stocktracker.inventory.adapter.mail

import org.nordicthings.stocktracker.inventory.application.CheckScheduledShoppingListEmailUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ShoppingListEmailScheduler(
    private val checkScheduledShoppingListEmail: CheckScheduledShoppingListEmailUseCase,
) {

    @Scheduled(
        cron = "\${stocktracker.shopping-list-email.schedule:-}",
        zone = "Europe/Berlin",
    )
    fun checkForScheduledDispatch() {
        checkScheduledShoppingListEmail.checkScheduledShoppingListEmail()
    }
}
