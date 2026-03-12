# Umsetzungsskizze (ohne Coding)

## Kontext
Dieses Dokument beschreibt, wie TS-S2 (Adapter) und US-C5 (Audit) strukturiert umgesetzt bzw. abgesichert werden würden, ohne direkt Implementierungscode zu ändern.

## TS-S2: Adapter

### Zielbild
- Fachliche Logik liegt hinter Ports/Interfaces (Use-Case-orientiert).
- Transportschicht (HTTP, später gRPC) implementiert ausschließlich Adapter.
- Composition Root (z. B. Bean-Konfiguration) entscheidet, welcher Adapter aktiv ist.

### Konkrete Struktur
1. **Domain/Service-Port**
   - Beispielhaft bestehen bereits Ports wie `OriginClient` (Edge) oder `TransportClient` (CLI).
   - Fachliche Services hängen nur von diesen Ports ab.
2. **Adapter je Transport**
   - HTTP-Adapter kapselt Requests/Responses/Statuscodes.
   - Optionaler gRPC-Adapter implementiert denselben Port mit Protobuf-Mapping.
3. **Konfiguration/DI**
   - Austausch per Profil/Property/Factory, ohne Service-Code zu ändern.

### Abnahmesicherung
- Unit-Tests der Fachlogik mit Fakes/Mocks des Ports.
- Adapter-Tests separat pro Transport.
- Nachweis: Austausch HTTP↔gRPC ändert nur Konfiguration/Wiring.

### Einschätzung
- **Schwierigkeit:** niedrig bis mittel, da die Grundstruktur bereits adapterfreundlich ist.
- **Risiko:** gering; hauptsächlich Konsistenz und saubere Paketgrenzen.

## US-C5: Audit

### Zielbild
- Jede relevante Aktion (wer, wann, was, Ergebnis) erzeugt einen unveränderlichen Audit-Eintrag.
- Audit ist abfragbar und exportierbar.
- Korrektheit wird über deterministische Tests sichergestellt.

### Konkrete Struktur
1. **Audit-Domainmodell**
   - Felder: `timestamp`, `actorId`, `action`, `resource`, `result`, `correlationId`, optional `metadata`.
2. **Audit-Port**
   - `AuditLogPort.append(entry)` für Write-Path.
   - `AuditQueryPort.query(filter)` und `export(filter, format)` für Read-/Export-Path.
3. **Adapter/Persistenz**
   - Initial z. B. relationale Tabelle oder append-only Datei.
   - Export-Adapter für CSV/JSON.
4. **Integration in Use Cases**
   - Nach jeder sicherheits- oder datenrelevanten Aktion Audit-Aufruf.
   - Fehlerpfade ebenfalls auditieren (`result=FAILURE`).
5. **Query/Export API**
   - Filter nach User, Zeitraum, Ergebnis, Aktion.
   - Paginierung und Sortierung nach Zeit.

### Korrektheitsstrategie
- Unit-Tests: Mapping + Filterlogik.
- Integrationstests: Persistenz, Sortierung, Zeitfenster, Ergebnisflags.
- E2E: Mehrere Aktionen eines Nutzers ausführen, danach Query/Export gegen erwartete Timeline prüfen.

### Einschätzung
- **Schwierigkeit:** mittel.
- **Hauptaufwand:** konsistente Instrumentierung aller relevanten Aktionen und robuste Filter-/Exportlogik.
- **Risiko:** mittel (fehlende/inkonsistente Event-Erfassung, Zeit-/Zeitzonenfehler, unvollständige Failure-Logs).

## Grober Aufwand (relativ)
- TS-S2 Absicherung/Nachschärfung: **1–2 Tage**.
- US-C5 End-to-End inkl. Tests/Export: **3–6 Tage** (abhängig von gewünschtem Exportumfang und Datenhaltung).

## Empfohlene Reihenfolge
1. Audit-Domain + Ports finalisieren.
2. Write-Path in Kern-Use-Cases instrumentieren.
3. Query/Export ergänzen.
4. Korrektheit über Integrations- und E2E-Tests nachweisen.
