package org.nordicthings.stocktracker.inventory.adapter.web

import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.nordicthings.stocktracker.inventory.adapter.persistence.InventoryItemJpaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:inventory-web-test;MODE=MariaDB;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    ],
)
class InventoryControllerTest @Autowired constructor(
    private val jpaRepository: InventoryItemJpaRepository,
) {

    @LocalServerPort
    private var port: Int = 0

    private val httpClient = HttpClient.newBuilder()
        .cookieHandler(CookieManager(null, CookiePolicy.ACCEPT_ALL))
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build()

    @BeforeTest
    fun clearRepository() {
        jpaRepository.deleteAll()
    }

    @Test
    fun `shows the empty inventory page`() {
        val response = get("/")

        assertEquals(200, response.statusCode())
        assertContains(response.body(), "Vorratsverwaltung")
        assertContains(response.body(), "aria-label=\"Artikel erfassen\"")
        assertContains(response.body(), "Hinzufügen")
        assertFalse(response.body().contains("<summary>Artikel erfassen</summary>"))
        assertFalse(response.body().contains("Istbestandspflege"))
        assertFalse(response.body().contains("<nav class=\"tabs\""))
        assertFalse(response.body().contains("name=\"note\""))
        assertContains(response.body(), "Noch keine Artikel vorhanden.")
    }

    @Test
    fun `creates an item and renders purchase need on inventory and shopping list`() {
        val createResponse = post(
            "/items",
            form(
                "name" to "Nudeln (500g)",
                "currentStock" to "1",
                "minimumStock" to "3",
                "targetStock" to "5",
            ),
        )

        assertEquals(200, createResponse.statusCode())
        assertFalse(createResponse.body().contains("Artikel wurde angelegt."))
        assertContains(createResponse.body(), "Nudeln (500g)")
        assertContains(createResponse.body(), "Nachkaufbedarf")
        assertContains(createResponse.body(), "1 Artikel unter Mindestbestand")
        assertContains(createResponse.body(), "<th scope=\"col\">Artikel</th>")
        assertContains(createResponse.body(), "<th scope=\"col\">Istbestand</th>")
        assertContains(createResponse.body(), "<th scope=\"col\">Sollbestand</th>")
        assertContains(createResponse.body(), "Entnehmen")
        assertContains(createResponse.body(), "Auf Sollbestand")
        val inventoryTableHtml = createResponse.body().substringBefore("</table>")
        assertFalse(inventoryTableHtml.contains(">Setzen<"))
        assertFalse(inventoryTableHtml.contains(">+<"))
        assertFalse(inventoryTableHtml.contains(">-<"))
        assertFalse(inventoryTableHtml.contains("1 entnehmen"))

        val shoppingResponse = get("/?tab=shopping")

        assertEquals(200, shoppingResponse.statusCode())
        assertContains(shoppingResponse.body(), "Einkaufsliste")
        assertContains(shoppingResponse.body(), "Zurück zur Bestandsliste")
        assertContains(shoppingResponse.body(), "Unter Mindestbestand")
        assertContains(shoppingResponse.body(), "Einkaufsmenge")
        assertContains(shoppingResponse.body(), "Auf Sollbestand")
        assertContains(shoppingResponse.body(), ">4<")
    }

    @Test
    fun `links to shopping list when items are below target stock`() {
        val response = post(
            "/items",
            form(
                "name" to "Salz",
                "currentStock" to "3",
                "minimumStock" to "3",
                "targetStock" to "5",
            ),
        )

        assertEquals(200, response.statusCode())
        assertContains(response.body(), "1 Artikel unter Sollbestand")
        assertFalse(response.body().contains("1 Artikel unter Mindestbestand"))

        val shoppingResponse = get("/?tab=shopping")

        assertEquals(200, shoppingResponse.statusCode())
        assertContains(shoppingResponse.body(), "Salz")
        assertContains(shoppingResponse.body(), ">2<")
        assertFalse(shoppingResponse.body().contains("Unter Mindestbestand"))
    }

    @Test
    fun `decreases current stock by one from inventory table`() {
        post(
            "/items",
            form(
                "name" to "Haferflocken",
                "currentStock" to "2",
                "minimumStock" to "3",
                "targetStock" to "5",
            ),
        )
        val itemId = jpaRepository.findAll().single().id

        val response = post(
            "/items/$itemId/stock/decrease",
            form("quantity" to "1"),
        )

        assertEquals(200, response.statusCode())
        assertFalse(response.body().contains("Istbestand wurde verringert."))
        assertContains(response.body(), ">1<")
    }

    @Test
    fun `fills current stock to target without rendering success message`() {
        post(
            "/items",
            form(
                "name" to "Tomaten",
                "currentStock" to "1",
                "minimumStock" to "2",
                "targetStock" to "5",
            ),
        )
        val itemId = jpaRepository.findAll().single().id

        val response = post("/items/$itemId/stock/fill-to-target", "")

        assertEquals(200, response.statusCode())
        assertFalse(response.body().contains("Istbestand wurde auf Sollbestand gesetzt."))
        assertContains(response.body(), ">5<")
    }

    @Test
    fun `disables stock action buttons when action is not applicable`() {
        val emptyStockResponse = post(
            "/items",
            form(
                "name" to "Mehl",
                "currentStock" to "0",
                "minimumStock" to "1",
                "targetStock" to "3",
            ),
        )

        assertEquals(200, emptyStockResponse.statusCode())
        assertContains(emptyStockResponse.body(), "<button type=\"submit\" disabled=\"disabled\">Entnehmen</button>")

        jpaRepository.deleteAll()

        val targetReachedResponse = post(
            "/items",
            form(
                "name" to "Zucker",
                "currentStock" to "3",
                "minimumStock" to "1",
                "targetStock" to "3",
            ),
        )

        assertEquals(200, targetReachedResponse.statusCode())
        assertContains(
            targetReachedResponse.body(),
            "<button type=\"submit\" disabled=\"disabled\">Auf Sollbestand</button>",
        )
    }

    @Test
    fun `renders a useful message for invalid form values`() {
        val response = post(
            "/items",
            form(
                "name" to "Reis",
                "currentStock" to "eins",
                "minimumStock" to "2",
                "targetStock" to "4",
            ),
        )

        assertEquals(200, response.statusCode())
        assertContains(response.body(), "Istbestand muss eine ganze Zahl sein.")
    }

    @Test
    fun `shows item detail and edits item data there`() {
        post(
            "/items",
            form(
                "name" to "Reis (1kg)",
                "currentStock" to "2",
                "minimumStock" to "3",
                "targetStock" to "6",
            ),
        )
        val itemId = jpaRepository.findAll().single().id

        val detailResponse = get("/items/$itemId")

        assertEquals(200, detailResponse.statusCode())
        assertContains(detailResponse.body(), "Reis (1kg)")
        assertContains(detailResponse.body(), "Istbestand")
        assertContains(detailResponse.body(), "Sollbestand")
        assertContains(detailResponse.body(), "Änderungen speichern")
        assertContains(detailResponse.body(), ">Zurück</a>")
        assertContains(detailResponse.body(), "Löschen")
        assertFalse(detailResponse.body().contains("Zur Istbestandspflege"))
        assertFalse(detailResponse.body().contains("Istbestand speichern"))

        val editResponse = post(
            "/items/$itemId/edit",
            form(
                "name" to "Basmatireis (1kg)",
                "currentStock" to "5",
                "minimumStock" to "4",
                "targetStock" to "8",
                "note" to "Großer Sack",
            ),
        )

        assertEquals(200, editResponse.statusCode())
        assertContains(editResponse.body(), "Artikel wurde aktualisiert.")
        assertContains(editResponse.body(), "Basmatireis (1kg)")
        assertContains(editResponse.body(), "name=\"currentStock\"")
        assertContains(editResponse.body(), "value=\"5\"")
        assertContains(editResponse.body(), "Großer Sack")
    }

    @Test
    fun `deletes item from detail action`() {
        post(
            "/items",
            form(
                "name" to "Bohnen",
                "currentStock" to "2",
                "minimumStock" to "3",
                "targetStock" to "6",
            ),
        )
        val itemId = jpaRepository.findAll().single().id

        val deleteResponse = post("/items/$itemId/delete", "")

        assertEquals(200, deleteResponse.statusCode())
        assertContains(deleteResponse.body(), "Artikel wurde gelöscht.")
        assertContains(deleteResponse.body(), "Noch keine Artikel vorhanden.")
    }

    private fun get(path: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
            .GET()
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    private fun post(path: String, body: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    private fun baseUrl(): String = "http://localhost:$port"

    private fun form(vararg values: Pair<String, String>): String =
        values.joinToString("&") { (name, value) -> "${name.urlEncode()}=${value.urlEncode()}" }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8)
}
