# TODO – Re-Implementierung Download-Statistik (Follow-up auf neu gepushten Stand)

- [x] Aktuellen Code-Stand prüfen und bestehende Router-zentrierte Zählung validieren.
- [x] Download-Snapshot stabilisieren (deterministische Reihenfolge der Maps für konsistente JSON-Ausgabe).
- [x] Zählung robuster machen (Pfad normalisieren, führende Slashes entfernen).
- [x] Keine Tests ändern/hinzufügen (Anweisung beachtet).
- [x] Verifikation dokumentieren.

## Review
- Die Download-Zählung bleibt Router-zentriert, wurde aber robuster gemacht: Pfade werden vor dem Zählen normalisiert (`/docs/a.pdf` und `docs/a.pdf` landen auf demselben Key).
- Snapshot-Maps für Downloads sind nun deterministisch (`TreeMap`), wodurch JSON-Ausgaben und CLI-Darstellung stabiler vergleichbar sind.
- Verifikation ohne Tests: `git diff --check` (sauber), gezielte `rg`-Prüfung auf neue Normalisierungs- und Snapshot-Logik, manueller Diff-Review.
