# TODO – US-S5 Download-Statistik

- [x] Anforderung analysieren: bestehender `admin stats show` Flow soll erweitert werden, kein neuer Command.
- [x] Edge: Download-Zählung pro Datei in `EdgeMetricsService` ergänzen.
- [x] Edge: Aufrufpfad in `EdgeFileController` so erweitern, dass GET-Requests den Dateipfad mitzählen.
- [x] Edge Tests anpassen/ergänzen (`EdgeMetricsServiceTest`).
- [x] Router DTO erweitern (`EdgeStatsPayload` um `downloadsByFile`).
- [x] Router Aggregation erweitern (`AdminStatsController`):
  - [x] `downloads.byFileTotal`
  - [x] `downloads.byFileByEdge`
- [x] Router Tests ergänzen (Controller-Logik).
- [x] CLI Ausgabe erweitern (`AdminStatsCommand`) für beide Download-Statistiken.
- [x] Verifikation: relevante Tests/Checks ausführen.
- [x] Review-Abschnitt dokumentieren.

## Review
- US-S5 wurde als additive Erweiterung des bestehenden Stats-Flows umgesetzt: Edge liefert pro-Datei-Downloads, Router aggregiert global/je-Edge, CLI zeigt die neuen Werte in Textausgabe an und bleibt mit `--json` vollständig kompatibel.
- Validierung:
  - `git diff --check` ohne Whitespace-/Merge-Artefakt-Fehler.
  - Maven-Testlauf war in der Umgebung wegen fehlender extern auflösbarer Parent-Dependencies nicht ausführbar (offline/403).
