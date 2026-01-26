layout: page
title: "Privacy Policy"
permalink: /privacy-policy

# Datenschutzerklärung für Gold Portfolio

**Stand:** 26. Januar 2026

Der Schutz Ihrer persönlichen Daten ist uns ein wichtiges Anliegen. Diese Datenschutzerklärung erläutert, wie die Android-App **Gold Portfolio** (im Folgenden "die App") Daten verarbeitet.

## 1. Grundsatz der lokalen Datenspeicherung
Die App ist darauf ausgelegt, Ihre Privatsphäre maximal zu schützen. Alle von Ihnen eingegebenen Informationen über Ihre Goldbestände (wie Bezeichnung, Menge, Kaufpreise und Historie) werden ausschließlich **lokal auf Ihrem Endgerät** in einer gesicherten Datenbank (Room/SQLite) gespeichert.

Es findet keine automatische Übertragung Ihrer Portfoliodaten an unsere Server oder Dritte statt.

## 2. Datenerhebung und -verarbeitung

### a) Marktpreis-Aktualisierungen
Um Ihnen aktuelle Marktwerte anzuzeigen, ruft die App Daten von externen Schnittstellen ab:
* **GoldAPI.io:** Zur Abfrage des aktuellen Gold-Spotpreises über eine verschlüsselte HTTPS-Verbindung.
* **Philoro:** Gegebenenfalls werden öffentlich zugängliche Preisdaten von Edelmetallhändlern abgerufen, um spezifische Produktpreise zu aktualisieren.

Bei diesen Anfragen werden technisch bedingt Daten wie Ihre IP-Adresse an die Server der Anbieter übermittelt. Es werden jedoch **keine** Informationen über den Inhalt Ihres Portfolios übertragen.

### b) Backup- und Wiederherstellungsfunktion
Die App bietet eine manuelle Backup-Funktion an:
* **Export:** Wenn Sie ein Backup erstellen, wird eine JSON-Datei mit Ihren Portfoliodaten lokal auf Ihrem Gerät generiert.
* **Teilen-Funktion:** Wenn Sie die "Teilen"-Funktion nutzen, um das Backup in einen Cloud-Speicher (z. B. Google Drive) oder per E-Mail zu versenden, unterliegt die Sicherheit dieser Daten den Bestimmungen des von Ihnen gewählten Drittanbieters.

## 3. Erforderliche Berechtigungen
Die App benötigt lediglich Berechtigungen, die für die Kernfunktionen technisch notwendig sind:
* **INTERNET:** Um aktuelle Goldpreise von der API abzurufen.
* **DATEIZUGRIFF (via FileProvider):** Ermöglicht den sicheren Export und Import von Backup-Dateien.

## 4. Analyse und Tracking
Diese App verwendet **keine** Analyse-Software (wie Google Analytics, Firebase Analytics) und enthält **keine** Werbenetzwerke. Ihr Nutzungsverhalten wird nicht ausgewertet.

## 5. Ihre Rechte und Datenlöschung
Da die Datenhoheit vollständig bei Ihnen liegt, können Sie Ihre Daten jederzeit selbst verwalten:
* **Löschung:** Durch das Löschen eines Eintrags in der App oder durch die Deinstallation der App werden alle lokal gespeicherten Daten sofort und unwiderruflich von Ihrem Gerät entfernt.
* **Auskunft:** Da wir keine Daten zentral speichern, können wir keine Auskunft über Ihre Bestände geben. Ihre Daten gehören Ihnen.

## 6. Kontakt
Bei Fragen zu dieser Datenschutzerklärung oder der App können Sie mich über GitHub kontaktieren:
[Github-Profil](https://sebastianrn.github.io/PortfolioApp/)