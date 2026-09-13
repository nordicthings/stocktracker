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
import org.nordicthings.stocktracker.inventory.application.ViewCategoriesUseCase
import org.nordicthings.stocktracker.inventory.application.CategorySort
import org.nordicthings.stocktracker.inventory.application.SetCurrentStockCommand
import org.nordicthings.stocktracker.inventory.application.SetCurrentStockUseCase
import org.nordicthings.stocktracker.inventory.application.SetStockToTargetUseCase
import org.nordicthings.stocktracker.inventory.application.ViewInventoryItemUseCase
import org.nordicthings.stocktracker.inventory.application.ViewInventoryItemsUseCase
import org.nordicthings.stocktracker.inventory.domain.InventoryException
import org.nordicthings.stocktracker.inventory.domain.Category
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.bind.annotation.SessionAttributes

@Controller
@SessionAttributes("inventoryFilter")
class InventoryItemController(
    private val createInventoryItem: CreateInventoryItemUseCase,
    private val editInventoryItem: EditInventoryItemUseCase,
    private val deleteInventoryItem: DeleteInventoryItemUseCase,
    private val setCurrentStock: SetCurrentStockUseCase,
    private val increaseCurrentStock: IncreaseCurrentStockUseCase,
    private val setStockToTarget: SetStockToTargetUseCase,
    private val decreaseCurrentStock: DecreaseCurrentStockUseCase,
    private val viewInventoryItem: ViewInventoryItemUseCase,
    private val viewInventoryItems: ViewInventoryItemsUseCase,
    private val viewCategories: ViewCategoriesUseCase,
) {

    @GetMapping("/")
    fun root(): String = "redirect:/items"

    @GetMapping("/items")
    fun items(
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false) inventorySort: String?,
        @RequestParam(required = false) categoryId: String?,
        @RequestParam(defaultValue = "false") resetFilters: Boolean,
        @ModelAttribute("inventoryFilter") inventoryFilter: InventoryFilter,
        model: Model,
    ): String {
        if (resetFilters) {
            inventoryFilter.reset()
        } else {
            if (searchTerm != null) {
                inventoryFilter.searchTerm = searchTerm
            }
            if (inventorySort != null) {
                inventoryFilter.sort = inventorySort.toInventoryItemSort()
            }
            if (categoryId != null) {
                inventoryFilter.categoryId = categoryId.ifBlank { null }
            }
        }

        val selectedInventorySort = inventoryFilter.sort
        val selectedSearchTerm = inventoryFilter.searchTerm
        val selectedCategoryId = inventoryFilter.categoryId
        val inventoryOverview = viewInventoryItems.viewInventoryItems(
            InventoryItemsQuery(searchTerm = selectedSearchTerm, categoryId = selectedCategoryId, sort = selectedInventorySort),
        )

        model.addAttribute("searchTerm", selectedSearchTerm.orEmpty())
        model.addAttribute("inventorySort", selectedInventorySort)
        model.addAttribute("categoryId", selectedCategoryId.orEmpty())
        model.addAttribute("categories", viewCategories.viewCategories(CategorySort.NAME_ASCENDING))
        model.addAttribute("nextNameSort", selectedInventorySort.nextNameSort())
        model.addAttribute("nextCategorySort", selectedInventorySort.nextCategorySort())
        model.addAttribute("inventoryOverview", inventoryOverview)
        if (!model.containsAttribute("focusTarget") && searchTerm != null) {
            model.addAttribute("focusTarget", "inventorySearch")
        }

        return "inventory/items"
    }

    @ModelAttribute("inventoryFilter")
    fun inventoryFilter(): InventoryFilter = InventoryFilter()

    @GetMapping("/items/{itemId}")
    fun detail(
        @PathVariable itemId: String,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        try {
            model.addAttribute("item", viewInventoryItem.viewInventoryItem(itemId))
            model.addAttribute("categories", viewCategories.viewCategories(CategorySort.NAME_ASCENDING))
        } catch (exception: InventoryApplicationException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
            return "redirect:/items"
        } catch (exception: InventoryException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
            return "redirect:/items"
        }

        return "inventory/detail"
    }

    @PostMapping("/items")
    fun create(
        @RequestParam name: String,
        @RequestParam currentStock: String,
        @RequestParam minimumStock: String,
        @RequestParam targetStock: String,
        @RequestParam(required = false) categoryId: String?,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(
        redirectAttributes = redirectAttributes,
        successMessage = null,
        successFocusTarget = "quickEntryName",
    ) {
        createInventoryItem.create(
            CreateInventoryItemCommand(
                name = name,
                categoryId = categoryId ?: Category.SYSTEM_CATEGORY_ID.value,
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
        @RequestParam(required = false) categoryId: String?,
        @RequestParam(required = false) note: String?,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(
        redirectAttributes = redirectAttributes,
        successMessage = null,
        successRedirectPath = "/items",
        failureRedirectPath = "/items/$itemId",
    ) {
        editInventoryItem.edit(
            EditInventoryItemCommand(
                itemId = itemId,
                name = name,
                categoryId = categoryId ?: viewInventoryItem.viewInventoryItem(itemId).categoryId,
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
    ): String = handleInventoryAction(redirectAttributes, successMessage = null) {
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
    ): String = handleInventoryAction(redirectAttributes, successMessage = null) {
        increaseCurrentStock.increaseCurrentStock(ChangeCurrentStockCommand(itemId, quantity.toRequiredInt("Menge")))
    }

    @PostMapping("/items/{itemId}/stock/decrease")
    fun decreaseStock(
        @PathVariable itemId: String,
        @RequestParam(defaultValue = "1") quantity: String,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(redirectAttributes, successMessage = null) {
        decreaseCurrentStock.decreaseCurrentStock(ChangeCurrentStockCommand(itemId, quantity.toRequiredInt("Menge")))
    }

    @PostMapping("/items/{itemId}/stock/fill-to-target")
    fun fillToTarget(
        @PathVariable itemId: String,
        @RequestParam(required = false) returnTo: String?,
        redirectAttributes: RedirectAttributes,
    ): String = handleInventoryAction(
        redirectAttributes = redirectAttributes,
        successMessage = null,
        successRedirectPath = returnTo.redirectPath(),
    ) {
        setStockToTarget.setStockToTarget(itemId)
    }

    private fun handleInventoryAction(
        redirectAttributes: RedirectAttributes,
        successMessage: String?,
        successRedirectPath: String = "/items",
        failureRedirectPath: String = successRedirectPath,
        successFocusTarget: String? = null,
        action: () -> Unit,
    ): String {
        try {
            action()
            if (successMessage != null) {
                redirectAttributes.addFlashAttribute("successMessage", successMessage)
            }
            if (successFocusTarget != null) {
                redirectAttributes.addFlashAttribute("focusTarget", successFocusTarget)
            }
            return "redirect:$successRedirectPath"
        } catch (exception: InventoryApplicationException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        } catch (exception: InventoryException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        } catch (exception: InvalidWebInputException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.message)
        }

        return "redirect:$failureRedirectPath"
    }

    private fun String?.toInventoryItemSort(): InventoryItemSort =
        enumValueOrDefault(this, InventoryItemSort.NAME_ASCENDING)

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        value?.let { candidate -> T::class.java.enumConstants.firstOrNull { it.name == candidate } } ?: default

    private fun String.toRequiredInt(fieldName: String): Int =
        trim().toIntOrNull() ?: throw InvalidWebInputException("$fieldName muss eine ganze Zahl sein.")
}

private fun String?.redirectPath(): String = if (this == "shopping-list") "/shopping-list" else "/items"

private class InvalidWebInputException(message: String) : RuntimeException(message)

data class InventoryFilter(
    var searchTerm: String? = null,
    var categoryId: String? = null,
    var sort: InventoryItemSort = InventoryItemSort.NAME_ASCENDING,
) {
    fun reset() {
        searchTerm = null
        categoryId = null
        sort = InventoryItemSort.NAME_ASCENDING
    }
}

private fun InventoryItemSort.nextNameSort(): InventoryItemSort =
    if (this == InventoryItemSort.NAME_ASCENDING) InventoryItemSort.NAME_DESCENDING else InventoryItemSort.NAME_ASCENDING

private fun InventoryItemSort.nextCategorySort(): InventoryItemSort =
    if (this == InventoryItemSort.CATEGORY_ASCENDING) InventoryItemSort.CATEGORY_DESCENDING else InventoryItemSort.CATEGORY_ASCENDING
