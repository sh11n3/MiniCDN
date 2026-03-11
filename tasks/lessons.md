# Lessons Learned

## 2026-03-11
- Bei Nutzerkorrekturen sofort die Arbeitsweise anpassen und explizite Planungs-/Tracking-Artefakte (`tasks/todo.md`, `tasks/lessons.md`) pflegen.
- Vor jeder nicht-trivialen Implementierung zuerst eine prüfbare Spezifikation notieren.
- Verifizierungsschritte bereits im Plan vorsehen, nicht erst am Ende.

## 2026-03-11 (Korrektur: Platzierung & Nutzbarkeit)
- Bei Utility-/Benchmark-Skripten immer zuerst eine klare Repo-Location wählen (z. B. `scripts/performance/`) statt nur Root-Ablage.
- Für bestehende Nutzerpfade Kompatibilitäts-Wrapper im Root bereitstellen, wenn bereits referenziert.
- In der Doku immer explizit beantworten: *wo liegt die Datei*, *wie wird sie ausgeführt*, *wo landet der Output*.

## 2026-03-11 (Korrektur: Windows-Kompatibilität)
- Performance-/Utility-Tools für Teamnutzung standardmäßig plattformneutral bauen (Python/Java), nicht Bash-first.
- Bei Skripten immer mindestens einen nativen Windows-Ausführungspfad dokumentieren (`python ...` oder `.ps1`).
- Kompatibilitäts-Wrapper dürfen bleiben, aber die Hauptlogik muss in einem überall laufenden Entry-Point liegen.
