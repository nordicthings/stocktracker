package org.nordicthings.stocktracker.inventory.adapter.web

import org.nordicthings.stocktracker.inventory.application.ShoppingListQuery
import org.nordicthings.stocktracker.inventory.application.ShoppingListSort
import org.nordicthings.stocktracker.inventory.application.SetShoppingListToTargetCommand
import org.nordicthings.stocktracker.inventory.application.SetShoppingListToTargetUseCase
import org.nordicthings.stocktracker.inventory.application.SendShoppingListEmailUseCase
import org.nordicthings.stocktracker.inventory.application.InventoryApplicationException
import org.nordicthings.stocktracker.inventory.application.ViewShoppingListUseCase
import org.nordicthings.stocktracker.inventory.domain.InventoryException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ShoppingListController(
    private val viewShoppingList: ViewShoppingListUseCase,
    private val setShoppingListToTarget: SetShoppingListToTargetUseCase,
    private val sendShoppingListEmail: SendShoppingListEmailUseCase,
) {

    @GetMapping("/shopping-list")
    fun shoppingList(
        @RequestParam(required = false) shoppingSort: String?,
        model: Model,
    ): String {
        val selectedShoppingSort = shoppingSort.toShoppingListSort()

        val shoppingList = viewShoppingList.viewShoppingList(ShoppingListQuery(selectedShoppingSort))
        if (shoppingList.isEmpty()) {
            return "redirect:/items"
        }

        model.addAttribute("shoppingSort", selectedShoppingSort)
        model.addAttribute("nextNameSort", selectedShoppingSort.nextNameSort())
        model.addAttribute("nextCategorySort", selectedShoppingSort.nextCategorySort())
        model.addAttribute("shoppingList", shoppingList)

        return "inventory/shopping-list"
    }

    @PostMapping("/shopping-list/fill-to-target")
    fun fillAllToTarget(redirectAttributes: RedirectAttributes): String = handleShoppingListAction(redirectAttributes) {
        setShoppingListToTarget.setShoppingListToTarget(SetShoppingListToTargetCommand(confirmed = true))
    }

    @PostMapping("/shopping-list/email")
    fun sendEmail(redirectAttributes: RedirectAttributes): String = handleShoppingListAction(
        redirectAttributes,
        "Einkaufsliste wurde per E-Mail versendet.",
    ) {
        sendShoppingListEmail.sendShoppingListEmail()
    }

    private fun handleShoppingListAction(
        redirectAttributes: RedirectAttributes,
        successMessage: String? = null,
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
        }
        return "redirect:/shopping-list"
    }

    private fun String?.toShoppingListSort(): ShoppingListSort =
        enumValueOrDefault(this, ShoppingListSort.CATEGORY_ASCENDING)

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        value?.let { candidate -> T::class.java.enumConstants.firstOrNull { it.name == candidate } } ?: default
}

private fun ShoppingListSort.nextNameSort(): ShoppingListSort =
    if (this == ShoppingListSort.NAME_ASCENDING) ShoppingListSort.NAME_DESCENDING else ShoppingListSort.NAME_ASCENDING

private fun ShoppingListSort.nextCategorySort(): ShoppingListSort =
    if (this == ShoppingListSort.CATEGORY_ASCENDING) ShoppingListSort.CATEGORY_DESCENDING else ShoppingListSort.CATEGORY_ASCENDING
