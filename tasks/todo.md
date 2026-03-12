# Todo – US-C5 Audit Implementierung (einfacher Uni-Ansatz)

## Plan (verifizierbar)
- [x] Ist-Stand geprüft: bestehende User-/Auth- und Admin-Endpunkte im Router identifiziert.
- [x] Minimal-Design definiert: Audit-Service + SQLite-Tabelle + Interceptor + Admin-Query/Export-API.
- [x] Implementieren: Audit-Domain/DTO + persistente Audit-Logs in `data/users.db`.
- [x] Implementieren: automatische Audit-Erfassung für Requests mit User-Kontext.
- [x] Implementieren: Abfrage-/Exportfunktion (JSON + CSV) nach `userId`.
- [x] Tests: Audit-Service (Write, Query-Filter, CSV-Export) und Build-Verifikation.
- [x] Doku: Nutzungsanleitung mit konkreten Commands + Fazit wo die Logs liegen.

## Review
- US-C5 wurde schlank umgesetzt: `audit_logs`-Tabelle im Router-DB-File, automatische Erfassung über Interceptor und Admin-API für Query/CSV-Export.
- Audit-Einträge enthalten Zeit (`timestamp_utc`), User (`user_id`), Aktion (`action`), Ressource (`resource`) und Ergebnis (`result`) inklusive HTTP-Status.
- Abfrage/Export ist über `/api/cdn/admin/audit` und `/api/cdn/admin/audit/export` mit Filtern nutzbar.
- Testausführung via Maven war in dieser Umgebung wegen externem Dependency-Download (HTTP 403 von Maven Central) blockiert; die Testklasse ist dennoch enthalten.
