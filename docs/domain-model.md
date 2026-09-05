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
- einen aktuellen Bestand
- einen Mindestbestand
- einen Zielbestand
- optional eine Notiz

Der Artikel ist in Version 1 das zentrale fachliche Objekt.

### Artikelname

Der Artikelname bezeichnet einen Artikel fachlich eindeutig.

Unterschiedliche Packungsgrößen, Füllmengen oder Gewichte sind Bestandteil des Artikelnamens.

Der Artikelname darf nicht leer sein. Mehrere Artikel dürfen nicht denselben normalisierten Namen haben. Groß- und Kleinschreibung allein unterscheiden zwei Artikelnamen nicht.

### Bestand

Der Bestand beschreibt, wie viele Stück eines Artikels aktuell im Haushalt vorhanden sind.

Der Bestand ist eine nicht-negative ganze Zahl.

### Mindestbestand

Der Mindestbestand beschreibt die fachliche Untergrenze eines Artikels.

Sinkt der aktuelle Bestand unter den Mindestbestand, ist der Artikel nachkaufpflichtig.

Der Mindestbestand ist verpflichtend und muss mindestens 1 sein.

### Zielbestand

Der Zielbestand beschreibt den gewünschten Bestand nach einem Einkauf.

Der Zielbestand ist verpflichtend, muss mindestens so groß wie der Mindestbestand sein und dient zur Berechnung der empfohlenen Einkaufsmenge.

Der aktuelle Bestand darf über dem Zielbestand liegen.

### Notiz

Die Notiz ist ein optionaler Hinweistext zu einem Artikel.

Sie hat in Version 1 keine eigene steuernde Fachlogik. Bei nachkaufpflichtigen Artikeln wird sie in der Einkaufsliste als Einkaufshinweis angezeigt.

Die Notiz darf maximal 500 Zeichen lang sein.

### Einkaufsliste

Die Einkaufsliste ist eine aus den aktuellen Artikelbeständen abgeleitete Sicht.

Sie enthält alle Artikel, deren aktueller Bestand unter dem Mindestbestand liegt.

Die Einkaufsliste wird in Version 1 nicht manuell gepflegt. Einträge können nicht ergänzt oder abgehakt werden.

### Einkaufsposition

Eine Einkaufsposition beschreibt einen nachkaufpflichtigen Artikel in der Einkaufsliste.

Sie enthält:

- Artikelname
- aktueller Bestand
- Mindestbestand
- Zielbestand
- empfohlene Einkaufsmenge
- optionale Notiz als Einkaufshinweis

Die empfohlene Einkaufsmenge berechnet sich aus:

```text
Zielbestand - aktueller Bestand
```

Da Einkaufspositionen nur für Artikel unter Mindestbestand entstehen und der Zielbestand mindestens so groß wie der Mindestbestand sein muss, ist die empfohlene Einkaufsmenge im Normalfall mindestens 1.

## Use Cases Version 1

### Artikel anlegen

Ein neuer Artikel wird mit Name, aktuellem Bestand, Mindestbestand, Zielbestand und optionaler Notiz angelegt.

Vorbedingungen:

- Der Name ist nicht leer.
- Der normalisierte Name existiert noch nicht.
- Der aktuelle Bestand ist eine nicht-negative ganze Zahl.
- Der Mindestbestand ist eine ganze Zahl größer oder gleich 1.
- Der Zielbestand ist eine ganze Zahl größer oder gleich dem Mindestbestand.

Ergebnis:

- Der Artikel ist angelegt.
- Falls der aktuelle Bestand unter dem Mindestbestand liegt, erscheint der Artikel auf der Einkaufsliste.

### Artikel bearbeiten

Name, Mindestbestand, Zielbestand und Notiz eines bestehenden Artikels können geändert werden.

Vorbedingungen:

- Der Artikel existiert.
- Der neue Name ist nicht leer.
- Der normalisierte neue Name kollidiert nicht mit einem anderen Artikel.
- Der neue Mindestbestand ist eine ganze Zahl größer oder gleich 1.
- Der neue Zielbestand ist eine ganze Zahl größer oder gleich dem neuen Mindestbestand.

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

### Bestand direkt setzen

Der aktuelle Bestand eines bestehenden Artikels wird auf einen konkreten Wert gesetzt.

Vorbedingungen:

- Der Artikel existiert.
- Der neue Bestand ist eine nicht-negative ganze Zahl.

Ergebnis:

- Der aktuelle Bestand ist aktualisiert.
- Die Einkaufsliste ergibt sich anschließend neu aus dem aktuellen Zustand.

### Bestand erhöhen

Der aktuelle Bestand eines bestehenden Artikels wird um eine Menge erhöht.

Die Oberfläche soll eine schnelle Erhöhung um 1 sowie eine frei eingebbare Erhöhungsmenge unterstützen.

Vorbedingungen:

- Der Artikel existiert.
- Die Erhöhungsmenge ist eine ganze Zahl größer oder gleich 1.

Ergebnis:

- Der aktuelle Bestand ist um die Menge erhöht.
- Die Einkaufsliste ergibt sich anschließend neu aus dem aktuellen Zustand.

### Bestand verringern

Der aktuelle Bestand eines bestehenden Artikels wird um eine Menge verringert.

Die Oberfläche soll eine schnelle Verringerung um 1 sowie eine frei eingebbare Verringerungsmenge unterstützen.

Vorbedingungen:

- Der Artikel existiert.
- Die Verringerungsmenge ist eine ganze Zahl größer oder gleich 1.
- Der Bestand darf durch die Verringerung nicht negativ werden.

Ergebnis:

- Der aktuelle Bestand ist um die Menge verringert.
- Die Einkaufsliste ergibt sich anschließend neu aus dem aktuellen Zustand.

### Ein Stück entnehmen

Ein Stück eines bestehenden Artikels wird entnommen.

Dieser Use Case ist eine Komfortvariante von "Bestand verringern" mit der Menge 1.

### Auf Zielbestand setzen

Der aktuelle Bestand eines bestehenden Artikels wird auf seinen Zielbestand gesetzt.

Die Aktion dient als Auffüllaktion und darf nur verwendet werden, wenn der aktuelle Bestand unter dem Zielbestand liegt.

Vorbedingungen:

- Der Artikel existiert.
- Der aktuelle Bestand liegt unter dem Zielbestand.

Ergebnis:

- Der aktuelle Bestand entspricht dem Zielbestand.
- Der Artikel ist anschließend nicht nachkaufpflichtig.

### Vorratsliste anzeigen

Die Vorratsliste zeigt alle Artikel.

Sie unterstützt:

- Suche nach Artikelname
- Standardsortierung nach Artikelname
- Sortierung nach kritischen Artikeln zuerst
- Sortierung nach aktuellem Bestand aufsteigend
- Sortierung nach aktuellem Bestand absteigend

### Einkaufsliste anzeigen

Die Einkaufsliste zeigt alle aktuell nachkaufpflichtigen Artikel.

Sie unterstützt:

- Standardsortierung nach Artikelname
- Sortierung nach größter empfohlener Einkaufsmenge zuerst
- Sortierung nach kleinster empfohlener Einkaufsmenge zuerst

## Fachliche Regeln

- Ein Artikel ist nachkaufpflichtig, wenn sein aktueller Bestand kleiner als sein Mindestbestand ist.
- Ein Artikel ist nicht nachkaufpflichtig, wenn sein aktueller Bestand größer oder gleich seinem Mindestbestand ist.
- Die empfohlene Einkaufsmenge ist die Differenz zwischen Zielbestand und aktuellem Bestand.
- Bestand, Mindestbestand und Zielbestand sind Stückzahlen.
- Bestand, Mindestbestand und Zielbestand sind ganze Zahlen.
- Der aktuelle Bestand darf 0 sein.
- Der aktuelle Bestand darf nicht negativ sein.
- Der Mindestbestand muss mindestens 1 sein.
- Der Zielbestand muss größer oder gleich dem Mindestbestand sein.
- Der aktuelle Bestand darf größer als der Zielbestand sein.
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
