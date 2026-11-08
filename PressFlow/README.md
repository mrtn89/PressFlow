# PressFlow

Interne Webanwendung zur kollaborativen Erstellung und Freigabe von Pressemitteilungen.
Basiert auf der Architektur von Require4Testing und modernisiert deren Stack:
Java 21, Jakarta EE 10 (Faces 4, CDI 4, Persistence 3.1), PrimeFaces 15, Hibernate 6, MySQL 8, Apache TomEE 10, Maven.

## Freigabeprozess

```
Entwurf → In Prüfung → Zur Freigabe → Freigegeben → Word-Export
              ↓              ↓
         Überarbeitung ←─────┘  (zurück an die Redaktion, danach erneut einreichen)
```

| Rolle | Aufgaben |
|---|---|
| Redakteur:in | Pressemitteilung anlegen, bearbeiten, bei einer Fachprüfer:in einreichen |
| Fachprüfer:in | Sachliche Richtigkeit bestätigen oder Überarbeitung anfordern |
| Pressesprecher:in | Final freigeben oder Überarbeitung anfordern |
| Administrator:in | Benutzer:innen anlegen; darf zu Testzwecken alle Schritte ausführen |

## Umgesetzte User Stories (Version 1)

- MUST: Als Mitarbeiter:in möchte ich mich anmelden, damit ich nur die Funktionen meiner Rolle nutze.
- MUST: Als Redakteur:in möchte ich Pressemitteilungen erstellen und bearbeiten.
- MUST: Als Redakteur:in möchte ich eine Pressemitteilung einer Fachprüfer:in zur Prüfung zuweisen.
- MUST: Als Fachprüfer:in möchte ich die sachliche Richtigkeit bestätigen oder eine Überarbeitung anfordern.
- MUST: Als Pressesprecher:in möchte ich geprüfte Pressemitteilungen freigeben.
- SHOULD: Als Beteiligte:r möchte ich Kommentare hinterlassen, um Änderungswünsche abzustimmen.
- SHOULD: Als Beteiligte:r möchte ich den Verlauf (Statushistorie) einsehen.
- SHOULD: Als Beteiligte:r möchte ich auf einer Übersicht meine offenen Aufgaben sehen.
- COULD: Als Pressesprecher:in möchte ich die freigegebene Pressemitteilung als Word-Datei exportieren.
- COULD: Als Administrator:in möchte ich neue Benutzer:innen anlegen.
- WON'T (diesmal): Veröffentlichung auf der Homepage, PDF-Export, Passwort zurücksetzen, LDAP/SSO.

## Projektstruktur

- `src/main/java/de/pressflow/model`: Entitäten und Enums (User, PressRelease, Comment, StatusChange)
- `src/main/java/de/pressflow/service`: Datenzugriff, Freigaberegeln (`WorkflowService`), Word-Export
- `src/main/java/de/pressflow/view`: CDI-/JSF-Beans für die Seiten
- `src/main/java/de/pressflow/security`: Login-Filter und Passwort-Hashing
- `src/main/java/de/pressflow/config`: `EntityManagerProducer` (aus Require4Testing übernommen)
- `src/main/webapp`: XHTML-Seiten, Facelets-Template, CSS
- `src/test/java`: Unit-Tests (JUnit 5) für Freigaberegeln, Passwort-Hashing und Word-Export
- `sql/create-database.sql`: Datenbank und Datenbankbenutzer anlegen

## Start (Kurzfassung)

1. `sql/create-database.sql` in MySQL ausführen.
2. Projekt in Eclipse über *File → Import → Existing Maven Projects* importieren.
3. Apache TomEE 10 als Server einrichten und das Projekt deployen.
4. `http://localhost:8080/pressflow/` öffnen.

Die ausführliche Schritt-für-Schritt-Anleitung für Eclipse liegt separat vor.

## Demo-Zugänge

Beim ersten Login werden automatisch Demo-Benutzer:innen angelegt (Passwort jeweils `pressflow`):

| Benutzername | Name | Rolle |
|---|---|---|
| redakteur | Lena Schulte | Redakteur:in |
| fachpruefer | Thomas Becker (Technik) | Fachprüfer:in |
| fachpruefer2 | Jonas Hoffmann (Finanzen) | Fachprüfer:in |
| pressesprecher | Sabine Krämer | Pressesprecher:in |
| admin | Administrator | Administrator:in |

## Unterschiede zu Require4Testing

- `javax.*` → `jakarta.*`, Java 8 → Java 21, `Date` → `LocalDateTime`
- Einfacher Login mit gehashten Passwörtern ersetzt die Rollenauswahl
- `AbstractService` kapselt Transaktionen inkl. Rollback
- Eigener Datenbankbenutzer statt `root`
