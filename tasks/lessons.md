# Lessons Learned

## 2026-03-14 – US-C4 Nachkorrektur (CLI-Sichtbarkeit + Help)
- Bei UX-orientierten CLI-Features immer explizit prüfen, ob der Nutzer den neuen Ablauf **im Terminal sichtbar** nachvollziehen kann (nicht nur funktional, auch observability).
- Bei Änderungen an CLI-Kommandos immer `--help`-Beispiele mitpflegen, insbesondere wenn Defaults (z. B. Host) den typischen Aufruf vereinfachen.
- Für CLI-Kommandos konsequent gemeinsame Utilities aus `common` für URL/Path/Exit-Code verwenden, um inkonsistente Validierung zu vermeiden.
- Segment-/Parallel-Features grundsätzlich mit mindestens einem Unit-Test für Grenzfälle (z. B. Segment-Capping) absichern.
