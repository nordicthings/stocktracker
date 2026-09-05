# Technischer Entwurf Version 1

## Fachmodul

Version 1 wird als ein Fachmodul `inventory` umgesetzt.

Das Modul `inventory` umfasst:

- Artikelverwaltung
- Istbestandspflege
- Mindestbestandsprüfung
- Sollbestandslogik
- Ableitung der Einkaufsliste
- Suche und Sortierung für Vorratsliste und Einkaufsliste

Die Einkaufsliste wird in Version 1 nicht als eigenes Fachmodul umgesetzt, weil sie ausschließlich aus den Istbeständen des Vorrats abgeleitet wird.

## Package-Struktur

Das Fachmodul folgt der vorgesehenen leichtgewichtigen hexagonalen Struktur:

```text
de.<projekt>.inventory
├── domain
├── application
├── adapter
│   ├── persistence
│   └── web
```

Der konkrete Root-Package-Name wird beim Projekt-Setup festgelegt.

Die festgelegte Package-Basis lautet:

- `org.nordicthings.stocktracker`

Namensräume aus `de.jensklingenberg` werden für diese Anwendung bewusst nicht verwendet.

## Build-Grundlage

Das Projekt verwendet:

- Gradle Kotlin DSL
- Gradle Wrapper mit Gradle 9.7.1
- Kotlin
- Spring Boot
- Java 25 als lokale Laufzeit

Kotlin kompiliert mit JVM-Target 25. Java-Quellen werden ebenfalls mit Release 25 kompiliert.

Spring-Boot-4-Auto-Configurations werden über die passenden Boot-Module eingebunden. Deshalb verwendet das Projekt für Liquibase `spring-boot-starter-liquibase` statt nur `liquibase-core` und für die H2-Konsole `spring-boot-h2console`.

## Namenskonvention

Technische Namen im Code werden auf Englisch vergeben.

Beispiele:

- `InventoryItem`
- `ItemName`
- `CurrentStock`
- `MinimumStock`
- `TargetStock`
- `ShoppingListItem`

Die Oberfläche verwendet weiterhin die in den Anforderungen festgelegten deutschen Begriffe.

## Architekturregeln

- Die Domain enthält die fachlichen Objekte und Regeln.
- Die Domain bleibt frei von Spring, JPA, Thymeleaf und HTMX.
- Die Application-Schicht orchestriert Use Cases und definiert Ports.
- Die Application-Schicht hängt nicht von Web- oder Persistence-Adaptern ab.
- Web- und Persistence-Adapter kommunizieren nicht direkt miteinander.
- Die Einkaufsliste wird über Application-Use-Cases aus den gespeicherten Artikeln abgeleitet.

## Aggregat-Schnitt

Das zentrale Aggregat in Version 1 ist der Vorratsartikel.

Voraussichtlicher englischer technischer Name:

- `InventoryItem`

Das Aggregat trägt:

- Identität
- Artikelname
- Istbestand
- Mindestbestand
- Sollbestand
- optionale Notiz

Die Einkaufsliste ist kein eigenes Aggregat. Sie wird als read-only Sicht aus den gespeicherten `InventoryItem`-Aggregaten abgeleitet.

Die Eindeutigkeit normalisierter Artikelnamen ist eine modulweite Regel. Sie wird in der Application-Schicht mit Hilfe des Persistence-Ports geprüft, weil dafür der Vergleich mit anderen Artikeln erforderlich ist.

Die Identität wird über ein Value Object `InventoryItemId` modelliert. Die ID basiert fachlich auf einer UUID, wird im Domänenmodell aber als `String` gekapselt.

`InventoryItemId` stellt sicher, dass der String ein gültiges UUID-Format hat.

Neue Artikel werden über eine statische Factory-Methode auf dem Aggregat erzeugt, in Kotlin voraussichtlich über ein `companion object`.

Vorgesehene Factory-Methoden:

- `InventoryItem.create(...)` erzeugt einen neuen Artikel inklusive neuer `InventoryItemId`.
- `InventoryItem.reconstitute(...)` baut einen bestehenden Artikel aus gespeicherten Daten wieder auf, ohne eine neue ID zu erzeugen.

`InventoryItem.create(...)` darf die ID direkt über `UUID.randomUUID().toString()` erzeugen. Die Domain hängt dadurch nur von der Standardbibliothek ab, nicht von Spring, JPA oder Persistenzdetails.

## Mutabilität

Das Domain Model wird immutable modelliert.

Änderungen an `InventoryItem` erzeugen eine neue, fachlich gültige Instanz.

Beispiele:

- Istbestand direkt setzen
- Istbestand erhöhen
- Istbestand verringern
- ein Stück entnehmen
- auf Sollbestand setzen
- Artikeldaten bearbeiten

Die Änderungsmethoden kapseln die fachlichen Regeln. Ein öffentlicher `copy`-Mechanismus, der Regeln umgehen könnte, wird vermieden.

## Application-Use-Cases

Version 1 verwendet eine zentrale Implementierung `InventoryService`.

Nach außen werden die Anwendungsfälle über getrennte Interfaces sichtbar. Dadurch bleibt die Implementierung schlank, während Web-Adapter und Tests use-case-nahe Verträge verwenden können.

Vorgesehene Interfaces:

- `CreateInventoryItemUseCase`
- `EditInventoryItemUseCase`
- `DeleteInventoryItemUseCase`
- `SetCurrentStockUseCase`
- `IncreaseCurrentStockUseCase`
- `DecreaseCurrentStockUseCase`
- `RemoveOneItemUseCase`
- `SetStockToTargetUseCase`
- `ViewInventoryItemsUseCase`
- `ViewShoppingListUseCase`

`InventoryService` implementiert diese Interfaces.

## Persistence-Port

Die Application-Schicht definiert einen fachlichen Port `InventoryItemRepository`.

Vorgesehene Operationen:

- `save(item)`
- `findById(id)`
- `findAll()`
- `deleteById(id)`
- `existsByNormalizedName(normalizedName)`
- `existsByNormalizedNameExcludingId(normalizedName, excludedId)`

Der Port arbeitet mit Domain-Typen und enthält keine Spring-Data-, JPA- oder SQL-Abhängigkeiten.

## Fehlerbehandlung

Fachliche Fehler werden in Version 1 über klar benannte Exceptions modelliert.

Beispiele:

- `InventoryItemNotFoundException`
- `DuplicateItemNameException`
- `InvalidItemNameException`
- `InvalidCurrentStockException`
- `InvalidMinimumStockException`
- `InvalidTargetStockException`
- `InvalidStockOperationException`

Domain und Application schützen ihre Regeln über diese Exceptions. Der Web-Adapter übersetzt sie später in geeignete Formular- oder Seitenmeldungen.

## Application-DTOs

Die Use-Case-Interfaces arbeiten nach außen mit Command- und View-DTOs.

Schreibende Use Cases erhalten Commands, zum Beispiel:

- `CreateInventoryItemCommand`
- `EditInventoryItemCommand`
- `SetCurrentStockCommand`
- `ChangeCurrentStockCommand`

Lesende Use Cases liefern Views, zum Beispiel:

- `InventoryItemView`
- `ShoppingListItemView`
- `InventoryOverviewView`

Die DTOs gehören zur Application-Schicht. Sie enthalten keine Web-, Thymeleaf-, HTMX-, JPA- oder Spring-Data-Abhängigkeiten.

Innerhalb der Application-Schicht werden Commands in Domain-Typen übersetzt und Domain-Objekte in Views abgebildet.

## Persistenzmodell

Der Persistence-Adapter verwendet eigene JPA-Entities und Mapper zwischen Persistenzmodell und Domain Model.

Vorgesehene Bausteine:

- `InventoryItemJpaEntity`
- `InventoryItemJpaRepository`
- `InventoryItemJpaAdapter`
- `InventoryItemJpaMapper`

Die Domain-Typen werden nicht mit JPA-Annotationen versehen.

Liquibase definiert das relationale Datenbankschema. JPA wird nicht zur automatischen Schema-Erzeugung verwendet.

### Datenbankschema Version 1

Vorgesehene Tabelle:

- `inventory_items`

Vorgesehene Spalten:

- `id`
- `name`
- `normalized_name`
- `current_stock`
- `minimum_stock`
- `target_stock`
- `note`
- `created_at`
- `updated_at`

`normalized_name` erhält einen eindeutigen Constraint, damit die fachliche Eindeutigkeit auch auf Datenbankebene abgesichert wird.

`created_at` und `updated_at` sind technische Persistenzinformationen und werden nicht Teil des Domain Models.

## Web-Schicht

Die Web-Schicht wird für Version 1 entlang der beiden Listenansichten geschnitten:

- `InventoryItemController` für die Bestandsliste und Artikelpflege
- `ShoppingListController` für die Einkaufsliste

Die Listen werden als zwei eigene serverseitig gerenderte Routen umgesetzt:

- `/items` für die Istbestandspflege
- `/shopping-list` für die Einkaufsliste

Die Root-Route leitet auf `/items` weiter.

Der `InventoryItemController` verantwortet:

- Anzeige der Vorratsliste
- Suche und Sortierung der Vorratsliste
- Formulare zum Erfassen und eine Detailseite zum Bearbeiten von Artikeln
- Löschen von Artikeln nach Bestätigung
- Istbestand direkt setzen
- Istbestand erhöhen
- Istbestand verringern
- ein Stück entnehmen
- auf Sollbestand setzen

Der `ShoppingListController` verantwortet:

- Anzeige der Einkaufsliste
- Sortierung der Einkaufsliste
- Anzeige von Einkaufshinweisen aus Artikelnotizen

Beide Controller greifen ausschließlich auf die passenden Application-Use-Case-Interfaces zu und nicht direkt auf Domain oder Persistence-Adapter.

HTMX ergänzt die serverseitig gerenderten Seiten in Version 1 über `hx-boost` für Navigation und Formulare. Die Antworten bleiben vollständige Seiten; fragmentbasierte Aktualisierungen einzelner Listenbereiche werden in Version 1 nicht verwendet.

Bestandsliste und Einkaufsliste sind klassische serverseitig gerenderte Routen. Anlegen, Bearbeiten, Löschen, Suche, Sortierung und Bestandsaktionen erfolgen per Request/Response und können später bei Bedarf fragmentbasiert verfeinert werden.

## Testschnitt

Der erste Umsetzungsschritt konzentriert sich auf Domain- und Application-Tests.

Domain-Tests decken insbesondere ab:

- `ItemName`
- `InventoryItemId`
- `CurrentStock`
- `MinimumStock`
- `TargetStock`
- `InventoryItem`
- Nachkaufpflicht
- empfohlene Einkaufsmenge
- Istbestandsoperationen

Application-Tests decken insbesondere `InventoryService` mit einem Mock- oder Fake-Repository ab:

- Artikel anlegen
- Dublettenprüfung über normalisierten Artikelnamen
- Artikel bearbeiten
- Artikel löschen
- Istbestand setzen, erhöhen und verringern
- Einkaufsliste ableiten
- Suche und Sortierung

Web-Tests werden ergänzt, sobald Controller und Templates konkret umgesetzt werden.

## Value Objects

Für die Stückzahlen werden getrennte Value Objects verwendet:

- `ItemName`
- `CurrentStock`
- `MinimumStock`
- `TargetStock`

Damit bleiben die fachlichen Rollen der Werte im Code sichtbar.

Vorgesehene Verantwortlichkeiten:

- `ItemName` stellt sicher, dass der Artikelname nicht leer ist.
- `ItemName` bewahrt den Anzeigenamen und stellt zusätzlich einen normalisierten Vergleichswert bereit.
- `ItemName` normalisiert für den Vergleich führende und nachfolgende Leerzeichen, Groß- und Kleinschreibung sowie mehrfach vorkommende Leerzeichen innerhalb des Namens.
- `CurrentStock` stellt sicher, dass der Istbestand eine nicht-negative ganze Zahl ist.
- `MinimumStock` stellt sicher, dass der Mindestbestand mindestens 1 ist.
- `TargetStock` stellt sicher, dass der Sollbestand mindestens 1 ist.
- `InventoryItem` stellt sicher, dass der Sollbestand größer oder gleich dem Mindestbestand ist.
- `InventoryItem` stellt sicher, dass Istbestandsverringerungen nicht zu einem negativen Istbestand führen.
