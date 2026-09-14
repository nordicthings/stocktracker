package org.nordicthings.stocktracker.inventory.adapter.mail

import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmail
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailItem
import org.springframework.mail.javamail.JavaMailSender

class SpringMailShoppingListEmailSenderTest {

    @Test
    fun `uses the subject as HTML email title without a separate dispatch number`() {
        val mailSender = mock(JavaMailSender::class.java)
        val message = MimeMessage(Session.getInstance(Properties()))
        `when`(mailSender.createMimeMessage()).thenReturn(message)
        val properties = ShoppingListEmailProperties().apply { from = "stocktracker@example.test" }
        val email = ShoppingListEmail(
            dispatchNumber = 42,
            recipients = listOf("recipient@example.test"),
            subject = "Einkaufsliste #42",
            items = listOf(ShoppingListEmailItem("Nudeln", "--ohne--", 3, isBelowMinimumStock = false)),
        )

        SpringMailShoppingListEmailSender(mailSender, properties).send(email)

        val html = message.content as String
        assertContains(html, "<h1>Einkaufsliste #42</h1>")
        assertFalse(html.contains("Versandnummer:"))
        assertContains(html, "<td style=\"text-align: center;\">3</td>")
        assertFalse(html.contains("Unter Mindestbestand"))
        verify(mailSender).send(message)
    }

    @Test
    fun `adds the minimum-stock column when at least one item is below minimum stock`() {
        val mailSender = mock(JavaMailSender::class.java)
        val message = MimeMessage(Session.getInstance(Properties()))
        `when`(mailSender.createMimeMessage()).thenReturn(message)
        val properties = ShoppingListEmailProperties().apply { from = "stocktracker@example.test" }
        val email = ShoppingListEmail(
            dispatchNumber = 43,
            recipients = listOf("recipient@example.test"),
            subject = "Einkaufsliste #43",
            items = listOf(
                ShoppingListEmailItem("Reis", "--ohne--", 3, isBelowMinimumStock = true),
                ShoppingListEmailItem("Salz", "--ohne--", 2, isBelowMinimumStock = false),
            ),
        )

        SpringMailShoppingListEmailSender(mailSender, properties).send(email)

        val html = message.content as String
        assertContains(html, "<th style=\"text-align: left;\">Dringlichkeit</th>")
        assertContains(html, "<td style=\"text-align: left;\">Unter Mindestbestand</td>")
        verify(mailSender).send(message)
    }
}
