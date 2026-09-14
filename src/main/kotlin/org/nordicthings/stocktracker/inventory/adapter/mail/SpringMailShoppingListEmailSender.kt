package org.nordicthings.stocktracker.inventory.adapter.mail

import java.nio.charset.StandardCharsets
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmail
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailConfigurationException
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailSender
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils

@Component
class SpringMailShoppingListEmailSender(
    private val mailSender: JavaMailSender?,
    private val properties: ShoppingListEmailProperties,
) : ShoppingListEmailSender {

    override fun send(email: ShoppingListEmail) {
        val sender = mailSender ?: throw ShoppingListEmailConfigurationException("SMTP is not configured.")
        if (properties.from.isBlank()) {
            throw ShoppingListEmailConfigurationException("No shopping-list email sender address is configured.")
        }

        val message = sender.createMimeMessage()
        MimeMessageHelper(message, StandardCharsets.UTF_8.name()).apply {
            setFrom(properties.from)
            setTo(email.recipients.toTypedArray())
            setSubject(email.subject)
            setText(renderHtml(email), true)
        }
        sender.send(message)
    }

    private fun renderHtml(email: ShoppingListEmail): String = buildString {
        val includesItemBelowMinimumStock = email.items.any { it.isBelowMinimumStock }
        append("<html><body><h1>")
        append(HtmlUtils.htmlEscape(email.subject))
        append("</h1><table border=\"1\" cellpadding=\"8\" cellspacing=\"0\">")
        append("<thead><tr><th style=\"text-align: left;\">Kategorie</th><th style=\"text-align: left;\">Artikel</th><th>Einkaufsmenge</th>")
        if (includesItemBelowMinimumStock) {
            append("<th style=\"text-align: left;\">Dringlichkeit</th>")
        }
        append("</tr></thead><tbody>")
        email.items.forEach { item ->
            append("<tr><td style=\"text-align: left;\">")
            append(HtmlUtils.htmlEscape(item.categoryName))
            append("</td><td style=\"text-align: left;\">")
            append(HtmlUtils.htmlEscape(item.itemName))
            append("</td><td style=\"text-align: center;\">")
            append(item.purchaseQuantity)
            append("</td>")
            if (includesItemBelowMinimumStock) {
                append("<td style=\"text-align: left;\">")
                if (item.isBelowMinimumStock) {
                    append("Unter Mindestbestand")
                }
                append("</td>")
            }
            append("</tr>")
        }
        append("</tbody></table></body></html>")
    }
}
