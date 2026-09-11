# Fachliches Domänenmodell: Lebensmittelvorratsverwaltung

## Zweck

Dieses Dokument leitet aus den Anforderungen für Version 1 die zentralen fachlichen Begriffe, Use Cases und Domänenregeln ab.

Es beschreibt noch keine technische Implementierung. Klassennamen, Persistenzmodell, Web-Endpunkte und UI-Struktur werden daraus später abgeleitet.

## Fachliche Begriffe

### Artikel

Ein Artikel beschreibt einen konkret zu bevorratenden Lebensmittelvorrat.

Beispiele:

- Speiseöl (1L)
- Passierte Tomaten (500g)
- Nudeln (500g)

Ein Artikel besitzt:

- einen eindeutigen Namen
- einen Istbestand
- einen Mindestbestand
- einen Sollbestand
- optional eine Notiz

Der Artikel ist in Version 1 das zentrale fachliche Objekt.

### Artikelname

Der Artikelname bezeichnet einen Artikel fachlich eindeutig.

Unterschiedliche Packungsgrößen, Füllmengen oder Gewichte sind Bestandteil des Artikelnamens.

Der Artikelname darf nicht leer sein. Mehrere Artikel dürfen nicht denselben normalisierten Namen haben. Groß- und Kleinschreibung allein unterscheiden zwei Artikelnamen nicht.

### Istbestand

Der Istbestand beschreibt, wie viele Stück eines Artikels aktuell im Haushalt vorhanden sind.

Der Istbestand ist eine nicht-negative ganze Zahl.

### Mindestbestand

Der Mindestbestand beschreibt die fachliche Untergrenze eines Artikels.

Sinkt der aktuelle Istbestand unter den Mindestbestand, ist der Artikel nachkaufpflichtig.

Der Mindestbestand ist verpflichtend und muss mindestens 1 sein.

### Sollbestand

Der Sollbestand beschreibt den gewünschten Istbestand nach einem Einkauf.

Der Sollbestand ist verpflichtend, muss mindestens so groß wie der Mindestbestand sein und dient zur Berechnung der empfohlenen Einkaufsmenge.

Der Istbestand darf über dem Sollbestand liegen.

### Notiz

Die Notiz ist ein optionaler Hinweistext zu einem Artikel.

Sie hat in Version 1 keine eigene steuernde Fachlogik. Bei Artikeln in der Einkaufsliste wird sie als Einkaufshinweis angezeigt.

Die Notiz darf maximal 500 Zeichen lang sein.

### Einkaufsliste

Die Einkaufsliste ist eine aus den Artikel-Istbeständen abgeleitete Sicht.

Sie enthält alle Artikel, deren Istbestand unter dem Sollbestand liegt.

Die Einkaufsliste wird in Version 1 nicht manuell gepflegt. Einträge können nicht ergänzt oder abgehakt werden.

### Einkaufsposition

Eine Einkaufsposition beschreibt einen aufzufüllenden Artikel in der Einkaufsliste.

Sie enthält:

- Artikelname
- Istbestand
- Mindestbestand
- Sollbestand
- empfohlene Einkaufsmenge
- optionale Notiz als Einkaufshinweis

Die empfohlene Einkaufsmenge berechnet sich aus:

```text
Sollbestand - Istbestand
```

Da Einkaufspositionen nur für Artikel unter Sollbestand entstehen, ist die empfohlene Einkaufsmenge mindestens 1.

## Use Cases Version 1

### Artikel anlegen

Ein neuer Artikel wird mit Name, Istbestand, Mindestbestand, Sollbestand und optionaler Notiz angelegt.

Vorbedingungen:

- Der Name ist nicht leer.
- Der normalisierte Name existiert noch nicht.
- Der aktuelle Istbestand ist eine nicht-negative ganze Zahl.
- Der Mindestbestand ist eine ganze Zahl größer oder gleich 1.
- Der Sollbestand ist eine ganze Zahl größer oder gleich dem Mindestbestand.

Ergebnis:

- Der Artikel ist angelegt.
- Falls der aktuelle Istbestand unter dem Sollbestand liegt, erscheint der Artikel auf der Einkaufsliste.

### Artikel bearbeiten

Name, Istbestand, Mindestbestand, Sollbestand und Notiz eines bestehenden Artikels können geändert werden.

Vorbedingungen:

- Der Artikel existiert.
- Der neue Name ist nicht leer.
- Der normalisierte neue Name kollidiert nicht mit einem anderen Artikel.
- Der neue Istbestand ist eine nicht-negative ganze Zahl.
- Der neue Mindestbestand ist eine ganze Zahl größer oder gleich 1.
- Der neue Sollbestand ist eine ganze Zahl größer oder gleich dem neuen Mindestbestand.

Ergebnis:

- Die Artikeldaten sind aktualisiert.
- Die Einkaufsliste ergibt sich anschließend neu aus dem aktuellen Zustand.

### Artikel löschen

Ein bestehender Artikel kann nach Bestätigung gelöscht werden.

Vorbedingungen:

- Der Artikel existiert.
- Die Löschaktion wurde bestätigt.

Ergebnis:

- Der Artikel ist gelöscht.
- Er erscheint nicht mehr in Vorratsliste oder Einkaufsliste.

### Istbestand direkt setzen

Der aktuelle Istbestand eines bestehenden Artikels wird auf einen konkreten Wert gesetzt.

Vorbedingungen:

- Der Artikel existiert.
- Der neue Istbestand ist eine nicht-negative ganze Zahl.

Ergebnis:

- Der aktuelle Istbestand ist aktualisiert.
- Die Einkaufsliste ergibt sich anschließend neu aus dem aktuellen Zustand.

### Istbestand erhöhen

Der aktuelle Istbestand eines bestehenden Artikels wird um eine Menge erhöht.

Die Oberfläche soll eine schnelle Erhöhung um 1 sowie eine frei eingebbare Erhöhungsmenge unterstützen.

Vorbedingungen:

- Der Artikel existiert.
- Die Erhöhungsmenge ist eine ganze Zahl größer oder gleich 1.

Ergebnis:

- Der aktuelle Istbestand ist um die Menge erhöht.
- Die Einkaufsliste ergibt sich anschließend neu aus dem aktuellen Zustand.

### Istbestand verringern

Der aktuelle Istbestand eines bestehenden Artikels wird um eine Menge verringert.

Die Oberfläche soll eine schnelle Verringerung um 1 sowie eine frei eingebbare Verringerungsmenge unterstützen.

Vorbedingungen:

- Der Artikel existiert.
- Die Verringerungsmenge ist eine ganze Zahl größer oder gleich 1.
- Der Istbestand darf durch die Verringerung nicht negativ werden.

Ergebnis:

- Der aktuelle Istbestand ist um die Menge verringert.
- Die Einkaufsliste ergibt sich anschließend neu aus dem aktuellen Zustand.

### Ein Stück entnehmen

Ein Stück eines bestehenden Artikels wird entnommen.

Dieser Use Case ist eine Komfortvariante von "Istbestand verringern" mit der Menge 1.

### Auf Sollbestand setzen

Der aktuelle Istbestand eines bestehenden Artikels wird auf seinen Sollbestand gesetzt.

Die Aktion dient als Auffüllaktion und darf nur verwendet werden, wenn der aktuelle Istbestand unter dem Sollbestand liegt.

Vorbedingungen:

- Der Artikel existiert.
- Der aktuelle Istbestand liegt unter dem Sollbestand.

Ergebnis:

- Der aktuelle Istbestand entspricht dem Sollbestand.
- Der Artikel erscheint anschließend nicht mehr auf der Einkaufsliste.

### Vorratsliste anzeigen

Die Vorratsliste zeigt alle Artikel.

Sie unterstützt:

- Suche nach Artikelname
- Standardsortierung nach Artikelname
- Sortierung nach kritischen Artikeln zuerst
- Sortierung nach Istbestand aufsteigend
- Sortierung nach Istbestand absteigend

### Einkaufsliste anzeigen

Die Einkaufsliste zeigt alle aktuell aufzufüllenden Artikel, deren Istbestand unter dem Sollbestand liegt.

Sie unterstützt:

- Standardsortierung nach Artikelname
- Sortierung nach größter empfohlener Einkaufsmenge zuerst
- Sortierung nach kleinster empfohlener Einkaufsmenge zuerst

## Fachliche Regeln

- Ein Artikel ist nachkaufpflichtig, wenn sein Istbestand kleiner als sein Mindestbestand ist.
- Ein Artikel ist nicht nachkaufpflichtig, wenn sein Istbestand größer oder gleich seinem Mindestbestand ist.
- Ein Artikel erscheint auf der Einkaufsliste, wenn sein Istbestand kleiner als sein Sollbestand ist.
- Ein Artikel erscheint nicht auf der Einkaufsliste, wenn sein Istbestand größer oder gleich seinem Sollbestand ist.
- Die empfohlene Einkaufsmenge ist die Differenz zwischen Sollbestand und Istbestand.
- Istbestand, Mindestbestand und Sollbestand sind Stückzahlen.
- Istbestand, Mindestbestand und Sollbestand sind ganze Zahlen.
- Der Istbestand darf 0 sein.
- Der Istbestand darf nicht negativ sein.
- Der Mindestbestand muss mindestens 1 sein.
- Der Sollbestand muss größer oder gleich dem Mindestbestand sein.
- Der Istbestand darf größer als der Sollbestand sein.
- Der Artikelname darf nicht leer sein.
- Der normalisierte Artikelname muss eindeutig sein.
- Die Notiz ist optional.
- Die Notiz darf maximal 500 Zeichen lang sein.

## Normalisierung des Artikelnamens

Für die Eindeutigkeitsprüfung wird der Artikelname normalisiert.

Für Version 1 ist mindestens erforderlich:

- führende und nachfolgende Leerzeichen entfernen
- Groß- und Kleinschreibung ignorieren
- mehrfach vorkommende Leerzeichen innerhalb des Namens zu einem Leerzeichen zusammenfassen

## Erweiterungen Version 1.1

Version 1.1 erweitert das Domänenmodell rund um die Einkaufsliste und deren E-Mail-Versand.

Kategorien gehören nicht zu Version 1.1. Sie sind für Version 1.2 vorgesehen.

### Einkaufslistenversand

Ein Einkaufslistenversand beschreibt den Versand der aktuellen Einkaufsliste per E-Mail.

Jeder Versand erhält eine fortlaufende Versandnummer. Die Versandnummer wird in der E-Mail angezeigt, damit Nutzer erkennen können, welche E-Mail den aktuellen Einkaufsbedarf enthält.

Ein Versand enthält fachlich:

- Versandnummer
- Versandzeitpunkt
- Empfänger
- versendete Einkaufspositionen

Die versendeten Einkaufspositionen bilden die Einkaufsliste zum Zeitpunkt des Versands ab. Sie enthalten mindestens:

- Artikelname
- Einkaufsmenge

### Versandbedingung

Die Versandbedingung steuert, wann der automatische Einkaufslistenversand eine E-Mail auslöst.

Unterstützte Bedingungen:

- Bestandsänderung seit dem letzten Versand und mindestens ein geänderter Artikel ist unter Mindestbestand.
- Bestandsänderung seit dem letzten Versand und mindestens ein geänderter Artikel ist unter Sollbestand.

Die Bedingung bezieht sich auf Artikel, deren Bestand sich seit dem letzten Versand geändert hat.

### Versandrhythmus

Der automatische Versand wird in einem konfigurierbaren Rhythmus geprüft.

Der Rhythmus besteht fachlich aus:

- Wochentagen
- Uhrzeit

Der Rhythmus beschreibt, wann geprüft wird, ob die konfigurierte Versandbedingung erfüllt ist.

### Use Cases Version 1.1

#### Einkaufsliste kompakt anzeigen

Die Einkaufsliste wird tabellarisch angezeigt.

Je Position werden in der Tabelle nur Artikelname und Einkaufsmenge angezeigt.

#### Einkaufslistenposition auf Sollbestand setzen

Der Istbestand eines Artikels aus der Einkaufsliste wird auf seinen Sollbestand gesetzt.

Vorbedingungen:

- Der Artikel existiert.
- Der Artikel steht aktuell auf der Einkaufsliste.
- Der aktuelle Istbestand liegt unter dem Sollbestand.

Ergebnis:

- Der Istbestand des Artikels entspricht dem Sollbestand.
- Der Artikel erscheint anschließend nicht mehr auf der Einkaufsliste.

#### Alle Einkaufslistenpositionen auf Sollbestand setzen

Der Istbestand aller aktuellen Einkaufslistenpositionen wird auf den jeweiligen Sollbestand gesetzt.

Vorbedingungen:

- Die Aktion wurde bestätigt.
- Es gibt mindestens eine Einkaufslistenposition.

Ergebnis:

- Für jeden Artikel der aktuellen Einkaufsliste entspricht der Istbestand dem Sollbestand.
- Die Einkaufsliste ist anschließend leer, solange keine zwischenzeitlichen Bestandsänderungen neue Einkaufspositionen erzeugt haben.

#### Einkaufsliste manuell versenden

Der Nutzer löst den Versand der aktuellen Einkaufsliste aus der Einkaufsliste heraus aus.

Vorbedingungen:

- Es ist mindestens ein Empfänger konfiguriert.
- Der E-Mail-Versand ist technisch konfiguriert.

Ergebnis:

- Die aktuelle Einkaufsliste wird per E-Mail versendet.
- Der Versand erhält eine neue fortlaufende Versandnummer.
- Der Versand wird als letzter Versand berücksichtigt.

#### Automatischen Einkaufslistenversand prüfen

Die Anwendung prüft im konfigurierten Rhythmus, ob eine E-Mail versendet werden soll.

Vorbedingungen:

- Es ist mindestens ein Empfänger konfiguriert.
- Der E-Mail-Versand ist technisch konfiguriert.
- Der konfigurierte Prüfzeitpunkt ist erreicht.

Ergebnis:

- Wenn die konfigurierte Versandbedingung erfüllt ist, wird die aktuelle Einkaufsliste per E-Mail versendet.
- Wenn die konfigurierte Versandbedingung nicht erfüllt ist, wird keine E-Mail versendet.
- Bei Versand erhält der Vorgang eine neue fortlaufende Versandnummer und wird als letzter Versand berücksichtigt.
- Das Ergebnis der Prüfung wird im Anwendungslog festgehalten.
- Das Ergebnis eines ausgelösten Mailversands wird im Anwendungslog festgehalten.

### Fachliche Regeln Version 1.1

- Die kompakte Einkaufsliste zeigt Artikelname und Einkaufsmenge.
- Eine Einkaufslistenposition kann aus der Einkaufsliste heraus auf Sollbestand gesetzt werden.
- Alle aktuellen Einkaufslistenpositionen können gesammelt auf Sollbestand gesetzt werden.
- Das gesammelte Setzen aller Einkaufslistenpositionen auf Sollbestand muss bestätigt werden.
- Jeder erfolgreiche Einkaufslistenversand erhält genau eine neue fortlaufende Versandnummer.
- Die Versandnummer wird in der E-Mail angezeigt.
- Der automatische Versand prüft nur zum konfigurierten Rhythmus, ob ein Versand erforderlich ist.
- Der automatische Versand sendet nur dann eine E-Mail, wenn die konfigurierte Versandbedingung erfüllt ist.
- Das Ergebnis jeder automatischen Versandprüfung wird im Anwendungslog protokolliert.
- Das Ergebnis jedes Mailversands wird im Anwendungslog protokolliert.
