package org.nordicthings.stocktracker.inventory.adapter.web

import org.nordicthings.stocktracker.inventory.application.DeleteInventoryItemNotConfirmedException
import org.nordicthings.stocktracker.inventory.application.DuplicateItemNameException
import org.nordicthings.stocktracker.inventory.application.InventoryApplicationException
import org.nordicthings.stocktracker.inventory.application.InventoryItemNotFoundException
import org.nordicthings.stocktracker.inventory.domain.InvalidCurrentStockException
import org.nordicthings.stocktracker.inventory.domain.InvalidInventoryItemIdException
import org.nordicthings.stocktracker.inventory.domain.InvalidItemNameException
import org.nordicthings.stocktracker.inventory.domain.InvalidMinimumStockException
import org.nordicthings.stocktracker.inventory.domain.InvalidStockConfigurationException
import org.nordicthings.stocktracker.inventory.domain.InvalidStockOperationException
import org.nordicthings.stocktracker.inventory.domain.InvalidTargetStockException
import org.nordicthings.stocktracker.inventory.domain.InventoryException

fun InventoryApplicationException.toUserMessage(): String = when (this) {
    is DuplicateItemNameException -> "Ein Artikel mit diesem Namen existiert bereits."
    is DeleteInventoryItemNotConfirmedException -> "Der Artikel wurde nicht gelöscht, weil die Bestätigung fehlt."
    is InventoryItemNotFoundException -> "Der Artikel wurde nicht gefunden."
    else -> "Die Aktion konnte nicht ausgeführt werden."
}

fun InventoryException.toUserMessage(): String = when (this) {
    is InvalidInventoryItemIdException -> "Der Artikel wurde nicht gefunden."
    is InvalidItemNameException -> "Der Artikelname darf nicht leer sein."
    is InvalidCurrentStockException -> "Der Istbestand darf nicht negativ sein."
    is InvalidMinimumStockException -> "Der Mindestbestand muss mindestens 1 sein."
    is InvalidTargetStockException -> "Der Sollbestand muss mindestens 1 sein."
    is InvalidStockConfigurationException -> "Der Sollbestand muss größer oder gleich dem Mindestbestand sein."
    is InvalidStockOperationException -> message ?: "Die Istbestandsänderung ist fachlich nicht erlaubt."
    else -> "Die Eingabe ist fachlich ungültig."
}
