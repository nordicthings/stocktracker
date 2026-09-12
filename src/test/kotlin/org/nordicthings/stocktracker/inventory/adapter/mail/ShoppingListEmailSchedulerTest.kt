package org.nordicthings.stocktracker.inventory.adapter.mail

import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.scheduling.annotation.Scheduled

class ShoppingListEmailSchedulerTest {

    @Test
    fun `resolves the shopping-list schedule in the Berlin time zone`() {
        val scheduled = ShoppingListEmailScheduler::class.java
            .getDeclaredMethod("checkForScheduledDispatch")
            .getAnnotation(Scheduled::class.java)

        assertEquals("Europe/Berlin", scheduled.zone)
    }
}
