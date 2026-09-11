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
- Direkt sichtbare Erfassungszeile in der Bestandsliste und eigene Detailseite für die Bearbeitung

Nicht zum Umfang von Version 1 gehören vorbereitende Fachfunktionen für asynchrone Benachrichtigungen, Verbrauchstracking, Haltbarkeitsverwaltung, Import/Export oder Benutzerverwaltung.

## Umfang Version 1.1

Version 1.1 legt den fachlichen Fokus auf die Einkaufsliste.

Zum Umfang gehören:

- tabellarisches Layout der Einkaufsliste statt Card-Layout
- reduzierte Anzeige der Einkaufsliste mit Artikel und Einkaufsmenge
- Istbestand einzelner Einkaufslistenpositionen direkt aus der Einkaufsliste auf Sollbestand setzen
- Istbestand aller aktuellen Einkaufslistenpositionen über die Einkaufsliste auf Sollbestand setzen
- Sicherheitsabfrage vor dem Setzen aller Einkaufslistenpositionen auf Sollbestand
- manueller Versand der aktuellen Einkaufsliste per E-Mail aus der Einkaufsliste heraus
- automatischer Versand der aktuellen Einkaufsliste per E-Mail in einem konfigurierbaren Rhythmus
- konfigurierbare Empfängerliste für den E-Mail-Versand
- konfigurierbare Versandbedingung für den automatischen Versand
- HTML-E-Mail mit einfachem tabellarischem Abbild der Einkaufsliste
- durchnummerierte Versandvorgänge mit Anzeige der Versandnummer in der E-Mail
- Protokollierung der Versandprüfung und des Mailversands im Anwendungslog

Nicht zum Umfang von Version 1.1 gehören:

- Kategorien
- manuelles Ergänzen von Einkaufslistenpositionen
- Abhaken von Einkaufslistenpositionen
- automatische Produkterkennung oder externe Einkaufsdienste

Kategorien werden für Version 1.2 vorgesehen.

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

Das Erfassen neuer Artikel erfolgt in Version 1 über eine direkt sichtbare Erfassungszeile in der Bestandsliste.

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

### Navigation

Die Anwendung stellt in Version 1 zwei getrennte serverseitig gerenderte Seiten bereit:

- Die Bestandsliste unter `/items` dient als Hauptarbeitsbereich für das Erfassen und Pflegen der Artikel-Istbestände.
- Die Einkaufsliste unter `/shopping-list` zeigt alle Artikel, deren Istbestand unter dem Sollbestand liegt. Kritische Artikel unter dem Mindestbestand werden hervorgehoben.

Die Root-Route leitet auf die Bestandsliste weiter.

Bei bestehendem Nachkaufbedarf weist die Bestandsliste deutlich darauf hin und verlinkt auf die Einkaufsliste.

Wenn noch keine Artikel vorhanden sind, zeigt die Anwendung einen einfachen Hinweis.

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

### Einkaufsliste anzeigen ab Version 1.1

Die Einkaufsliste wird ab Version 1.1 tabellarisch angezeigt.

Je Einkaufslistenposition werden in der Tabelle nur folgende Informationen angezeigt:

- Artikel
- Einkaufsmenge

Zusätzlich enthält jede Zeile eine Aktion `Auf Sollbestand`. Diese Aktion setzt den Istbestand des jeweiligen Artikels auf seinen Sollbestand.

Im Seitenkopf der Einkaufsliste gibt es die Aktion `Alles auf Sollbestand`. Diese Aktion setzt den Istbestand aller aktuell angezeigten Einkaufslistenpositionen auf den jeweiligen Sollbestand. Vor der Ausführung muss die Aktion durch eine Sicherheitsabfrage bestätigt werden.

### Einkaufsliste per E-Mail versenden ab Version 1.1

Die aktuelle Einkaufsliste kann ab Version 1.1 per E-Mail an eine konfigurierte Empfängerliste versendet werden.

Der E-Mail-Inhalt enthält ein einfaches HTML-Abbild der Einkaufsliste. Das HTML-Abbild zeigt mindestens:

- Versandnummer
- Artikel
- Einkaufsmenge

Jeder Versandvorgang erhält eine fortlaufende Versandnummer. Die Versandnummer wird in der E-Mail angegeben, damit Nutzer erkennen können, welche E-Mail den aktuellen Einkaufsbedarf enthält.

Der Versand kann manuell durch den Nutzer aus der Einkaufsliste heraus ausgelöst werden.

Zusätzlich wird der Versand in einem konfigurierbaren Rhythmus automatisch getriggert. Konfigurierbar sind:

- Wochentage
- Uhrzeit

Für den automatischen Versand ist die Versandbedingung konfigurierbar.

Unterstützte Versandbedingungen:

- Versand, wenn sich der Bestand von Artikeln seit dem letzten Versand geändert hat und für mindestens einen dieser geänderten Artikel gilt: `Istbestand < Mindestbestand`.
- Versand, wenn sich der Bestand von Artikeln seit dem letzten Versand geändert hat und für mindestens einen dieser geänderten Artikel gilt: `Istbestand < Sollbestand`.

Wenn die konfigurierte Versandbedingung nicht erfüllt ist, wird kein automatischer E-Mail-Versand ausgelöst.

Das Ergebnis jeder automatischen Versandprüfung wird im Anwendungslog festgehalten. Das Log soll erkennen lassen, ob ein Versand ausgelöst wurde oder warum kein Versand ausgelöst wurde.

Das Ergebnis jedes Mailversands wird ebenfalls im Anwendungslog festgehalten. Das gilt für erfolgreichen und fehlgeschlagenen Versand.

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
- Bestandsliste und Einkaufsliste sind getrennte Seiten unter `/items` und `/shopping-list`.
- Ab Version 1.1 kann der Istbestand eines Artikels aus der Einkaufsliste auf den Sollbestand gesetzt werden.
- Ab Version 1.1 kann der Istbestand aller aktuellen Einkaufslistenpositionen gesammelt auf den jeweiligen Sollbestand gesetzt werden.
- Ab Version 1.1 muss das gesammelte Setzen aller Einkaufslistenpositionen auf Sollbestand bestätigt werden.
- Ab Version 1.1 wird jeder E-Mail-Versand der Einkaufsliste fortlaufend nummeriert.
- Ab Version 1.1 löst der automatische Versand nur dann eine E-Mail aus, wenn sich seit dem letzten Versand relevante Artikelbestände geändert haben und die konfigurierte Versandbedingung erfüllt ist.
- Ab Version 1.1 wird das Ergebnis jeder automatischen Versandprüfung im Anwendungslog protokolliert.
- Ab Version 1.1 wird das Ergebnis jedes Mailversands im Anwendungslog protokolliert.

## Offene Fragen

- Welche Kategorien sollen in Version 1.2 ergänzt werden?
- Sollen spätere Versionen manuelle Interaktionen mit der Einkaufsliste unterstützen, z. B. Ergänzen, Abhaken oder automatische Istbestandsaktualisierung?
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
