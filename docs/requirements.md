# Anforderungen: Lebensmittelvorratsverwaltung

## Zielbild

Die Anwendung unterstützt einen Haushalt dabei, Lebensmittelvorräte einfach zu erfassen, Istbestände zu pflegen und rechtzeitig zu erkennen, welche Artikel nachgekauft werden müssen.

Der fachliche Fokus liegt zunächst auf einer schlanken Vorratsliste mit Mindestbeständen und einer daraus abgeleiteten Einkaufsliste. Die Vorratshaltung dient insbesondere dazu, einen überschaubaren Haushaltsvorrat im Sinne der staatlichen Empfehlungen zur privaten Katastrophenvorsorge zu pflegen.

## Umfang Version 1

Version 1 soll eine komfortable erste Haushaltsversion liefern.

Zum Umfang gehören:

- Artikel erfassen, anzeigen, bearbeiten und löschen
- Istbestand pflegen
- Mindestbestand und Sollbestand je Artikel verwalten
- Einkaufsliste aus den Istbeständen ableiten
- deutliche Hinweise auf Nachkaufbedarf innerhalb der Anwendung anzeigen
- einfache Suche nach Artikelname
- Sortierung von Vorratsliste und Einkaufsliste
- optionale Notizen als Einkaufshinweise
- Dialoge oder aufklappbare Bereiche für Erfassung und Bearbeitung

Nicht zum Umfang von Version 1 gehören vorbereitende Fachfunktionen für asynchrone Benachrichtigungen, Verbrauchstracking, Haltbarkeitsverwaltung, Import/Export oder Benutzerverwaltung.

## Grobe Funktionalität

### Artikel verwalten

Ein Lebensmittelartikel soll erfasst, angezeigt, geändert und gelöscht werden können.

Für jeden Artikel sollen mindestens folgende fachliche Informationen verwaltet werden:

- Name des Artikels
- Istbestand
- Mindestbestand
- Sollbestand
- optionale Notiz

Beim Erfassen eines Artikels müssen Name, Istbestand, Mindestbestand und Sollbestand angegeben werden.

Das Erfassen neuer Artikel soll in Version 1 über ein aufklappbares Formular oder einen Dialog direkt von der Startseite aus möglich sein.

Das Bearbeiten bestehender Artikel soll in Version 1 über eine eigene Detailseite möglich sein.

Vor dem Löschen eines Artikels muss die Aktion bestätigt werden.

Istbestandsangaben beziehen sich immer auf Stück. Unterschiedliche Packungsgrößen, Füllmengen oder Gewichte werden über den Artikelnamen unterschieden, zum Beispiel "Speiseöl (1L)" oder "Passierte Tomaten (500g)".

In der ersten Version werden keine Kategorien verwaltet. Kategorien können in einer späteren Version ergänzt werden.

Die optionale Notiz hat keine eigene steuernde Fachlogik. Sie kann Einkaufshinweise enthalten und soll bei Artikeln in der Einkaufsliste angezeigt werden.

### Istbestand pflegen

Der Istbestand eines Artikels soll möglichst einfach aktualisiert werden können.

In der ersten Version sollen folgende Bedienvarianten unterstützt werden:

- direkte Eingabe eines neuen Istbestands
- schnelle Erhöhung oder Verringerung um eine Menge
- einfache Verbrauchsaktion, z. B. "1 entnehmen"
- einfache Auffüllaktion, z. B. "auf Sollbestand setzen"

Die Istbestandspflege soll alltagstauglich sein und mit möglichst wenigen Schritten funktionieren.

### Mindestbestand überwachen

Für jeden Artikel muss ein Mindestbestand hinterlegt werden.

Wenn der Istbestand eines Artikels unter seinen Mindestbestand sinkt, gilt der Artikel als nachkaufpflichtig.

### Sollbestand verwalten

Für jeden Artikel muss ein Sollbestand hinterlegt werden.

Der Sollbestand beschreibt den gewünschten Istbestand nach einem Einkauf. Er dient dazu, für die Einkaufsliste eine konkrete empfohlene Einkaufsmenge zu berechnen.

Es ist erlaubt, den Istbestand über den Sollbestand hinaus aufzubauen.

### Einkaufsliste ableiten

Die Anwendung soll eine Liste aller Artikel anzeigen können, die aufgefüllt werden sollen.

Ein Artikel gehört auf diese Liste, wenn sein Istbestand unter dem hinterlegten Sollbestand liegt.

Die Einkaufsliste wird in der ersten Version ausschließlich aus den Istbeständen abgeleitet. Einträge können nicht manuell ergänzt oder abgehakt werden. Ein Artikel verschwindet erst von der Einkaufsliste, wenn sein Istbestand so erhöht wurde, dass er nicht mehr unter dem Sollbestand liegt.

Die empfohlene Einkaufsmenge ergibt sich aus der Differenz zwischen Sollbestand und Istbestand.

Liegt der Istbestand bereits auf oder über dem Sollbestand, beträgt die empfohlene Einkaufsmenge 0. Da ein Artikel nur bei Unterschreitung des Sollbestands auf die Einkaufsliste kommt, ist dieser Fall nur bei inkonsistenten oder später geänderten Stammdaten relevant.

Wenn für einen Artikel in der Einkaufsliste eine Notiz hinterlegt ist, soll sie dort als Einkaufshinweis angezeigt werden.

Vorratsliste und Einkaufsliste werden standardmäßig alphabetisch nach Artikelname sortiert.

In Version 1 soll es möglich sein, die Sortierung der Listen in der Oberfläche zu ändern.

Die Vorratsliste unterstützt folgende Sortierungen:

- Artikelname
- kritische Artikel zuerst, also Artikel unter Mindestbestand
- Istbestand aufsteigend
- Istbestand absteigend

Die Einkaufsliste unterstützt folgende Sortierungen:

- Artikelname
- größte empfohlene Einkaufsmenge zuerst
- kleinste empfohlene Einkaufsmenge zuerst

### Suche

In Version 1 soll eine einfache Textsuche nach Artikelname unterstützt werden.

Die Suche dient der schnelleren Bedienung und hat keine eigene fachliche Steuerungslogik.

### Startseite und Navigation

Die Startseite zeigt in Version 1 eine schlichte Oberfläche mit zwei getrennten Tabs:

- Istbestandspflege
- Einkaufsliste

Der Tab "Istbestandspflege" dient als Hauptarbeitsbereich für das Erfassen und Pflegen der Artikel-Istbestände.

Der Tab "Einkaufsliste" zeigt alle Artikel, deren Istbestand unter dem Sollbestand liegt. Kritische Artikel unter dem Mindestbestand sollen in der Liste hervorgehoben werden.

Bei bestehendem Nachkaufbedarf soll die Startseite deutlich darauf hinweisen.

Wenn noch keine Artikel vorhanden sind, zeigt die Anwendung einen einfachen Hinweis.

Wenn keine Artikel aufgefüllt werden müssen, zeigt die Einkaufsliste einen einfachen Hinweis.

### Begrifflichkeit

In Version 1 werden in der Oberfläche die folgenden Begriffe verwendet:

- Artikel
- Istbestand
- Mindestbestand
- Sollbestand
- Einkaufsliste
- Einkaufsmenge

### Benachrichtigung auslösen

Wenn mindestens ein Artikel unter den Mindestbestand sinkt, soll die Anwendung in Version 1 innerhalb der Oberfläche deutlich darauf hinweisen.

Die Oberfläche soll eine Liste aller aufzufüllenden Artikel anzeigen und kritische Artikel unter Mindestbestand deutlich hervorheben.

Eine aktive asynchrone Benachrichtigung wird in Version 1 noch nicht umgesetzt.

In einer späteren Version soll es definitiv eine aktive Benachrichtigungsfunktion geben. Voraussichtlich soll diese asynchron in einem festen, konfigurierbaren Rhythmus ausgeführt werden und eine Liste aller nachzukaufenden Artikel enthalten.

## Erste fachliche Regeln

- Ein Artikel mit Istbestand unter Mindestbestand ist nachkaufpflichtig.
- Ein Artikel mit Istbestand gleich Mindestbestand ist nicht nachkaufpflichtig.
- Ein Artikel mit Istbestand unter Sollbestand erscheint auf der Einkaufsliste.
- Ein Artikel mit Istbestand gleich oder über Sollbestand erscheint nicht auf der Einkaufsliste.
- Istbestand, Mindestbestand und Sollbestand sind nicht-negative ganze Zahlen.
- Der Mindestbestand muss mindestens 1 sein.
- Artikelnamen dürfen nicht leer sein.
- Artikelnamen müssen eindeutig sein. Die Eindeutigkeit wird über eine normalisierte Schreibweise geprüft, sodass sich Namen nicht nur durch Groß- und Kleinschreibung unterscheiden dürfen.
- Istbestand, Mindestbestand und Sollbestand werden immer als Stückzahl interpretiert.
- Jeder Artikel hat genau einen verpflichtenden Mindestbestand.
- Jeder Artikel hat genau einen verpflichtenden Sollbestand.
- Der Sollbestand muss größer oder gleich dem Mindestbestand sein.
- Der Istbestand darf größer als der Sollbestand sein.
- Ein Lagerort wird nicht verwaltet.
- Kategorien werden in der ersten Version nicht verwaltet.
- Die Einkaufsliste ist in der ersten Version eine reine Ableitung aus den Artikel-Istbeständen.
- Benachrichtigung bedeutet in Version 1 ausschließlich eine deutliche Anzeige innerhalb der Anwendung.
- In Version 1 gibt es keine Benutzerverwaltung und keine Unterscheidung nach Haushaltsmitgliedern.
- Mindesthaltbarkeitsdaten oder Ablaufdaten werden in Version 1 nicht verwaltet.
- Artikel werden in Version 1 hart gelöscht und nicht archiviert.
- Istbestandsänderungen werden in Version 1 nicht historisiert.
- Vorratsliste und Einkaufsliste werden standardmäßig alphabetisch nach Artikelname sortiert.
- Die Sortierung der Listen kann in Version 1 in der Oberfläche geändert werden.
- In Version 1 wird eine einfache Textsuche nach Artikelname unterstützt.
- Die Startseite trennt Istbestandspflege und Einkaufsliste in zwei Tabs.

## Offene Fragen

- Sollen Kategorien in einer späteren Version ergänzt werden?
- Sollen spätere Versionen manuelle Interaktionen mit der Einkaufsliste unterstützen, z. B. Ergänzen, Abhaken oder automatische Istbestandsaktualisierung?
- Wie soll die spätere asynchrone Benachrichtigung technisch und fachlich ausgestaltet werden?
- Wie soll in einer späteren Version der Verbrauch getrackt werden, um Altbestände ohne Verbrauch zu erkennen?

## Nicht-Ziele für die erste Ausbaustufe

Diese Punkte sind fachlich denkbar, gehören aber noch nicht zur ersten groben Funktionalität:

- Barcode-Scan
- automatische Produkterkennung
- Import oder Export von Artikeldaten
- Historisierung von Istbestandsänderungen
- Preisvergleich
- Cloud-Synchronisation
- Mehrbenutzerverwaltung mit Rollen und Rechten
- Integration externer Einkaufsdienste
- Mindesthaltbarkeits- oder Ablaufdaten
