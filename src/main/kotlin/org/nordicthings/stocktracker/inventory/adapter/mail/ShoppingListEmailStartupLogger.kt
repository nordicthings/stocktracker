package org.nordicthings.stocktracker.inventory.adapter.mail

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ShoppingListEmailStartupLogger(
    private val properties: ShoppingListEmailProperties,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        logConfiguration()
    }

    fun logConfiguration() {
        if (properties.schedule == "-") {
            logger.info("Automatischer Versand der Einkaufsliste ist deaktiviert (CRON: -).")
        } else {
            logger.info("Automatischer Versand der Einkaufsliste ist aktiviert (CRON: {}).", properties.schedule)
        }

        val recipients = properties.recipients.map(String::trim).filter(String::isNotBlank)
        if (recipients.isEmpty()) {
            logger.info("Keine Empfänger für Einkaufslisten-E-Mails konfiguriert.")
        } else {
            logger.info(
                "Empfänger für Einkaufslisten-E-Mails: {}.",
                recipients.joinToString(", ", transform = ::maskRecipient),
            )
        }
    }

    private fun maskRecipient(recipient: String): String {
        val atIndex = recipient.lastIndexOf('@')
        if (atIndex <= 0 || atIndex == recipient.lastIndex) {
            return recipient.take(4) + "***"
        }

        val localPart = recipient.substring(0, atIndex)
        val domain = recipient.substring(atIndex + 1)
        return "${localPart.take(4)}***@${domain.take(5)}***"
    }

    private companion object {
        val logger = LoggerFactory.getLogger(ShoppingListEmailStartupLogger::class.java)
    }
}
