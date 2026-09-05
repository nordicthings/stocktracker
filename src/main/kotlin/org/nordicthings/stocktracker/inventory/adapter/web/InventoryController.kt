package org.nordicthings.stocktracker.inventory.adapter.web

import org.nordicthings.stocktracker.inventory.application.ChangeCurrentStockCommand
import org.nordicthings.stocktracker.inventory.application.CreateInventoryItemCommand
import org.nordicthings.stocktracker.inventory.application.CreateInventoryItemUseCase
import org.nordicthings.stocktracker.inventory.application.DeleteInventoryItemCommand
import org.nordicthings.stocktracker.inventory.application.DeleteInventoryItemUseCase
import org.nordicthings.stocktracker.inventory.application.DecreaseCurrentStockUseCase
import org.nordicthings.stocktracker.inventory.application.EditInventoryItemCommand
import org.nordicthings.stocktracker.inventory.application.EditInventoryItemUseCase
import org.nordicthings.stocktracker.inventory.application.IncreaseCurrentStockUseCase
import org.nordicthings.stocktracker.inventory.application.InventoryApplicationException
import org.nordicthings.stocktracker.inventory.application.InventoryItemSort
import org.nordicthings.stocktracker.inventory.application.InventoryItemsQuery
import org.nordicthings.stocktracker.inventory.application.SetCurrentStockCommand
import org.nordicthings.stocktracker.inventory.application.SetCurrentStockUseCase
import org.nordicthings.stocktracker.inventory.application.SetStockToTargetUseCase
import org.nordicthings.stocktracker.inventory.application.ShoppingListQuery
import org.nordicthings.stocktracker.inventory.application.ShoppingListSort
import org.nordicthings.stocktracker.inventory.application.ViewInventoryItemUseCase
import org.nordicthings.stocktracker.inventory.application.ViewInventoryItemsUseCase
import org.nordicthings.stocktracker.inventory.application.ViewShoppingListUseCase
import org.nordicthings.stocktracker.inventory.domain.InventoryException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class InventoryController(
    private val createInventoryItem: CreateInventoryItemUseCase,
    private val editInventoryItem: EditInventoryItemUseCase,
    private val deleteInventoryItem: DeleteInventoryItemUseCase,
    private val setCurrentStock: SetCurrentStockUseCase,
    private val increaseCurrentStock: IncreaseCurrentStockUseCase,
    private val setStockToTarget: SetStockToTargetUseCase,
    private val decreaseCurrentStock: DecreaseCurrentStockUseCase,
    private val viewInventoryItem: ViewInventoryItemUseCase,
    private val viewInventoryItems: ViewInventoryItemsUseCase,
    private val viewShoppingList: ViewShoppingListUseCase,
) {

    @GetMapping("/")
    fun index(
        @RequestParam(required = false) tab: String?,
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false) inventorySort: String?,
        @RequestParam(required = false) shoppingSort: String?,
        model: Model,
    ): String {
        val selectedInventorySort = inventorySort.toInventoryItemSort()
        val selectedShoppingSort = shoppingSort.toShoppingListSort()
        val inventoryOverview = viewInventoryItems.viewInventoryItems(
            InventoryItemsQuery(searchTerm = searchTerm, sort = selectedInventorySort),
        )
        val shoppingList = viewShoppingList.viewShoppingList(ShoppingListQuery(selectedShoppingSort))

        model.addAttribute("activeTab", if (tab == "shopping") "shopping" else "inventory")
        model.addAttribute("searchTerm", searchTerm.orEmpty())
        model.addAttribute("inventorySort", selectedInventorySort)
        model.addAttribute("shoppingSort", selectedShoppingSort)
        model.addAttribute("inventorySorts", InventoryItemSort.entries)
        model.addAttribute("shoppingSorts", ShoppingListSort.entries)
        model.addAttribute("inventoryOverview", inventoryOverview)
        model.addAttribute("shoppingList", shoppingList)

        return "inventory/index"
    }

    @GetMapping("/items/{itemId}")
    fun detail(
        @PathVariable itemId: String,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        try {
            model.addAttribute("item", viewInventoryItem.viewInventoryItem(itemId))
        } catch (exception: InventoryApplicationException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
            return "redirect:/"
        } catch (exception: InventoryException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
            return "redirect:/"
        }

        return "inventory/detail"
    }

    @PostMapping("/items")
    fun create(
        @RequestParam name: String,
        @RequestParam currentStock: String,
        @RequestParam minimumStock: String,
        @RequestParam targetStock: String,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, successMessage = null) {
        createInventoryItem.create(
            CreateInventoryItemCommand(
                name = name,
                currentStock = currentStock.toRequiredInt("Istbestand"),
                minimumStock = minimumStock.toRequiredInt("Mindestbestand"),
                targetStock = targetStock.toRequiredInt("Sollbestand"),
                note = null,
            ),
        )
    }

    @PostMapping("/items/{itemId}/edit")
    fun edit(
        @PathVariable itemId: String,
        @RequestParam name: String,
        @RequestParam currentStock: String,
        @RequestParam minimumStock: String,
        @RequestParam targetStock: String,
        @RequestParam(required = false) note: String?,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, "Artikel wurde aktualisiert.", "/items/$itemId") {
        editInventoryItem.edit(
            EditInventoryItemCommand(
                itemId = itemId,
                name = name,
                currentStock = currentStock.toRequiredInt("Istbestand"),
                minimumStock = minimumStock.toRequiredInt("Mindestbestand"),
                targetStock = targetStock.toRequiredInt("Sollbestand"),
                note = note,
            ),
        )
    }

    @PostMapping("/items/{itemId}/delete")
    fun delete(
        @PathVariable itemId: String,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, "Artikel wurde gelöscht.") {
        deleteInventoryItem.delete(DeleteInventoryItemCommand(itemId, confirmed = true))
    }

    @PostMapping("/items/{itemId}/stock")
    fun setStock(
        @PathVariable itemId: String,
        @RequestParam currentStock: String,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, "Istbestand wurde aktualisiert.") {
        setCurrentStock.setCurrentStock(SetCurrentStockCommand(itemId, currentStock.toRequiredInt("Istbestand")))
    }

    @PostMapping("/items/{itemId}/stock/increase")
    fun increaseStock(
        @PathVariable itemId: String,
        @RequestParam(defaultValue = "1") quantity: String,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, "Istbestand wurde erhöht.") {
        increaseCurrentStock.increaseCurrentStock(ChangeCurrentStockCommand(itemId, quantity.toRequiredInt("Menge")))
    }

    @PostMapping("/items/{itemId}/stock/decrease")
    fun decreaseStock(
        @PathVariable itemId: String,
        @RequestParam(defaultValue = "1") quantity: String,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, "Istbestand wurde verringert.") {
        decreaseCurrentStock.decreaseCurrentStock(ChangeCurrentStockCommand(itemId, quantity.toRequiredInt("Menge")))
    }

    @PostMapping("/items/{itemId}/stock/fill-to-target")
    fun fillToTarget(
        @PathVariable itemId: String,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, "Istbestand wurde auf Sollbestand gesetzt.") {
        setStockToTarget.setStockToTarget(itemId)
    }

    private fun handleInventoryAction(
        redirectAttributes: RedirectAttributes,
        successMessage: String?,
        redirectPath: String = "/",
        action: () -> Unit,
    ): String {
        try {
            action()
            if (successMessage != null) {
                redirectAttributes.addFlashAttribute("successMessage", successMessage)
            }
        } catch (exception: InventoryApplicationException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        } catch (exception: InventoryException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        } catch (exception: InvalidWebInputException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.message)
        }

        return "redirect:$redirectPath"
    }

    private fun String?.toInventoryItemSort(): InventoryItemSort =
        enumValueOrDefault(this, InventoryItemSort.NAME)

    private fun String?.toShoppingListSort(): ShoppingListSort =
        enumValueOrDefault(this, ShoppingListSort.NAME)

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        value?.let { candidate -> T::class.java.enumConstants.firstOrNull { it.name == candidate } } ?: default

    private fun String.toRequiredInt(fieldName: String): Int =
        trim().toIntOrNull() ?: throw InvalidWebInputException("$fieldName muss eine ganze Zahl sein.")
}

private class InvalidWebInputException(message: String) : RuntimeException(message)
