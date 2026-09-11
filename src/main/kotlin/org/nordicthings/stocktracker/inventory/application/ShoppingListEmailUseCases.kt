package org.nordicthings.stocktracker.inventory.application

interface SendShoppingListEmailUseCase {
    fun sendShoppingListEmail(): Long
}

interface CheckScheduledShoppingListEmailUseCase {
    fun checkScheduledShoppingListEmail()
}
