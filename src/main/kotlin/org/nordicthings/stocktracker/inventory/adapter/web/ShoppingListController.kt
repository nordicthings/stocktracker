package org.nordicthings.stocktracker.inventory.adapter.web

import org.nordicthings.stocktracker.inventory.application.ShoppingListQuery
import org.nordicthings.stocktracker.inventory.application.ShoppingListSort
import org.nordicthings.stocktracker.inventory.application.ViewShoppingListUseCase
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ShoppingListController(
    private val viewShoppingList: ViewShoppingListUseCase,
) {

    @GetMapping("/shopping-list")
    fun shoppingList(
        @RequestParam(required = false) shoppingSort: String?,
        model: Model,
    ): String {
        val selectedShoppingSort = shoppingSort.toShoppingListSort()

        model.addAttribute("shoppingSort", selectedShoppingSort)
        model.addAttribute("shoppingSorts", ShoppingListSort.entries)
        model.addAttribute("shoppingList", viewShoppingList.viewShoppingList(ShoppingListQuery(selectedShoppingSort)))

        return "inventory/shopping-list"
    }

    private fun String?.toShoppingListSort(): ShoppingListSort =
        enumValueOrDefault(this, ShoppingListSort.NAME)

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        value?.let { candidate -> T::class.java.enumConstants.firstOrNull { it.name == candidate } } ?: default
}
