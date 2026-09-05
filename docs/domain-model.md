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

Sie hat in Version 1 keine eigene steuernde Fachlogik. Bei nachkaufpflichtigen Artikeln wird sie in der Einkaufsliste als Einkaufshinweis angezeigt.

Die Notiz darf maximal 500 Zeichen lang sein.

### Einkaufsliste

Die Einkaufsliste ist eine aus den Artikel-Istbeständen abgeleitete Sicht.

Sie enthält alle Artikel, deren Istbestand unter dem Mindestbestand liegt.

Die Einkaufsliste wird in Version 1 nicht manuell gepflegt. Einträge können nicht ergänzt oder abgehakt werden.

### Einkaufsposition

Eine Einkaufsposition beschreibt einen nachkaufpflichtigen Artikel in der Einkaufsliste.

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

Da Einkaufspositionen nur für Artikel unter Mindestbestand entstehen und der Sollbestand mindestens so groß wie der Mindestbestand sein muss, ist die empfohlene Einkaufsmenge im Normalfall mindestens 1.

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
- Falls der aktuelle Istbestand unter dem Mindestbestand liegt, erscheint der Artikel auf der Einkaufsliste.

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
- Der Artikel ist anschließend nicht nachkaufpflichtig.

### Vorratsliste anzeigen

Die Vorratsliste zeigt alle Artikel.

Sie unterstützt:

- Suche nach Artikelname
- Standardsortierung nach Artikelname
- Sortierung nach kritischen Artikeln zuerst
- Sortierung nach Istbestand aufsteigend
- Sortierung nach Istbestand absteigend

### Einkaufsliste anzeigen

Die Einkaufsliste zeigt alle aktuell nachkaufpflichtigen Artikel.

Sie unterstützt:

- Standardsortierung nach Artikelname
- Sortierung nach größter empfohlener Einkaufsmenge zuerst
- Sortierung nach kleinster empfohlener Einkaufsmenge zuerst

## Fachliche Regeln

- Ein Artikel ist nachkaufpflichtig, wenn sein Istbestand kleiner als sein Mindestbestand ist.
- Ein Artikel ist nicht nachkaufpflichtig, wenn sein Istbestand größer oder gleich seinem Mindestbestand ist.
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
