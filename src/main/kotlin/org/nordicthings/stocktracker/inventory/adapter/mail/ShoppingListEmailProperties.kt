package org.nordicthings.stocktracker.inventory.adapter.mail

import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailCondition
import org.nordicthings.stocktracker.inventory.application.ShoppingListEmailSettings
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stocktracker.shopping-list-email")
class ShoppingListEmailProperties : ShoppingListEmailSettings {
    override var recipients: List<String> = emptyList()
    override var condition: ShoppingListEmailCondition = ShoppingListEmailCondition.CHANGED_ITEMS_BELOW_TARGET_STOCK
    var from: String = ""
}
