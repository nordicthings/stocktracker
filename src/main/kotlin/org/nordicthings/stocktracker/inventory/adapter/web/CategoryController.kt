package org.nordicthings.stocktracker.inventory.adapter.web

import org.nordicthings.stocktracker.inventory.application.CategorySort
import org.nordicthings.stocktracker.inventory.application.CreateCategoryCommand
import org.nordicthings.stocktracker.inventory.application.CreateCategoryUseCase
import org.nordicthings.stocktracker.inventory.application.DeleteCategoryUseCase
import org.nordicthings.stocktracker.inventory.application.EditCategoryCommand
import org.nordicthings.stocktracker.inventory.application.EditCategoryUseCase
import org.nordicthings.stocktracker.inventory.application.InventoryApplicationException
import org.nordicthings.stocktracker.inventory.application.ViewCategoriesUseCase
import org.nordicthings.stocktracker.inventory.application.ViewCategoryUseCase
import org.nordicthings.stocktracker.inventory.domain.InventoryException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class CategoryController(
    private val createCategory: CreateCategoryUseCase,
    private val editCategory: EditCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val viewCategory: ViewCategoryUseCase,
    private val viewCategories: ViewCategoriesUseCase,
) {

    @GetMapping("/categories")
    fun categories(@RequestParam(required = false) categorySort: String?, model: Model): String {
        val sort = categorySort.toCategorySort()
        model.addAttribute("categories", viewCategories.viewCategories(sort))
        model.addAttribute("nextNameSort", if (sort == CategorySort.NAME_ASCENDING) CategorySort.NAME_DESCENDING else CategorySort.NAME_ASCENDING)
        return "inventory/categories"
    }

    @GetMapping("/categories/new")
    fun newCategory(): String = "inventory/category-detail"

    @GetMapping("/categories/{categoryId}")
    fun detail(@PathVariable categoryId: String, model: Model, redirectAttributes: RedirectAttributes): String = try {
        model.addAttribute("category", viewCategory.viewCategory(categoryId))
        "inventory/category-detail"
    } catch (exception: InventoryApplicationException) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        "redirect:/categories"
    } catch (exception: InventoryException) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        "redirect:/categories"
    }

    @PostMapping("/categories")
    fun create(@RequestParam name: String, redirectAttributes: RedirectAttributes): String = handle(redirectAttributes) {
        createCategory.create(CreateCategoryCommand(name))
    }

    @PostMapping("/categories/{categoryId}")
    fun edit(@PathVariable categoryId: String, @RequestParam name: String, redirectAttributes: RedirectAttributes): String =
        handle(
            redirectAttributes = redirectAttributes,
            successRedirectPath = "/categories",
            failureRedirectPath = "/categories/$categoryId",
        ) {
            editCategory.edit(EditCategoryCommand(categoryId, name))
        }

    @PostMapping("/categories/{categoryId}/delete")
    fun delete(@PathVariable categoryId: String, redirectAttributes: RedirectAttributes): String = try {
        deleteCategory.delete(categoryId)
        "redirect:/categories"
    } catch (exception: InventoryApplicationException) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        "redirect:/categories/$categoryId"
    } catch (exception: InventoryException) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        "redirect:/categories/$categoryId"
    }

    private fun handle(
        redirectAttributes: RedirectAttributes,
        successRedirectPath: String = "/categories",
        failureRedirectPath: String = successRedirectPath,
        action: () -> Unit,
    ): String {
        try {
            action()
            return "redirect:$successRedirectPath"
        } catch (exception: InventoryApplicationException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        } catch (exception: InventoryException) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.toUserMessage())
        }
        return "redirect:$failureRedirectPath"
    }

    private fun String?.toCategorySort(): CategorySort =
        enumValueOrDefault(this, CategorySort.NAME_ASCENDING)

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        value?.let { candidate -> T::class.java.enumConstants.firstOrNull { it.name == candidate } } ?: default
}
