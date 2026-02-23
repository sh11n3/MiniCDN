# Lessons Learned

- Regel: Bei erneutem "neu machen" auf gepushtem Stand zuerst prüfen, was bereits korrekt ist, und dann gezielt nur Robustheit/Determinismus verbessern.
- Regel: Statistik-Keys (Dateipfade) vor Aggregation normalisieren, sonst entstehen Doppelzählungen durch unterschiedliche Pfadformen.
- Regel: Für Admin-JSON-Ausgaben deterministische Map-Reihenfolge liefern, um Ausgaben und Vergleiche stabil zu halten.
