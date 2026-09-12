package org.nordicthings.stocktracker.inventory.adapter.mail

import kotlin.test.Test
import kotlin.test.assertContains
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(OutputCaptureExtension::class)
class ShoppingListEmailStartupLoggerTest {

    @Test
    fun `logs the configured schedule and recipients on startup`(output: CapturedOutput) {
        val properties = ShoppingListEmailProperties().apply {
            schedule = "0 0 19 * * MON,WED,FRI"
            recipients = listOf("first@somewhere.org", "second@example.test")
        }

        ShoppingListEmailStartupLogger(properties).logConfiguration()

        assertContains(output.out, "Automatischer Versand der Einkaufsliste ist aktiviert (CRON: 0 0 19 * * MON,WED,FRI).")
        assertContains(output.out, "Empfänger für Einkaufslisten-E-Mails: firs***@somew***, seco***@examp***.")
    }
}
